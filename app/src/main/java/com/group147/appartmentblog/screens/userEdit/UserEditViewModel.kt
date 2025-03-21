package com.group147.appartmentblog.screens.userEdit

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.group147.appartmentblog.R
import com.group147.appartmentblog.model.User
import com.group147.appartmentblog.model.service.AuthService
import com.group147.appartmentblog.repositories.UserRepository

class UserEditViewModel(private val userRepository: UserRepository) : ViewModel() {
    val user: LiveData<User> = userRepository.userLiveData
    val authService = AuthService()

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun updateUser(user: User, image: Bitmap?) {
        _loading.postValue(true)
        userRepository.updateUser(user, image) { _, error ->
            if (error != null) {
                _toastMessage.postValue("Failed to update user")
            }

            _loading.postValue(false)
        }
    }

    fun signOut() {
        _loading.postValue(true)
        authService.signOut(userRepository)
        _loading.postValue(false)
    }
}