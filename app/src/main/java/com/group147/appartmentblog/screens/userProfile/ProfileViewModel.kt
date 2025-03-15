package com.group147.appartmentblog.screens.userProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.group147.appartmentblog.R
import com.group147.appartmentblog.model.User
import com.group147.appartmentblog.model.service.AuthService
import com.group147.appartmentblog.repositories.UserRepository

class ProfileViewModel(userRepository: UserRepository) : ViewModel() {
    val user: LiveData<User> = userRepository.userLiveData
    val authService = AuthService()

    fun signOut(navController: NavController) {
        authService.signOut()
        navController.navigate(R.id.loginFragment)

    }
}