package com.group147.appartmentblog.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group147.appartmentblog.databinding.FragmentFeedBinding
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.screens.adapters.PostAdapter

class FeedViewModelFactory(
    private val binding: FragmentFeedBinding,
    private val postAdapter: PostAdapter,
    private val postRepository: PostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FeedViewModel(binding, postAdapter, postRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}