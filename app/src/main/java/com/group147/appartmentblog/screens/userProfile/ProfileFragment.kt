package com.group147.appartmentblog.screens.userProfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentProfileBinding
import com.group147.appartmentblog.model.User
import com.group147.appartmentblog.screens.MainActivity
import com.squareup.picasso.Picasso

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userRepository = (activity as MainActivity).getUserRepository()

        val userPostButton = view.findViewById<Button>(R.id.user_posts_button)
        userPostButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_userPostsFragment)
        }

        viewModel = ViewModelProvider(
            requireActivity(),
            ProfileViewModelFactory(userRepository)
        )[ProfileViewModel::class.java]

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

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        loadUserProfile()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showProfileToolbarMenu {
            when (it.itemId) {
                R.id.logout -> {
                    onLogoutClicked()
                    true
                }

                else -> false
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).hideToolbarMenu()
    }

    private fun loadUserProfile() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.usernameInput.setText(user?.displayName ?: "")
            binding.phoneInput.setText(user?.phoneNumber ?: "")
            binding.emailText.text = user?.email

            if (user?.imageUrl.isNullOrEmpty()) {
                binding.profileImage.setImageResource(R.drawable.ic_user_placeholder)
            } else {
                Picasso.get().load(user.imageUrl).into(binding.profileImage)
            }
        }
    }

    private fun updateProfile() {
        val existingUser = viewModel.user.value

        if (existingUser == null) {
            context?.let {
                Toast.makeText(it, "User not found", Toast.LENGTH_SHORT).show()
            }

            return
        }

        val updatedUser = User(
            id = existingUser.id,
            email = existingUser.email,
            phoneNumber = binding.phoneInput.text.toString(),
            displayName = binding.usernameInput.text.toString()
        )

        var image = if (binding.profileImage.drawable.constantState != ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_user_placeholder,
                null
            )?.constantState
        ) (binding.profileImage.drawable.toBitmap()) else null

        viewModel.updateUser(updatedUser, image)
    }

    private fun onLogoutClicked() {
        viewModel.signOut()
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
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