package com.group147.appartmentblog.screens.feed

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.slider.RangeSlider
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentFeedBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.screens.adapters.PostAdapter
import com.group147.appartmentblog.screens.home.HomeActivity

class FeedFragment : Fragment() {
    private lateinit var binding: FragmentFeedBinding
    private lateinit var feedViewModel: FeedViewModel
    private lateinit var postAdapter: PostAdapter

    private var isPriceTouched = false
    private var isRoomsTouched = false
    private var isFloorTouched = false

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
        setupFilters()
        binding.resetFiltersButton.setOnClickListener {
            resetFiltersToDefaults()
        }
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
        feedViewModel.allPosts.observe(viewLifecycleOwner) { posts ->
            posts?.let {
                // Log to check the number of posts
                Log.d("FeedFragment", "Number of posts: ${it.size}")
                filterPosts(it) // Ensure filtering happens when posts change
            }
        }
    }

    private fun setupFilters() {
        // Set default values for the sliders (optional)
        binding.priceRangeSlider.setValues(0f, 0f)
        binding.roomsRangeSlider.setValues(1f, 1f)
        binding.floorRangeSlider.setValues(0f, 0f)

        binding.priceRangeSlider.addOnChangeListener { _, _, _ ->
            feedViewModel.allPosts.value?.let { posts ->
                isPriceTouched = true
                filterPosts(posts) // Reapply filter when sliders change
            }
        }

        binding.roomsRangeSlider.addOnChangeListener { _, _, _ ->
            isRoomsTouched = true
            feedViewModel.allPosts.value?.let { posts ->
                filterPosts(posts) // Reapply filter when sliders change
            }
        }

        binding.floorRangeSlider.addOnChangeListener { _, _, _ ->
            isFloorTouched = true
            feedViewModel.allPosts.value?.let { posts ->
                filterPosts(posts) // Reapply filter when sliders change
            }
        }
    }

    private fun filterPosts(posts: List<Post>) {
        val priceRange = getSliderRange(binding.priceRangeSlider)
        val roomsRange = getSliderRange(binding.roomsRangeSlider)
        val floorRange = getSliderRange(binding.floorRangeSlider)

        // Log the filter ranges for debugging
        Log.d("FeedFragment", "Price Range: $priceRange")
        Log.d("FeedFragment", "Rooms Range: $roomsRange")
        Log.d("FeedFragment", "Floor Range: $floorRange")

        // If no filter has been touched, show all posts
        val filteredPosts = posts.filter { post ->
            val isPriceValid = if (isPriceTouched) {
                // Apply price filter if touched, else ignore
                post.price in priceRange.first..priceRange.second
            } else {
                true  // No price filter applied
            }

            val isRoomsValid = if (isRoomsTouched) {
                // Apply rooms filter if touched, else ignore
                post.rooms in roomsRange.first.toInt()..roomsRange.second.toInt()
            } else {
                true  // No rooms filter applied
            }

            val isFloorValid = if (isFloorTouched) {
                // Apply floor filter if touched, else ignore
                post.floor in floorRange.first.toInt()..floorRange.second.toInt()
            } else {
                true  // No floor filter applied
            }

            isPriceValid && isRoomsValid && isFloorValid
        }

        Log.d("FeedFragment", "Number of posts after filtering: ${filteredPosts.size}")
        postAdapter.submitList(filteredPosts)  // Update the RecyclerView with filtered posts
    }

    private fun getSliderRange(slider: RangeSlider): Pair<Float, Float> {
        return if (slider.values.size >= 2) {
            slider.values[0] to slider.values[1]
        } else {
            slider.valueFrom to slider.valueTo
        }
    }

    private fun resetFiltersToDefaults() {
        // Reset the sliders to their default values
        binding.priceRangeSlider.values = listOf(0f, 0f)
        binding.roomsRangeSlider.values = listOf(1f, 1f)
        binding.floorRangeSlider.values = listOf(0f, 0f)

        // Reset the "touched" flags to false
        isPriceTouched = false
        isRoomsTouched = false
        isFloorTouched = false

        feedViewModel.allPosts.value?.let { posts ->
            filterPosts(posts)
        }

    }
}