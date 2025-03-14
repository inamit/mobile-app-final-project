package com.group147.appartmentblog.screens.signup

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class SignUpViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult: LiveData<Boolean> get() = _registrationResult

    fun registerUser(email: String, password: String, username: String, phone: String, imageBitmap: Bitmap?) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        if (imageBitmap != null) {
                            uploadImage(imageBitmap) { imageUrl ->
                                saveUserData(uid, username, phone, imageUrl)
                            }
                        } else {
                            saveUserData(uid, username, phone, null)
                        }
                    } else {
                        _registrationResult.value = false
                    }
                } else {
                    _registrationResult.value = false
                }
            }
    }

    private fun saveUserData(uid: String, username: String, phone: String, imageUrl: String?) {
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

    private fun uploadImage(image: Bitmap, callback: (String?) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("user_images/${System.currentTimeMillis()}.jpg")

        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)
        uploadTask
            .addOnFailureListener {
                callback(null)
            }
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }
            }
    }
}