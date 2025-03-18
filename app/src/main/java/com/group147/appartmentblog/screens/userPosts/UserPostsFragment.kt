package com.group147.appartmentblog.screens.userPosts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group147.appartmentblog.databinding.FragmentUserPostsBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.screens.MainActivity
import com.group147.appartmentblog.screens.adapters.PostAdapter

class UserPostsFragment : Fragment() {
    private lateinit var binding: FragmentUserPostsBinding
    private lateinit var userPostsViewModel: UserPostsViewModel
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserPostsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showToolbarNavigationIcon()
        userPostsViewModel = ViewModelProvider(
            requireActivity(),
            UserPostsViewModelFactory(
                (activity as MainActivity).getPostRepository(),
            )
        )[UserPostsViewModel::class.java]

        setupRecyclerView()
        observePosts()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter { post ->
            openPostFragment(post)
        }

        binding.userPostsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun openPostFragment(post: Post) {
        val action = UserPostsFragmentDirections
            .actionFragmentUserPostsFragmentToFragmentPostFragment(
                post.id,
                post.title,
                post.content,
                post.price.toFloat(),
                post.rooms.toFloat(),
                post.floor,
                post.image.toString(),
                floatArrayOf(post.location.latitude.toFloat(), post.location.longitude.toFloat())
            )

        findNavController().navigate(action)
    }

    private fun observePosts() {
        userPostsViewModel.allUserPosts?.observe(viewLifecycleOwner) { posts ->
            posts?.let {
                postAdapter.submitList(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).hideToolbarNavigationIcon()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showToolbarNavigationIcon()
    }
}