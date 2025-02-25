package com.group147.appartmentblog.screens.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group147.appartmentblog.model.service.AuthService
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    private val authService = AuthService()

    fun onSignUp(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                authService.signUpWithEmail(email, password)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}