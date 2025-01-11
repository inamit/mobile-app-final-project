package com.group147.appartmentblog.screens.login

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.group147.appartmentblog.ERROR_TAG
import com.group147.appartmentblog.UNEXPECTED_CREDENTIAL
import com.group147.appartmentblog.model.service.AuthService
import com.group147.appartmentblog.screens.AppartmentBlogViewModel

class LoginViewModel : AppartmentBlogViewModel() {
    private val authService: AuthService = AuthService()

    fun isLoggedIn(): Boolean {
        return authService.hasUser()
    }

    fun onSignUpWithGoogle(credential: Credential, onSuccess: () -> Unit, onError: () -> Unit) {
        launchCatching {
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                authService.loginWithGoogle(googleIdTokenCredential.idToken)
                onSuccess()
            } else {
                Log.e(ERROR_TAG, UNEXPECTED_CREDENTIAL)
                onError()
            }
        }
    }
}