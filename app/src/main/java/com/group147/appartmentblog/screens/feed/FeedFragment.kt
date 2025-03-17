package com.group147.appartmentblog.screens.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group147.appartmentblog.databinding.FragmentFeedBinding
import com.group147.appartmentblog.screens.MainActivity
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.screens.adapters.PostAdapter

class FeedFragment : Fragment() {
    private lateinit var binding: FragmentFeedBinding
    private lateinit var feedViewModel: FeedViewModel
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeedBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        feedViewModel = ViewModelProvider(
            requireActivity(),
            FeedViewModelFactory((activity as MainActivity).getPostRepository())
        )[FeedViewModel::class.java]

        setupRecyclerView()
        observePosts()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter { post ->
            openPostFragment(post)
        }

        binding.postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }
    private fun openPostFragment(post: Post) {
        val action = FeedFragmentDirections
            .actionFragmentFeedFragmentToFragmentPostFragment(
                post.id,
                post.title,
                post.content,
                post.price.toFloat(),
                post.rooms.toFloat(),
                post.floor,
                post.image.toString(),
                floatArrayOf( post.location.latitude.toFloat(),post.location.longitude.toFloat())
            )

        findNavController().navigate(action)
    }

    private fun observePosts() {
        feedViewModel.loadingPosts.observe(viewLifecycleOwner) {
            binding.listProgressBar.visibility = if (it) View.VISIBLE else View.GONE
            binding.postsRecyclerView.visibility = if (it) View.GONE else View.VISIBLE
        }
        feedViewModel.allPosts.observe(viewLifecycleOwner) { posts ->
            posts?.let {
                postAdapter.submitList(it)
            }
        }
    }
}