package com.group147.appartmentblog.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.group147.appartmentblog.ERROR_TAG
import com.group147.appartmentblog.model.service.AuthService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val authService: AuthService = AuthService()

    fun onLogin(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        launchCatching(onError) {
            val authResult = authService.loginWithEmail(email, password)

            if (authResult?.user != null) {
                onSuccess()
            } else {
                Log.e(ERROR_TAG, "Failed to login with email and password")
                onError("")
            }
        }
    }

    fun onLoginWithGoogle(account: GoogleSignInAccount, onSuccess: () -> Unit, onError: (String) -> Unit) {
        launchCatching(onError) {
            val idToken = account.idToken
            if (idToken != null) {
                authService.loginWithGoogle(idToken)
                onSuccess()
            } else {
                Log.e(ERROR_TAG, "Google ID token is null")
                onError("")
            }
        }
    }

    fun launchCatching(onError: (String) -> Unit = {}, block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(
            CoroutineExceptionHandler { _, throwable ->
                Log.d(ERROR_TAG, throwable.message.orEmpty())
                onError(throwable.message.orEmpty())
            },
            block = block
        )
}