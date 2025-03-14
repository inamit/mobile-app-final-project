package com.group147.appartmentblog.screens.apartment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group147.appartmentblog.model.Post

class PostViewModel : ViewModel() {

    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    fun setPost(post: Post) {
        _post.value = post
    }

    fun updatePost(updatedPost: Post) {
        _post.value = updatedPost
    }
}