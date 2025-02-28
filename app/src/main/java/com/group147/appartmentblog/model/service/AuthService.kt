package com.group147.appartmentblog.model.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.group147.appartmentblog.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthService {
    val auth = Firebase.auth
    val currentUser: Flow<User?>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { authState ->
                    this.trySend(authState.currentUser.toAppUser())
                }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    fun hasUser(): Boolean {
        return auth.currentUser != null
    }

    fun getUserProfile(): User {
        return auth.currentUser.toAppUser()
    }

    suspend fun loginWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential).await()
    }

    suspend fun loginWithEmail(email: String, password: String): AuthResult? {
        return auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUpWithEmail(email: String, password: String): AuthResult? {
        return auth.createUserWithEmailAndPassword(email, password).await()
    }


    fun signOut() {
        auth.signOut()
    }

    suspend fun deleteAccount() {
        auth.currentUser!!.delete().await()
    }

    private fun FirebaseUser?.toAppUser(): User {
        return if (this == null) User() else User(
            id = this.uid,
            email = this.email ?: "",
            provider = this.providerId,
            displayName = this.displayName ?: ""
        )
    }
}