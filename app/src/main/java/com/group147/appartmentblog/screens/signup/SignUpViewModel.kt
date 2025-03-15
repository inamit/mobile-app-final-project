package com.group147.appartmentblog.screens.signup

import android.graphics.Bitmap
import android.widget.EditText
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

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

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

    fun signUp(
        emailInput: EditText,
        passwordInput: EditText,
        usernameInput: EditText,
        phoneInput: EditText,
        confirmPasswordInput: EditText,
        imageBitmap: Bitmap?
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

            registerUser(email, password, username, phone, imageBitmap)
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