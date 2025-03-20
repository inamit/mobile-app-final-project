package com.group147.appartmentblog.screens.addReview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group147.appartmentblog.databinding.FragmentAddReviewBinding
import com.group147.appartmentblog.model.Comment
import com.group147.appartmentblog.model.User
import com.group147.appartmentblog.repositories.CommentRepository
import com.group147.appartmentblog.repositories.UserRepository

class AddReviewViewModel(
    private val binding: FragmentAddReviewBinding,
    private val commentRepository: CommentRepository,
    userRepository: UserRepository
) : ViewModel() {
    val user: LiveData<User> = userRepository.userLiveData
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: MutableLiveData<String>
        get() = _toastMessage

    fun saveComment(comment: Comment, callback: (String?) -> Unit) {
        if (!validateForm()) {
            return
        }
        commentRepository.insertComment(comment) { document, error ->
            if (error != null) {
                _toastMessage.postValue("Failed adding review. Please try again.")
            } else {
                callback(document)
                _toastMessage.postValue("Review posted successfully")
            }
        }
    }

    fun validateForm(): Boolean {
        var valid = true

        if (binding.reviewEditText.text.isEmpty()) {
            valid = false
            _toastMessage.postValue("Review is required. Please try again.")
        }

        return valid
    }
}