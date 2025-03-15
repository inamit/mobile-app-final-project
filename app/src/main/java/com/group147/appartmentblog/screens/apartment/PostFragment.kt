package com.group147.appartmentblog.screens.apartment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.firestore.GeoPoint
import com.group147.appartmentblog.databinding.FragmentPostBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.R
import com.group147.appartmentblog.screens.home.HomeActivity
import kotlinx.coroutines.launch
import java.util.Date

class PostFragment : Fragment() {

    private lateinit var binding: FragmentPostBinding
    private val viewModel: PostViewModel by viewModels()
    private val args: PostFragmentArgs by navArgs()

    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        (activity as HomeActivity).hideBottomNavBar()
        (activity as HomeActivity).hideAddApartmentButton()
        (activity as HomeActivity).showToolbarNavigationIcon()

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
        }
    }

    private fun bindPostData(post: Post) {
        viewLifecycleOwner.lifecycleScope.launch {
            val address: String? = viewModel.getAddressFromGeo(post)
            address?.let {
               // addressTextView.text = "Adress: ${address}"
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
        binding.editButton.setOnClickListener {
            toggleEditMode(true)
        }

        binding.saveButton.setOnClickListener {
            if (validateInput()) {
                updatePost()
                toggleEditMode(false)
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
        val updatedPost = viewModel.post.value?.copy(
            title = binding.titleEditText.text.toString(),
            content = binding.contentEditText.text.toString(),
            price = binding.priceEditText.text.toString().toDoubleOrNull() ?: 0.0,
            rooms = binding.roomsEditText.text.toString().toIntOrNull() ?: 0,
            floor = binding.floorEditText.text.toString().toIntOrNull() ?: 0,
            updateTime = Date().time
        )

        updatedPost?.let {
            viewModel.updatePost(it)
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
            userId = this.id
        )
    }
}
