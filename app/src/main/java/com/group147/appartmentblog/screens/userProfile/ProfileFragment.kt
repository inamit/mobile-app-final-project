package com.group147.appartmentblog.screens.userProfile

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.group147.appartmentblog.R
import java.io.ByteArrayOutputStream
import android.content.Intent
import com.group147.appartmentblog.screens.home.HomeActivity

class ProfileFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private lateinit var profileImageView: ImageView
    private lateinit var editImageView: ImageView
    private lateinit var usernameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var emailText: TextView
    private lateinit var updateProfileButton: Button
    private lateinit var btnLogout: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>

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

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { galleryUri ->
            galleryUri?.let {
                profileImageView.setImageURI(it)
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                profileImageView.setImageBitmap(it)
            }
        }

        editImageView.setOnClickListener {
            PopupMenu(requireContext(), it).apply {
                setOnMenuItemClickListener(this@ProfileFragment)
                menuInflater.inflate(R.menu.image_picker_menu, menu)
                setForceShowIcon(true)
                show()
            }
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
                    context?.let {
                        Toast.makeText(it, "Failed to load user data: $e", Toast.LENGTH_SHORT).show()
                    }
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

            val imageBitmap = profileImageView.drawable.toBitmap()
            uploadImage(imageBitmap, uid) { imageUrl ->
                if (imageUrl != null) {
                    userUpdates["imageUrl"] = imageUrl
                }
                saveUserUpdates(uid, userUpdates)
            }
        }
    }

    private fun uploadImage(image: Bitmap, uid: String, callback: (String?) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("user_images/$uid.jpg")

        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)
        uploadTask
            .addOnFailureListener {
                callback(null)
            }
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener {
                    callback(it.toString())
                }
            }
    }

    private fun saveUserUpdates(uid: String, userUpdates: Map<String, Any>) {
        firestore.collection("users").document(uid).update(userUpdates)
            .addOnSuccessListener {
                context?.let {
                    Toast.makeText(it, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                loadUserProfile()
            }
            .addOnFailureListener { e ->
                context?.let {
                    Toast.makeText(it, "Failed to update profile: $e", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun onLogoutClicked() {
        btnLogout.setOnClickListener {
            auth.signOut()
            context?.let {
                Toast.makeText(it, "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(activity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.camera -> {
                cameraLauncher.launch(null)
                true
            }
            R.id.gallery -> {
                galleryLauncher.launch("image/*")
                true
            }
            else -> false
        }
    }
}