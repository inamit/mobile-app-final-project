package com.group147.appartmentblog.screens.signup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.group147.appartmentblog.R
import com.group147.appartmentblog.screens.login.LoginActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var userImageView: ImageView
    private lateinit var uploadImageButton: Button
    private lateinit var selectedImageUri: Uri
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        userImageView = findViewById(R.id.user_image)
        uploadImageButton = findViewById(R.id.upload_image_button)

        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        findViewById<Button>(R.id.signup_button).setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            userImageView.setImageURI(selectedImageUri)
        }
    }

    private fun validateInputs(): Boolean {
        val username = findViewById<EditText>(R.id.username_input).text.toString().trim()
        val phone = findViewById<EditText>(R.id.phone_input).text.toString().trim()
        val email = findViewById<EditText>(R.id.email_input).text.toString().trim()
        val password = findViewById<EditText>(R.id.password_input).text.toString().trim()
        val confirmPassword = findViewById<EditText>(R.id.confirm_password_input).text.toString().trim()

        if (username.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registerUser() {
        val username = findViewById<EditText>(R.id.username_input).text.toString()
        val phone = findViewById<EditText>(R.id.phone_input).text.toString()
        val email = findViewById<EditText>(R.id.email_input).text.toString()
        val password = findViewById<EditText>(R.id.password_input).text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    uploadMoreDataToFirebase(uid!!, username, phone)
                } else {
                    Toast.makeText(this, "Failed to register user", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadMoreDataToFirebase(uid: String, username: String, phone: String) {
        if (::selectedImageUri.isInitialized) {
            saveAddedData(uid, selectedImageUri, username, phone)
        } else {
            val defaultImage: Uri = Uri.parse("android.resource://com.group147.appartmentblog/drawable/ic_user_placeholder")
            saveAddedData(uid, defaultImage, username, phone)
        }
    }

    private fun saveAddedData(uid: String, imageUri: Uri, username: String, phone: String) {
        val storageReference = FirebaseStorage.getInstance().reference.child("user_images/$uid")
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    saveUserData(uri.toString(), username, phone, uid)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData(imageUrl: String?, username: String, phone: String, uid: String) {
        val email = findViewById<EditText>(R.id.email_input).text.toString()

        val user = hashMapOf(
            "username" to username,
            "phone" to phone,
            "email" to email,
            "imageUrl" to imageUrl
        )

        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to register user: $e", Toast.LENGTH_SHORT).show()
            }
    }
}