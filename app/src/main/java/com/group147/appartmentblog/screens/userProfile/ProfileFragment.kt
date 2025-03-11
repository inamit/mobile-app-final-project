package com.group147.appartmentblog.screens.userProfile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.group147.appartmentblog.R
import com.group147.appartmentblog.screens.home.HomeActivity
import com.group147.appartmentblog.screens.login.LoginFragment

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var editImageView: ImageView
    private lateinit var usernameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var emailText: TextView
    private lateinit var updateProfileButton: Button
    private lateinit var btnLogout: Button
    private lateinit var selectedImageUri: Uri
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        profileImageView = view.findViewById(R.id.profile_image)
        editImageView = view.findViewById(R.id.edit_icon)
        usernameInput = view.findViewById(R.id.username_input)
        phoneInput = view.findViewById(R.id.phone_input)
        emailText = view.findViewById(R.id.email_text)
        updateProfileButton = view.findViewById(R.id.update_profile_button)
        btnLogout = view.findViewById(R.id.btnLogout)

        onLogoutClicked()

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data!!.data!!
                profileImageView.setImageURI(selectedImageUri)
            }
        }

        editImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        updateProfileButton.setOnClickListener {
            updateProfile()
        }

        loadUserProfile()

        return view
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        user?.let {
            val uid = user.uid
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        if (!document.getString("username").isNullOrEmpty())
                            usernameInput.hint = document.getString("username")
                        if (!document.getString("phone").isNullOrEmpty())
                            phoneInput.hint = document.getString("phone")
                        emailText.text = document.getString("email").orEmpty()
                        val imageUrl = document.getString("imageUrl")
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.ic_user_placeholder)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to load user data: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfile() {
        val user = auth.currentUser
        user?.let {
            val uid = user.uid
            val username = usernameInput.text.toString()
            val phone = phoneInput.text.toString()

            val userUpdates = hashMapOf<String, Any>()

            if (username.isNotEmpty()) {
                userUpdates["username"] = username
            }
            if (phone.isNotEmpty()) {
                userUpdates["phone"] = phone
            }

            if (::selectedImageUri.isInitialized) {
                val storageReference = storage.reference.child("user_images/$uid")
                storageReference.putFile(selectedImageUri)
                    .addOnSuccessListener {
                        storageReference.downloadUrl.addOnSuccessListener { uri ->
                            userUpdates["imageUrl"] = uri.toString()
                            saveUserUpdates(uid, userUpdates)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to upload image: $e", Toast.LENGTH_SHORT).show()
                    }
            } else {
                saveUserUpdates(uid, userUpdates)
            }
        }
    }

    private fun saveUserUpdates(uid: String, userUpdates: Map<String, Any>) {
        firestore.collection("users").document(uid).update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update profile: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onLogoutClicked() {
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }
}