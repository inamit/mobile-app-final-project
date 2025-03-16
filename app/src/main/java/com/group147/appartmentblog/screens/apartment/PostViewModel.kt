package com.group147.appartmentblog.screens.apartment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.util.geoToAdress.getGoogleAddressFromLatLng

class PostViewModel : ViewModel() {

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
}