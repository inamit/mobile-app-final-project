package com.group147.appartmentblog.screens.addReview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group147.appartmentblog.databinding.FragmentAddReviewBinding
import com.group147.appartmentblog.repositories.CommentRepository
import com.group147.appartmentblog.repositories.UserRepository

class AddReviewViewModelFactory(
    private val binding: FragmentAddReviewBinding,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddReviewViewModel(binding, commentRepository,userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}