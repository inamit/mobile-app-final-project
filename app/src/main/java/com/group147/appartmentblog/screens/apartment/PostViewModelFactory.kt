package com.group147.appartmentblog.screens.apartment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group147.appartmentblog.repositories.CommentRepository
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.screens.MainViewModel

class PostViewModelFactory(
    private val mainViewModel: MainViewModel,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(mainViewModel, postRepository, commentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}