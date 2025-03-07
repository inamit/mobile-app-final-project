package com.group147.appartmentblog.screens.login

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.group147.appartmentblog.ERROR_TAG
import com.group147.appartmentblog.model.service.AuthService
import com.group147.appartmentblog.screens.AppartmentBlogViewModel

class LoginViewModel : AppartmentBlogViewModel() {
    private val authService: AuthService = AuthService()

    fun isLoggedIn(): Boolean {
        return authService.hasUser()
    }

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
}