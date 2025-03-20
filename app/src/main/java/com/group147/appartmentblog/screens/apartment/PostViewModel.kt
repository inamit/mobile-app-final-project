package com.group147.appartmentblog.screens.apartment

import android.graphics.Bitmap
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group147.appartmentblog.databinding.FragmentPostBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.util.geoToAddress.getGoogleAddressFromLatLng
import kotlinx.coroutines.launch

class PostViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

    val allPosts = postRepository.postsLiveData

    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun setPost(post: Post) {
        _post.value = post
    }

    suspend fun getAddressFromGeo(post: Post, apiKey: String): String? {
        return getGoogleAddressFromLatLng(post.location.latitude, post.location.longitude, apiKey)
    }

    fun updatePost(post: Post, image: Bitmap?) {
        _isLoading.postValue(true)
        postRepository.updatePost(post, image) { _, error ->
            if (error != null) {
                _toastMessage.postValue("Failed to update post")
            } else {
                _toastMessage.postValue("Post updated successfully")
            }
            _isLoading.postValue(false)
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                postRepository.deletePost(post)
            } catch (e: Exception) {
                _toastMessage.postValue("Failed to delete post")
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