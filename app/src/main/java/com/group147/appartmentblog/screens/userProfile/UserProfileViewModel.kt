package com.group147.appartmentblog.screens.userProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.model.User
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.repositories.UserRepository
import com.group147.appartmentblog.service.AuthService

class UserProfileViewModel (val postRepository: PostRepository, val userRepository: UserRepository) : ViewModel() {
    val user: LiveData<User> = userRepository.userLiveData
    val authService = AuthService()

    val allUserPosts: LiveData<List<Post>> = authService.currentUser
        .asLiveData()
        .switchMap { user ->
            postRepository.getPostsByCurrentUser(user?.id.toString())
        }

    fun signOut() {
        authService.signOut(userRepository)
    }
}