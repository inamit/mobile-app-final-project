package com.group147.appartmentblog.screens.apartment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.GeoPoint
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentPostBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.screens.MainActivity
import com.group147.appartmentblog.screens.adapters.CommentAdapter
import kotlinx.coroutines.launch
import java.util.Date

class PostFragment : Fragment() {

    private lateinit var binding: FragmentPostBinding
    private lateinit var  viewModel: PostViewModel
    private val args: PostFragmentArgs by navArgs()
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        (activity as MainActivity).hideBottomNavBar()
        (activity as MainActivity).hideAddApartmentButton()
        (activity as MainActivity).showToolbarNavigationIcon()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val post = args.toPost()
        viewModel =
            ViewModelProvider(
                requireActivity(),
                PostViewModelFactory(
                    binding,
                   (activity as MainActivity).getCommentRepository()
                )
            )[PostViewModel::class.java]
        observePost()
        viewModel.setPost(post)
        viewModel.setupEditButton()
        viewModel.showAddReviewButton()
        binding.addCommentButton.setOnClickListener {
            val action = PostFragmentDirections
                .actionPostFragmentToAddReviewFragment(
                    post.id,
                )

            findNavController().navigate(action)
        }
        setupRecyclerView()
        observeComments()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).showBottomNavBar()
        (activity as MainActivity).hideToolbarNavigationIcon()

    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter { comment ->

        }
        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentAdapter
        }
    }

    private fun observePost() {
        viewModel.post.observe(viewLifecycleOwner) { post ->
            bindPostData(post)
        }
    }

    private fun bindPostData(post: Post) {
        viewLifecycleOwner.lifecycleScope.launch {
            val apiKey = getString(R.string.google_api_key)
            val address: String? = viewModel.getAddressFromGeo(post, apiKey)
            address?.let {
                binding.addressTextView.text = "Address: ${address}"
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

    private fun observeComments() {
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            comments?.let {
                commentAdapter.submitList(it)
            }
        }
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
            userId = Firebase.auth.currentUser?.uid
        )
    }
}
