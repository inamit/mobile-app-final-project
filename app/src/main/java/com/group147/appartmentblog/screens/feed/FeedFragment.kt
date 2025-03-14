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
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.screens.adapters.PostAdapter
import com.group147.appartmentblog.screens.home.HomeActivity
import com.group147.appartmentblog.R

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
            FeedViewModelFactory((activity as HomeActivity).getPostRepository())
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
        val bundle = Bundle().apply {
            putString("id", post.id)
            putString("userId", post.userId)
            putString("title", post.title)
            putString("content", post.content)
            putDouble("price", post.price)
            putInt("rooms", post.rooms)
            putInt("floor", post.floor)
            putString("image", post.image)
        }

        findNavController().navigate(R.id.postFragment, bundle)
    }

    private fun observePosts() {
        feedViewModel.allPosts.observe(viewLifecycleOwner) { posts ->
            posts?.let {
                postAdapter.submitList(it)
            }
        }
    }
}