package com.group147.appartmentblog.model.service

import android.graphics.Bitmap
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.base.TaskCallback
import com.group147.appartmentblog.model.FirebaseModel
import com.group147.appartmentblog.model.User
import com.group147.appartmentblog.repositories.UserRepository
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

    suspend fun loginWithGoogle(account: GoogleSignInAccount, userRepository: UserRepository) {
        val firebaseCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        val user = auth.signInWithCredential(firebaseCredential).await().user

        if (user != null) {
            FirebaseModel.instance.getById(Collections.USERS, user.uid) { document, error ->
                if (error != null || document == null) {
                    return@getById
                }

                if (!document.exists()) {
                    val appUser = User(
                        id = user.uid,
                        email = user.email ?: "",
                        phoneNumber = "",
                        displayName = "",
                        imageUrl = account.photoUrl?.toString()
                    )
                    userRepository.insertUser(appUser, null) { _, error ->
                        if (error != null) {
                            throw error
                        }
                    }
                }
            }
        }
    }

    suspend fun loginWithEmail(email: String, password: String): AuthResult? {
        return auth.signInWithEmailAndPassword(email, password).await()
    }

    fun registerUser(
        userRepository: UserRepository,
        email: String,
        password: String,
        username: String,
        phone: String,
        imageBitmap: Bitmap?,
        callback: TaskCallback<String>
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        val user = User(
                            id = uid,
                            email = email,
                            phoneNumber = phone,
                            displayName = username
                        )
                        userRepository.insertUser(user, imageBitmap) { _, error ->
                            if (error != null) {
                                callback(null, error)
                            } else {
                                callback(uid, null)
                            }
                        }
                    } else {
                        callback(null, Exception("Failed to get user ID"))
                    }
                } else {
                    callback(null, task.exception)
                }
            }
    }

    fun signOut(userRepository: UserRepository) {
        userRepository.deleteUser()
        auth.signOut()
    }

    private fun FirebaseUser?.toAppUser(): User? {
        return if (this == null) null else User(
            id = this.uid,
            email = this.email ?: "",
            phoneNumber = this.phoneNumber ?: "",
            displayName = this.displayName ?: ""
        )
    }
}