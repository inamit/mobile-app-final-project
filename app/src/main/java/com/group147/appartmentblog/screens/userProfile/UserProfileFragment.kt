package com.group147.appartmentblog.screens.userProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentUserProfileBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.screens.MainActivity
import com.group147.appartmentblog.screens.adapters.PostAdapter
import com.squareup.picasso.Picasso

class UserProfileFragment : Fragment() {
    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var userPostsViewModel: UserProfileViewModel
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showProfileToolbarMenu {
            when (it.itemId) {
                R.id.logout -> {
                    onLogoutClicked()
                    true
                }

                else -> false
            }
        }
        userPostsViewModel = ViewModelProvider(
            requireActivity(),
            UserProfileViewModelFactory(
                (activity as MainActivity).getPostRepository(),
                (activity as MainActivity).getUserRepository(),
            )
        )[UserProfileViewModel::class.java]
        binding.editIcon.setOnClickListener {
            findNavController().navigate(R.id.action_userProfileFragment_to_userEditFragment)
        }
        setupRecyclerView()
        observePosts()
        loadUserProfile()
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
        val action = UserProfileFragmentDirections
            .actionFragmentUserProfileFragmentToFragmentPostFragment(
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

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).hideToolbarMenu()
    }


    private fun loadUserProfile() {
        userPostsViewModel.user.observe(viewLifecycleOwner) { user ->
            binding.emailText.text = user?.email

            if (user?.imageUrl.isNullOrEmpty()) {
                binding.profileImage.setImageResource(R.drawable.ic_user_placeholder)
            } else {
                Picasso.get().load(user.imageUrl).into(binding.profileImage)
            }
        }
    }
    private fun onLogoutClicked() {
        userPostsViewModel.signOut()
        findNavController().navigate(R.id.action_userProfileFragment_to_loginFragment)
    }
}