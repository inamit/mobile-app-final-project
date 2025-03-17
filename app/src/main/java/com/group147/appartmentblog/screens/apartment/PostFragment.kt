package com.group147.appartmentblog.screens.apartment

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentPostBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.screens.home.HomeActivity
import com.group147.appartmentblog.model.service.AuthService
import com.group147.appartmentblog.model.service.SubscriptionService
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.database.post.PostDao
import com.group147.appartmentblog.database.post.PostDatabase
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: FragmentPostBinding
    private val viewModel: PostViewModel by viewModels()
    private val args: PostFragmentArgs by navArgs()

    private var isEditMode = false
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var subscriptionService: SubscriptionService<Post>
    private lateinit var postDao: PostDao
    private lateinit var authService: AuthService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        (activity as HomeActivity).hideBottomNavBar()
        (activity as HomeActivity).hideAddApartmentButton()
        (activity as HomeActivity).showToolbarNavigationIcon()

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        subscriptionService = SubscriptionService((activity as HomeActivity).getPostRepository())
        postDao = PostDatabase.getDatabase(requireContext()).postDao()
        authService = AuthService()

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { galleryUri ->
                galleryUri?.let {
                    binding.postImageView.setImageURI(it)
                }
            }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
                bitmap?.let {
                    binding.postImageView.setImageBitmap(it)
                }
            }

        binding.postImageView.setOnClickListener {
            PopupMenu(requireContext(), it).apply {
                setOnMenuItemClickListener(this@PostFragment)
                menuInflater.inflate(R.menu.image_picker_menu, menu)
                setForceShowIcon(true)
                show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val post = args.toPost()
        viewModel.setPost(post)

        observePost()
        setupEditButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (activity as HomeActivity).showBottomNavBar()
        (activity as HomeActivity).showAddApartmentButton()
        (activity as HomeActivity).hideToolbarNavigationIcon()
    }

    private fun observePost() {
        viewModel.post.observe(viewLifecycleOwner) { post ->
            bindPostData(post)
            (activity as HomeActivity).getPostRepository().streamAllExistingEntities()
        }
    }

    private fun bindPostData(post: Post) {
        viewLifecycleOwner.lifecycleScope.launch {
            val address: String? = viewModel.getAddressFromGeo(post)
            address?.let {
                Log.d("Address", it)
            } ?: Log.e("Address", "Address not found")
        }
        binding.apply {
            titleTextView.text = post.title
            contentTextView.text = post.content
            priceTextView.text = "Price: ${post.price}$"
            roomsTextView.text = "Number of Rooms: ${post.rooms}"
            floorTextView.text = "Floor: ${post.floor}"

            post.image?.let {
                Glide.with(this@PostFragment)
                    .load(it)
                    .placeholder(R.drawable.camera_icon)
                    .into(postImageView)
            }

            titleEditText.setText(post.title)
            contentEditText.setText(post.content)
            priceEditText.setText(post.price.toString())
            roomsEditText.setText(post.rooms.toString())
            floorEditText.setText(post.floor.toString())
        }
    }

    private fun setupEditButton() {
        val post = viewModel.post.value

        lifecycleScope.launch {
            val user = authService.currentUser.firstOrNull()
            if (user != null && post != null && user.id == post.userId) {
                binding.editButton.visibility = View.VISIBLE
                binding.editButton.setOnClickListener {
                    toggleEditMode(true)
                }

                binding.saveButton.setOnClickListener {
                    if (validateInput()) {
                        updatePost()
                        toggleEditMode(false)
                    }
                }
            } else {
                binding.editButton.visibility = View.GONE
                binding.saveButton.visibility = View.GONE
            }
        }
    }

    private fun toggleEditMode(editMode: Boolean) {
        isEditMode = editMode

        binding.titleTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.contentTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.priceTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.roomsTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.floorTextView.visibility = if (editMode) View.GONE else View.VISIBLE

        binding.titleEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        binding.contentEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        binding.priceEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        binding.roomsEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        binding.floorEditText.visibility = if (editMode) View.VISIBLE else View.GONE

        binding.editButton.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.addressTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.saveButton.visibility = if (editMode) View.VISIBLE else View.GONE
    }

    private fun updatePost() {
        lifecycleScope.launch {
            val user = authService.currentUser.firstOrNull()
            val post = viewModel.post.value

            if (user != null && post != null && user.id == post.userId) {
                val updatedPost = post.copy(
                    title = binding.titleEditText.text.toString().takeIf { it.isNotEmpty() }
                        ?: post.title,
                    content = binding.contentEditText.text.toString().takeIf { it.isNotEmpty() }
                        ?: post.content,
                    price = binding.priceEditText.text.toString().toDoubleOrNull() ?: post.price,
                    rooms = binding.roomsEditText.text.toString().toIntOrNull() ?: post.rooms,
                    floor = binding.floorEditText.text.toString().toIntOrNull() ?: post.floor,
                    updateTime = Date().time
                )

                val imageBitmap = binding.postImageView.drawable.toBitmap()
                uploadImage(imageBitmap, post.id) { imageUrl ->
                    if (imageUrl != null) {
                        updatedPost.image = imageUrl
                    }
                    savePostUpdates(post.id, updatedPost.toMap())
                }
            }
        }
    }

    private fun uploadImage(image: Bitmap, postId: String, callback: (String?) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("post_images/$postId.jpg")

        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)
        uploadTask
            .addOnFailureListener {
                callback(null)
            }
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener {
                    callback(it.toString())
                }
            }
    }

    private fun savePostUpdates(postId: String, postUpdates: Map<String, Any>) {
        firestore.collection("posts").document(postId).update(postUpdates)
            .addOnSuccessListener {
                context?.let {
                    Toast.makeText(it, "Post updated successfully", Toast.LENGTH_SHORT).show()
                }
                fetchUpdatedPost(postId)
                subscriptionService.listenForCollection(
                    Collections.POSTS,
                    postUpdates["updateTime"] as Long
                )

                // Update the local database
                val updatedPost = postUpdates.toPost()
                lifecycleScope.launch(Dispatchers.IO) {
                    postDao.update(updatedPost)
                }
            }
            .addOnFailureListener { e ->
                context?.let {
                    Toast.makeText(it, "Failed to update post: $e", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchUpdatedPost(postId: String) {
        firestore.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                document?.let {
                    val updatedPost = it.toObject(Post::class.java)
                    updatedPost?.let { post ->
                        viewModel.updatePost(post)
                    }
                }
            }
            .addOnFailureListener { e ->
                context?.let {
                    Toast.makeText(it, "Failed to fetch updated post: $e", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun validateInput(): Boolean {
        return binding.titleEditText.text.isNotEmpty() &&
                binding.contentEditText.text.isNotEmpty() &&
                binding.priceEditText.text.isNotEmpty() &&
                binding.roomsEditText.text.isNotEmpty() &&
                binding.floorEditText.text.isNotEmpty()
    }

    private fun PostFragmentArgs.toPost(): Post {
        return Post(
            id = this.id,
            title = this.title,
            content = this.content,
            price = this.price.toDouble(),
            rooms = this.rooms.toInt(),
            floor = this.floor,
            location = GeoPoint(location[0].toDouble(), location[1].toDouble()),
            image = this.image,
            updateTime = Date().time,
            userId = this.userId ?: ""
        )
    }

    private fun Post.toMap(): Map<String, Any> {
        val postMap = mutableMapOf<String, Any>()
        postMap["title"] = this.title
        postMap["content"] = this.content
        postMap["price"] = this.price
        postMap["rooms"] = this.rooms
        postMap["floor"] = this.floor
        postMap["updateTime"] = this.updateTime
        this.image?.let { postMap["image"] = it }
        return postMap
    }

    private fun Map<String, Any>.toPost(): Post {
        return Post(
            id = this["id"] as? String ?: "",
            title = this["title"] as? String ?: "",
            content = this["content"] as? String ?: "",
            price = this["price"] as? Double ?: 0.0,
            rooms = this["rooms"] as? Int ?: 0,
            floor = this["floor"] as? Int ?: 0,
            location = GeoPoint(0.0, 0.0), // Adjust as needed
            image = this["image"] as? String,
            updateTime = this["updateTime"] as? Long ?: 0L,
            userId = this["userId"] as? String ?: ""
        )
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.camera -> {
                cameraLauncher.launch(null)
                true
            }

            R.id.gallery -> {
                galleryLauncher.launch("image/*")
                true
            }

            else -> false
        }
    }
}