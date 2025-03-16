package com.group147.appartmentblog.screens.apartment

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group147.appartmentblog.databinding.FragmentPostBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.util.geoToAdress.getGoogleAddressFromLatLng
import java.util.Date

class PostViewModel(private val binding: FragmentPostBinding) : ViewModel() {

    private var isEditMode = false
    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    fun setPost(post: Post) {
        _post.value = post
    }

    fun updatePost(updatedPost: Post) {
        _post.value = updatedPost
    }

    suspend fun getAddressFromGeo(post: Post, apiKey: String): String? {
        var address = getGoogleAddressFromLatLng(
            post.location.latitude, post.location.longitude,
            apiKey
        )

        return address
    }


    fun setupEditButton() {
        binding.editButton.setOnClickListener {
            toggleEditMode(true)
        }

        binding.saveButton.setOnClickListener {
            if (validateInput()) {
                updatePost()
                toggleEditMode(false)
            }
        }
    }

    private fun toggleEditMode(editMode: Boolean) {
        isEditMode = editMode

        binding.titleTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.contentTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.priceTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.roomsTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.floorTextView.visibility = if (editMode) View.GONE else View.VISIBLE

        binding.titleEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        binding.contentEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        binding.priceEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        binding.roomsEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        binding.floorEditText.visibility = if (editMode) View.VISIBLE else View.GONE

        binding.editButton.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.addressTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.saveButton.visibility = if (editMode) View.VISIBLE else View.GONE
    }

    private fun updatePost() {
        val updatedPost = post.value?.copy(
            title = binding.titleEditText.text.toString(),
            content = binding.contentEditText.text.toString(),
            price = binding.priceEditText.text.toString().toDoubleOrNull() ?: 0.0,
            rooms = binding.roomsEditText.text.toString().toIntOrNull() ?: 0,
            floor = binding.floorEditText.text.toString().toIntOrNull() ?: 0,
            updateTime = Date().time
        )

        updatedPost?.let {
            updatePost(it)
        }
    }

    private fun validateInput(): Boolean {
        return binding.titleEditText.text.isNotEmpty() &&
                binding.contentEditText.text.isNotEmpty() &&
                binding.priceEditText.text.isNotEmpty() &&
                binding.roomsEditText.text.isNotEmpty() &&
                binding.floorEditText.text.isNotEmpty()
    }

}