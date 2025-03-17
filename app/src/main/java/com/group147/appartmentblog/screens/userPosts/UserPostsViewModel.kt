package com.group147.appartmentblog.screens.userPosts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.model.service.AuthService

class UserPostsViewModel (postRepository: PostRepository) : ViewModel() {
    val authService = AuthService()
    val allUserPosts: LiveData<List<Post>> = authService.currentUser
        .asLiveData()
        .switchMap { user ->
            postRepository.getPostsByCurrentUser(user?.id.toString())
        }
}