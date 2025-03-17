package com.group147.appartmentblog.screens.apartment

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group147.appartmentblog.databinding.FragmentPostBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.util.geoToAddress.getGoogleAddressFromLatLng
import androidx.core.graphics.drawable.toBitmap
import android.view.View

class PostViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    fun setPost(post: Post) {
        _post.value = post
    }

    suspend fun getAddressFromGeo(post: Post, apiKey: String): String? {
        return getGoogleAddressFromLatLng(post.location.latitude, post.location.longitude, apiKey)
    }

    fun updatePost(post: Post, image: Bitmap?) {
        postRepository.updatePost(post, image) { _, error ->
            if (error != null) {
                _toastMessage.postValue("Failed to update post")
            } else {
                _toastMessage.postValue("Post updated successfully")
            }
        }
    }

    fun setupEditButton(binding: FragmentPostBinding) {
        binding.editButton.setOnClickListener {
            toggleEditMode(binding, true)
        }

        binding.saveButton.setOnClickListener {
            if (validateInput(binding)) {
                val updatedPost = post.value?.copy(
                    title = binding.titleEditText.text.toString(),
                    content = binding.contentEditText.text.toString(),
                    price = binding.priceEditText.text.toString().toDouble(),
                    rooms = binding.roomsEditText.text.toString().toInt(),
                    floor = binding.floorEditText.text.toString().toInt(),
                    updateTime = System.currentTimeMillis()
                )
                val imageBitmap = binding.postImageView.drawable.toBitmap()
                updatedPost?.let {
                    updatePost(it, imageBitmap)
                }
                toggleEditMode(binding, false)
            }
        }
    }

    fun toggleEditMode(binding: FragmentPostBinding, editMode: Boolean) {
        binding.apply {
            titleTextView.visibility = if (editMode) View.GONE else View.VISIBLE
            contentTextView.visibility = if (editMode) View.GONE else View.VISIBLE
            priceTextView.visibility = if (editMode) View.GONE else View.VISIBLE
            roomsTextView.visibility = if (editMode) View.GONE else View.VISIBLE
            floorTextView.visibility = if (editMode) View.GONE else View.VISIBLE

            titleEditText.visibility = if (editMode) View.VISIBLE else View.GONE
            contentEditText.visibility = if (editMode) View.VISIBLE else View.GONE
            priceEditText.visibility = if (editMode) View.VISIBLE else View.GONE
            roomsEditText.visibility = if (editMode) View.VISIBLE else View.GONE
            floorEditText.visibility = if (editMode) View.VISIBLE else View.GONE

            editButton.visibility = if (editMode) View.GONE else View.VISIBLE
            addressTextView.visibility = if (editMode) View.GONE else View.VISIBLE
            saveButton.visibility = if (editMode) View.VISIBLE else View.GONE
        }
    }

    fun validateInput(binding: FragmentPostBinding): Boolean {
        return binding.titleEditText.text.isNotEmpty() &&
                binding.contentEditText.text.isNotEmpty() &&
                binding.priceEditText.text.isNotEmpty() &&
                binding.roomsEditText.text.isNotEmpty() &&
                binding.floorEditText.text.isNotEmpty()
    }
}