package com.group147.appartmentblog.screens.apartment

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentPostBinding
import com.group147.appartmentblog.model.Comment
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.model.User
import com.group147.appartmentblog.repositories.UserRepository
import com.group147.appartmentblog.screens.MainActivity
import com.group147.appartmentblog.screens.adapters.CommentAdapter
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class PostFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: FragmentPostBinding
    private lateinit var viewModel: PostViewModel
    private val args: PostFragmentArgs by navArgs()

    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)

        (activity as MainActivity).hideBottomNavBar()
        (activity as MainActivity).hideAddApartmentButton()
        (activity as MainActivity).showToolbarNavigationIcon()

        userRepository = (activity as MainActivity).getUserRepository()

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val postId = args.id

        viewModel = ViewModelProvider(
            requireActivity(),
            PostViewModelFactory(
                (activity as MainActivity).getPostRepository(),
                (activity as MainActivity).getCommentRepository()
            )
        )[PostViewModel::class.java]
        viewModel.setupEditButton(binding)
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
        binding.addCommentButton.setOnClickListener {
            val action = PostFragmentDirections
                .actionPostFragmentToAddReviewFragment(postId)
            findNavController().navigate(action)
        }

        observePost()
        observeUser()
        setupRecyclerView()
        observeComments(postId)
    }

    override fun onResume() {
        super.onResume()

        (activity as MainActivity).hideBottomNavBar()
        (activity as MainActivity).hideAddApartmentButton()
        (activity as MainActivity).showToolbarNavigationIcon()

    }

    override fun onDestroyView() {
        super.onDestroyView()

        (activity as MainActivity).showAddApartmentButton()
        (activity as MainActivity).hideToolbarNavigationIcon()
        (activity as MainActivity).hideToolbarMenu()
    }

    private fun observePost() {
        viewModel.allPosts.observe(viewLifecycleOwner) { allPosts ->
            val post = allPosts.find { it.id == args.id }

            if (post != null) {
                viewModel.setPost(post)
            } else {
                findNavController().popBackStack()
            }
        }
        viewModel.post.observe(viewLifecycleOwner) { post ->
            bindPostData(post)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                (activity as MainActivity).showLoadingOverlay()
            } else {
                (activity as MainActivity).hideLoadingOverlay()
            }
        }
    }

    private fun observeUser() {
        userRepository.userLiveData.observe(viewLifecycleOwner) { user ->
            showMenu(user)
            setupEditButton(user)
        }
    }

    private fun bindPostData(post: Post) {
        viewLifecycleOwner.lifecycleScope.launch {
            val apiKey = getString(R.string.google_api_key)
            val address: String? = viewModel.getAddressFromGeo(post, apiKey)
            address?.let {
                binding.addressTextView.text = "Address: $address"
            } ?: Log.e("Address", "Address not found")
        }
        binding.apply {
            titleTextView.text = post.title
            contentTextView.text = post.content
            priceTextView.text = "Price: ${post.price}$"
            roomsTextView.text = "Number of Rooms: ${post.rooms}"
            floorTextView.text = "Floor: ${post.floor}"

            post.image?.let {
                Picasso
                    .get()
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

    private fun showMenu(user: User?) {
        val post = viewModel.post.value

        if (user != null && post != null && user.id == post.userId) {
            (activity as MainActivity).showToolbarMenu(R.menu.post_toolbar_menu) {
                when (it.itemId) {
                    R.id.delete_post -> {
                        viewModel.deletePost(post)

                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun setupEditButton(user: User?) {
        val post = viewModel.post.value

        if (user != null && post != null && user.id == post.userId) {

            binding.editButton.visibility = View.VISIBLE
            binding.editButton.setOnClickListener {
                binding.postImageView.setOnClickListener {
                    PopupMenu(requireContext(), it).apply {
                        setOnMenuItemClickListener(this@PostFragment)
                        menuInflater.inflate(R.menu.image_picker_menu, menu)
                        show()
                    }
                }
                viewModel.toggleEditMode(binding, true)
            }

            binding.saveButton.setOnClickListener {
                if (viewModel.validateInput(binding)) {
                    val updatedPost = post.copy(
                        title = binding.titleEditText.text.toString(),
                        content = binding.contentEditText.text.toString(),
                        price = binding.priceEditText.text.toString().toDouble(),
                        rooms = binding.roomsEditText.text.toString().toInt(),
                        floor = binding.floorEditText.text.toString().toInt(),
                        updateTime = System.currentTimeMillis()
                    )
                    val imageBitmap = binding.postImageView.drawable.toBitmap()
                    viewModel.updatePost(updatedPost, imageBitmap)
                    binding.postImageView.setOnClickListener { }
                    viewModel.toggleEditMode(binding, false)
                }
            }
        } else {
            binding.editButton.visibility = View.GONE
            binding.saveButton.visibility = View.GONE
        }
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

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter {}
        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentAdapter
        }
    }

    private fun observeComments(postId: String) {
        viewModel.comments.observe(viewLifecycleOwner) { comments: List<Comment>? ->
            comments?.let {
                val filteredComments = it.filter { comment: Comment -> comment.postId == postId }
                commentAdapter.submitList(filteredComments)
            }
        }
    }
}