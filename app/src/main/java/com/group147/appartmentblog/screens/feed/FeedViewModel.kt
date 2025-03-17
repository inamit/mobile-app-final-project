package com.group147.appartmentblog.screens.feed

import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.slider.RangeSlider
import com.group147.appartmentblog.databinding.FragmentFeedBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.screens.adapters.PostAdapter
import kotlin.collections.set

class FeedViewModel(
    private val binding: FragmentFeedBinding,
    private val postAdapter: PostAdapter,
    postRepository: PostRepository
) : ViewModel() {

    val allPosts: LiveData<List<Post>> = postRepository.postsLiveData

    private var fieldTouchedMap: MutableMap<String, Boolean> = mutableMapOf(
        "priceRangeSlider" to false,
        "roomsRangeSlider" to false,
        "floorRangeSlider" to false
    )

    fun setupFilters() {
        initRanges()
        // Initialize sliders visibility and button click listeners
        toggleSliderVisibility(binding.priceRangeSlider, binding.priceCubeCancel, View.GONE)
        toggleSliderVisibility(binding.roomsRangeSlider, binding.roomsCubeCancel, View.GONE)
        toggleSliderVisibility(binding.floorRangeSlider, binding.floorCubeCancel, View.GONE)

        // Toggle sliders visibility when clicking on the cubes
        binding.priceCube.setOnClickListener {
            toggleSliderVisibility(binding.priceRangeSlider, binding.priceCubeCancel, View.VISIBLE)
        }

        binding.roomsCube.setOnClickListener {
            toggleSliderVisibility(binding.roomsRangeSlider, binding.roomsCubeCancel, View.VISIBLE)
        }

        binding.floorCube.setOnClickListener {
            toggleSliderVisibility(binding.floorRangeSlider, binding.floorCubeCancel, View.VISIBLE)
        }

        sliderEvents()
        cancelButtonsEvents()
    }

    // Helper function to show/hide sliders and cancel buttons
    private fun toggleSliderVisibility(slider: RangeSlider, cancelButton: ImageButton, visibility: Int) {
        slider.visibility = visibility
        cancelButton.visibility = visibility

        if (visibility == View.VISIBLE) {
            slider.animate()
                .translationY(100f)  // Slide it down or up
                .alpha(1f)  // Fade in
                .setDuration(300)  // Duration of the animation
                .start()
        } else {
            slider.animate()
                .translationY(0f)
                .alpha(0f)
                .setDuration(300)
                .start()
        }
    }

    private fun resetSlider(slider: RangeSlider, from: Float, to: Float, key: String, button: ImageButton) {
        slider.setValues(from, to)
        fieldTouchedMap[key] = false
        button.visibility = View.GONE
        slider.bringToFront()
        allPosts.value?.let { filterPosts(it) }
    }

    fun filterPosts(posts: List<Post>) {
        val priceRange = getSliderRange(binding.priceRangeSlider)
        val roomsRange = getSliderRange(binding.roomsRangeSlider)
        val floorRange = getSliderRange(binding.floorRangeSlider)

        val filteredPosts = posts.filter { post ->
            val isPriceValid = if (fieldTouchedMap["priceRangeSlider"] == true) {
                // Apply price filter if touched, else ignore
                post.price in priceRange.first..priceRange.second
            } else {
                true  // No price filter applied
            }

            val isRoomsValid = if (fieldTouchedMap["roomsRangeSlider"] == true) {
                // Apply rooms filter if touched, else ignore
                post.rooms in roomsRange.first.toInt()..roomsRange.second.toInt()
            } else {
                true  // No rooms filter applied
            }

            val isFloorValid = if (fieldTouchedMap["floorRangeSlider"] == true) {
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

    fun getSliderRange(slider: RangeSlider): Pair<Float, Float> {
        return if (slider.values.size >= 2) {
            slider.values[0] to slider.values[1]
        } else {
            slider.valueFrom to slider.valueTo
        }
    }

    fun resetFiltersToDefaults() {
        initRanges()

        fieldTouchedMap.keys.forEach { key ->
            fieldTouchedMap[key] = false
        }

        // Hide all cancel buttons
        binding.priceCubeCancel.visibility = View.GONE
        binding.roomsCubeCancel.visibility = View.GONE
        binding.floorCubeCancel.visibility = View.GONE

        allPosts.value?.let { filterPosts(it) }
    }

    private fun initRanges() {
        binding.priceRangeSlider.setValues(0f, 1000000f)
        binding.roomsRangeSlider.setValues(1f, 20f)
        binding.floorRangeSlider.setValues(0f, 50f)
    }

    private fun sliderEvents() {
        binding.priceRangeSlider.addOnChangeListener { _, _, _ ->
            fieldTouchedMap["priceRangeSlider"] = true
            allPosts.value?.let { filterPosts(it) }
        }

        binding.roomsRangeSlider.addOnChangeListener { _, _, _ ->
            fieldTouchedMap["roomsRangeSlider"] = true
            allPosts.value?.let { filterPosts(it) }
        }

        binding.floorRangeSlider.addOnChangeListener { _, _, _ ->
            fieldTouchedMap["floorRangeSlider"] = true
            allPosts.value?.let { filterPosts(it) }
        }
    }

    private fun cancelButtonsEvents() {
        binding.priceCubeCancel.setOnClickListener {
            resetSlider(binding.priceRangeSlider, 0f, 1000000f, "priceRangeSlider", it as ImageButton)
            binding.priceRangeSlider.bringToFront()
        }

        binding.roomsCubeCancel.setOnClickListener {
            resetSlider(binding.roomsRangeSlider, 1f, 20f, "roomsRangeSlider", it as ImageButton)
            binding.roomsRangeSlider.bringToFront()
        }

        binding.floorCubeCancel.setOnClickListener {
            resetSlider(binding.floorRangeSlider, 0f, 50f, "floorRangeSlider", it as ImageButton)
            binding.floorRangeSlider.bringToFront()
        }
    }
}
