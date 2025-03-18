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

class FeedViewModel(
    private val binding: FragmentFeedBinding,
    private val postAdapter: PostAdapter,
    postRepository: PostRepository
) : ViewModel() {

    val allPosts: LiveData<List<Post>> = postRepository.postsLiveData
    var fieldTouchedMap: MutableMap<String, Boolean> = mutableMapOf(
        "priceRangeSlider" to false,
        "roomsRangeSlider" to false,
        "floorRangeSlider" to false
    )

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

    fun toggleSliderVisibility(slider: RangeSlider, cancelButton: ImageButton, visibility: Int) {
        slider.visibility = visibility
        cancelButton.visibility = visibility
    }

    fun resetSlider(slider: RangeSlider, from: Float, to: Float, key: String, button: ImageButton) {
        slider.setValues(from, to)
        fieldTouchedMap[key] = false
        toggleSliderVisibility(slider, button, View.GONE)
        allPosts.value?.let { filterPosts(it) }
    }

    private fun getSliderRange(slider: RangeSlider): Pair<Float, Float> {
        return if (slider.values.size >= 2) {
            slider.values[0] to slider.values[1]
        } else {
            slider.valueFrom to slider.valueTo
        }
    }
}
