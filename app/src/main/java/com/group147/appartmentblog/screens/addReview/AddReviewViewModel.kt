package com.group147.appartmentblog.screens.addReview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group147.appartmentblog.databinding.FragmentAddReviewBinding
import com.group147.appartmentblog.model.Comment
import com.group147.appartmentblog.repositories.CommentRepository

class AddReviewViewModel(
    private val binding: FragmentAddReviewBinding,
    private val commentRepository: CommentRepository
) : ViewModel() {
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: MutableLiveData<String>
        get() = _toastMessage

    fun saveComment(comment:Comment,callback: (String?) -> Unit) {
        if (!validateForm()) {
            return
        }

        commentRepository.insertComment(comment) { document, error ->
            if (error != null) {
                _toastMessage.postValue("Failed adding review. Please try again.")
            } else {
                callback(document)
                _toastMessage.postValue("Apartment posted successfully")
            }
        }
    }

    fun validateForm(): Boolean {
        var valid = true

        val requiredEditTexts = listOf(
            Pair(binding.reviewEditText, "Review is required"),
            Pair(binding.ratingBar.rating, "rating is required"),
        )


        return valid
    }
}