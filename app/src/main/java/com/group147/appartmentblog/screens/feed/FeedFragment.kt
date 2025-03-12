package com.group147.appartmentblog.screens.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.group147.appartmentblog.database.post.PostDatabase
import com.group147.appartmentblog.databinding.FragmentFeedBinding
import com.group147.appartmentblog.model.FirebaseModel
import com.group147.appartmentblog.repositories.PostRepository
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

        val firebaseModel = FirebaseModel()
        val database = PostDatabase.getDatabase(requireContext())
        val postDao = database.postDao()
        val postRepository = PostRepository(firebaseModel, postDao)
        feedViewModel = ViewModelProvider(
            requireActivity(),
            FeedViewModelFactory(postRepository)
        )[FeedViewModel::class.java]

        setupRecyclerView()
        observePosts()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        binding.postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun observePosts() {
        feedViewModel.allPosts.observe(viewLifecycleOwner) { posts ->
            posts?.let {
                postAdapter.submitList(it)
            }
        }
    }
}