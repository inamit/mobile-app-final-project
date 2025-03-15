package com.group147.appartmentblog.screens.userProfile

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentProfileBinding
import com.group147.appartmentblog.screens.home.HomeActivity
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding

    private var galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { galleryUri ->
            galleryUri?.let {
                binding.profileImage.setImageURI(it)
            }
        }

    private var cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                binding.profileImage.setImageBitmap(it)
            }
        }

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            requireActivity(),
            ProfileViewModelFactory((activity as HomeActivity).getUserRepository())
        )[ProfileViewModel::class.java]

        (activity as HomeActivity).showProfileToolbarMenu {
            when (it.itemId) {
                R.id.logout -> {
                    onLogoutClicked()
                    true
                }

                else -> false
            }
        }

        binding.editIcon.setOnClickListener {
            PopupMenu(requireContext(), it).apply {
                setOnMenuItemClickListener(this@ProfileFragment)
                menuInflater.inflate(R.menu.image_picker_menu, menu)
                setForceShowIcon(true)
                show()
            }
        }

        binding.updateProfileButton.setOnClickListener {
            updateProfile()
        }

        loadUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as HomeActivity).hideToolbarMenu()
    }

    private fun loadUserProfile() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.usernameInput.hint = user.displayName
            binding.phoneInput.hint = user.phoneNumber
            binding.emailText.text = user.email

            if (user.imageUrl.isNullOrEmpty()) {
                binding.profileImage.setImageResource(R.drawable.ic_user_placeholder)
            } else {
                Picasso.get().load(user.imageUrl).into(binding.profileImage)
            }
        }
    }

    private fun updateProfile() {
        val user = auth.currentUser
        user?.let {
            val uid = user.uid
            val username = binding.usernameInput.text.toString()
            val phone = binding.phoneInput.text.toString()

            val userUpdates = hashMapOf<String, Any>()

            if (username.isNotEmpty()) {
                userUpdates["username"] = username
            }
            if (phone.isNotEmpty()) {
                userUpdates["phone"] = phone
            }

            val imageBitmap = binding.profileImage.drawable.toBitmap()
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
        viewModel.signOut(findNavController())
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