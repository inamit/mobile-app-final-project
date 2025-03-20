package com.group147.appartmentblog.screens.feed

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group147.appartmentblog.databinding.FragmentFeedBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.screens.MainActivity
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
        (activity as MainActivity).showAddApartmentButton()

        setupRecyclerView()

        feedViewModel = ViewModelProvider(
            requireActivity(),
            FeedViewModelFactory((activity as MainActivity).getPostRepository())
        )[FeedViewModel::class.java]

        observePosts()
        setupFilters()
    }

    override fun onPause() {
        super.onPause()
        // Reset filters or update ViewModel if needed
        resetFilters()
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
            .actionFragmentFeedFragmentToFragmentPostFragment(post.id)

        findNavController().navigate(action)
    }

    private fun observePosts() {
        feedViewModel.loadingPosts.observe(viewLifecycleOwner) {
            binding.listProgressBar.visibility = if (it) View.VISIBLE else View.GONE
            binding.postsRecyclerView.visibility = if (it) View.GONE else View.VISIBLE
        }
        feedViewModel.allPosts.observe(viewLifecycleOwner) { posts ->
            posts?.let {
                // Log to check the number of posts
                Log.d("FeedFragment", "Number of posts: ${it.size}")
                feedViewModel.filterPosts(binding, it) // Ensure filtering happens when posts change
            }
        }

        feedViewModel.filteredPosts.observe(viewLifecycleOwner) {
            postAdapter.submitList(it)
        }
    }

    fun resetFilters() {
        feedViewModel.resetSlider(
            binding,
            binding.priceRangeSlider,
            0f,
            1000000f,
            "priceRangeSlider",
            binding.priceCubeCancel
        )
        feedViewModel.resetSlider(
            binding,
            binding.roomsRangeSlider,
            1f,
            20f,
            "roomsRangeSlider",
            binding.roomsCubeCancel
        )
        feedViewModel.resetSlider(
            binding,
            binding.floorRangeSlider,
            0f,
            50f,
            "floorRangeSlider",
            binding.floorCubeCancel
        )

        Log.d("FeedViewModel", "Filters reset")
    }

    fun setupFilters() {
        initRanges()
        toggleSlidersGone()
        cubeEvents()
        slidersEvents()
        cancelButtonsEvents()
    }

    private fun initRanges() {
        binding.priceRangeSlider.setValues(0f, 1000000f)
        binding.roomsRangeSlider.setValues(1f, 20f)
        binding.floorRangeSlider.setValues(0f, 50f)
    }

    fun toggleSlidersGone() {
        feedViewModel.toggleSliderVisibility(
            binding.priceRangeSlider,
            binding.priceCubeCancel,
            View.GONE
        )
        feedViewModel.toggleSliderVisibility(
            binding.roomsRangeSlider,
            binding.roomsCubeCancel,
            View.GONE
        )
        feedViewModel.toggleSliderVisibility(
            binding.floorRangeSlider,
            binding.floorCubeCancel,
            View.GONE
        )
    }

    private fun slidersEvents() {
        binding.priceRangeSlider.addOnChangeListener { _, _, _ ->
            feedViewModel.fieldTouchedMap["priceRangeSlider"] = true
            feedViewModel.allPosts.value?.let { feedViewModel.filterPosts(binding, it) }
        }

        binding.roomsRangeSlider.addOnChangeListener { _, _, _ ->
            feedViewModel.fieldTouchedMap["roomsRangeSlider"] = true
            feedViewModel.allPosts.value?.let { feedViewModel.filterPosts(binding, it) }
        }

        binding.floorRangeSlider.addOnChangeListener { _, _, _ ->
            feedViewModel.fieldTouchedMap["floorRangeSlider"] = true
            feedViewModel.allPosts.value?.let { feedViewModel.filterPosts(binding, it) }
        }
    }

    private fun cancelButtonsEvents() {
        binding.priceCubeCancel.setOnClickListener {
            feedViewModel.resetSlider(
                binding,
                binding.priceRangeSlider,
                0f,
                1000000f,
                "priceRangeSlider",
                it as ImageButton
            )
            binding.priceRangeSlider.bringToFront()
        }

        binding.roomsCubeCancel.setOnClickListener {
            feedViewModel.resetSlider(
                binding,
                binding.roomsRangeSlider,
                1f,
                20f,
                "roomsRangeSlider",
                it as ImageButton
            )
            binding.roomsRangeSlider.bringToFront()
        }

        binding.floorCubeCancel.setOnClickListener {
            feedViewModel.resetSlider(
                binding,
                binding.floorRangeSlider,
                0f,
                50f,
                "floorRangeSlider",
                it as ImageButton
            )
            binding.floorRangeSlider.bringToFront()
        }
    }

    private fun cubeEvents() {
        binding.priceCube.setOnClickListener {
            feedViewModel.toggleSliderVisibility(
                binding.priceRangeSlider,
                binding.priceCubeCancel,
                View.VISIBLE
            )
        }

        binding.roomsCube.setOnClickListener {
            feedViewModel.toggleSliderVisibility(
                binding.roomsRangeSlider,
                binding.roomsCubeCancel,
                View.VISIBLE
            )
        }

        binding.floorCube.setOnClickListener {
            feedViewModel.toggleSliderVisibility(
                binding.floorRangeSlider,
                binding.floorCubeCancel,
                View.VISIBLE
            )
        }
    }
}