package com.group147.appartmentblog.screens.feed

import android.util.Log
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
) :
    ViewModel() {
    val allPosts: LiveData<List<Post>> = postRepository.postsLiveData

    private var fieldTouchedMap: MutableMap<String, Boolean> = mutableMapOf(
        "priceRangeSlider" to false,
        "roomsRangeSlider" to false,
        "floorRangeSlider" to false
    )

    fun setupFilters() {
        // Set default values for the sliders (optional)
        initRanges()

        binding.priceRangeSlider.addOnChangeListener { _, _, _ ->
            fieldTouchedMap["priceRangeSlider"] = true
            allPosts.value?.let { posts ->
                filterPosts(posts) // Reapply filter when sliders change
            }
        }

        binding.roomsRangeSlider.addOnChangeListener { _, _, _ ->
            fieldTouchedMap["roomsRangeSlider"] = true
            allPosts.value?.let { posts ->
                filterPosts(posts) // Reapply filter when sliders change
            }
        }

        binding.floorRangeSlider.addOnChangeListener { _, _, _ ->
            fieldTouchedMap["floorRangeSlider"] = true
            allPosts.value?.let { posts ->
                filterPosts(posts) // Reapply filter when sliders change
            }
        }
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

        allPosts.value?.let { posts ->
            filterPosts(posts)
        }
    }

    private fun initRanges() {
        binding.priceRangeSlider.setValues(0f, 0f)
        binding.roomsRangeSlider.setValues(1f, 1f)
        binding.floorRangeSlider.setValues(0f, 0f)
    }
}