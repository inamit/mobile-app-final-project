package com.group147.appartmentblog.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group147.appartmentblog.model.User
import com.group147.appartmentblog.model.service.AuthService
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val authService = AuthService()
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    init {
        viewModelScope.launch {
            authService.currentUser.collect {
                _currentUser.postValue(it)
            }
        }
    }
}