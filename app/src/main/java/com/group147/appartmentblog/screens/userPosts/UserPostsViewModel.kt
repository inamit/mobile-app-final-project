package com.group147.appartmentblog.screens.userPosts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.model.service.AuthService
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.screens.home.HomeViewModel
class UserPostsViewModel (postRepository: PostRepository) : ViewModel() {
    val authService = AuthService()
    val allUserPosts: List<Post> = postRepository.getPostsByCurrentUser(authService.)
}