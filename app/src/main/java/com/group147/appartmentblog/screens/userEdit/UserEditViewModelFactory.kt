package com.group147.appartmentblog.screens.userEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group147.appartmentblog.repositories.UserRepository
import com.group147.appartmentblog.screens.MainViewModel

class UserEditViewModelFactory(
    private val mainViewModel: MainViewModel,
    private val userRepository: UserRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserEditViewModel(mainViewModel, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}