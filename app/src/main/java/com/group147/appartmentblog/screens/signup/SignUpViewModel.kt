package com.group147.appartmentblog.screens.signup

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SignUpViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult: LiveData<Boolean> get() = _registrationResult

    fun registerUser(email: String, password: String, username: String, phone: String, imageUri: Uri?) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        uploadMoreDataToFirebase(uid, username, phone, imageUri)
                    } else {
                        _registrationResult.value = false
                    }
                } else {
                    _registrationResult.value = false
                }
            }
    }

    private fun uploadMoreDataToFirebase(uid: String, username: String, phone: String, imageUri: Uri?) {
        val uri = imageUri ?: Uri.parse("android.resource://com.group147.appartmentblog/drawable/ic_user_placeholder")
        saveAddedData(uid, uri, username, phone)
    }

    private fun saveAddedData(uid: String, imageUri: Uri, username: String, phone: String) {
        val storageReference = storage.reference.child("user_images/$uid")
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    saveUserData(uri.toString(), username, phone, uid)
                }
            }
            .addOnFailureListener {
                _registrationResult.value = false
            }
    }

    private fun saveUserData(imageUrl: String?, username: String, phone: String, uid: String) {
        val email = auth.currentUser?.email ?: return

        val user = hashMapOf(
            "username" to username,
            "phone" to phone,
            "email" to email,
            "imageUrl" to imageUrl
        )

        firestore.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                _registrationResult.value = true
            }
            .addOnFailureListener {
                _registrationResult.value = false
            }
    }
}