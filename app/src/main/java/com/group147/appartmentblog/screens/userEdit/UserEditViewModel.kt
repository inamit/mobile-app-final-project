package com.group147.appartmentblog.screens.userEdit

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group147.appartmentblog.model.User
import com.group147.appartmentblog.repositories.UserRepository
import com.group147.appartmentblog.screens.MainViewModel
import com.group147.appartmentblog.service.AuthService

class UserEditViewModel(
    private val mainViewModel: MainViewModel,
    private val userRepository: UserRepository
) : ViewModel() {
    val user: LiveData<User> = userRepository.userLiveData
    val authService = AuthService()

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    fun updateUser(user: User, image: Bitmap?) {
        mainViewModel.startLoading()
        userRepository.updateUser(user, image) { _, error ->
            if (error != null) {
                _toastMessage.postValue("Failed to update user")
            }

            mainViewModel.stopLoading()
        }
    }

    fun signOut() {
        mainViewModel.startLoading()
        authService.signOut(userRepository)
        mainViewModel.stopLoading()
    }
}