package com.group147.appartmentblog.screens.addApartment

import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.GeoPoint
import com.group147.appartmentblog.databinding.FragmentAddApartmentBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.repositories.PostRepository

class AddApartmentViewModel(
    private val binding: FragmentAddApartmentBinding,
    private val postRepository: PostRepository
) : ViewModel() {
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: MutableLiveData<String>
        get() = _toastMessage

    fun savePost(location: GeoPoint?, callback: (String?) -> Unit) {
        if (location == null) {
            _toastMessage.postValue("Location is required to upload a post")
            return
        }

        if (!validateForm()) {
            return
        }

        val post = Post(
            userId = Firebase.auth.currentUser?.uid,
            title = binding.titleEditText.text.toString(),
            content = binding.contentEditText.text.toString(),
            floor = binding.floorEditText.text.toString().toInt(),
            rooms = binding.roomsEditText.text.toString().toInt(),
            price = binding.priceEditText.text.toString().toDouble(),
            location = location
        )
        val image = binding.imagePreview.drawable.toBitmap()

        postRepository.insertPost(post, image) { document, error ->
            if (error != null) {
                _toastMessage.postValue("Failed post apartment. Please try again.")
            } else {
                callback(document)
                _toastMessage.postValue("Apartment posted successfully")
            }
        }
    }

    fun validateForm(): Boolean {
        var valid = true

        val requiredEditTexts = listOf(
            Pair(binding.titleEditText, "Title is required"),
            Pair(binding.floorEditText, "Floor is required"),
            Pair(binding.roomsEditText, "Rooms is required"),
            Pair(binding.priceEditText, "Price is required"),
            Pair(binding.contentEditText, "Content is required"),
        )

        for ((editText, errorMessage) in requiredEditTexts) {
            if (editText.text.isEmpty()) {
                editText.error = errorMessage
                valid = false
            }
        }

        val image = binding.imagePreview.drawable

        if (image == null) {
            binding.pickImage.error = "Image is required"
            valid = false
        }

        return valid
    }
}