package com.group147.appartmentblog.screens.userPosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group147.appartmentblog.repositories.PostRepository

class UserPostsViewModelFactory (private val postRepository: PostRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserPostsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserPostsViewModel(postRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}