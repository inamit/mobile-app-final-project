package com.group147.appartmentblog.screens.signup

import android.graphics.Bitmap
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.group147.appartmentblog.base.TaskCallback
import com.group147.appartmentblog.model.service.AuthService
import com.group147.appartmentblog.repositories.UserRepository

class SignUpViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    fun signUp(
        emailInput: EditText,
        passwordInput: EditText,
        usernameInput: EditText,
        phoneInput: EditText,
        confirmPasswordInput: EditText,
        imageBitmap: Bitmap?,
        callback: TaskCallback<String>
    ) {
        if (validateInputs(
                emailInput,
                passwordInput,
                usernameInput,
                phoneInput,
                confirmPasswordInput
            )
        ) {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val username = usernameInput.text.toString()
            val phone = phoneInput.text.toString()

            AuthService().registerUser(
                userRepository,
                email,
                password,
                username,
                phone,
                imageBitmap,
                callback
            )
        }
    }

    private fun validateInputs(
        emailInput: EditText,
        passwordInput: EditText,
        usernameInput: EditText,
        phoneInput: EditText,
        confirmPasswordInput: EditText
    ): Boolean {
        val username = usernameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        if (username.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _toastMessage.postValue("Please fill out all the fields")
            return false
        }

        if (password != confirmPassword) {
            _toastMessage.postValue("Passwords do not match")
            return false
        }

        return true
    }
}