package com.group147.appartmentblog.screens.addReview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group147.appartmentblog.databinding.FragmentAddReviewBinding
import com.group147.appartmentblog.repositories.CommentRepository

class AddReviewViewModelFactory(
    private val binding: FragmentAddReviewBinding,
    private val commentRepository: CommentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddReviewViewModel(binding, commentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}