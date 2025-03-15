package com.group147.appartmentblog.screens.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentSignUpBinding
import com.group147.appartmentblog.screens.home.HomeActivity

class SignUpFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var viewModel: SignUpViewModel

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { galleryUri ->
            galleryUri?.let {
                binding.userImage.setImageURI(it)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                binding.userImage.setImageBitmap(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(layoutInflater)

        (activity as HomeActivity).hideBottomNavBar()
        (activity as HomeActivity).hideAddApartmentButton()
        (activity as HomeActivity).hideToolbar()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            requireActivity(),
            SignUpViewModelFactory((activity as HomeActivity).getUserRepository())
        )[SignUpViewModel::class.java]

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.uploadImageButton.setOnClickListener {
            PopupMenu(requireContext(), it).apply {
                setOnMenuItemClickListener(this@SignUpFragment)
                menuInflater.inflate(R.menu.image_picker_menu, menu)
                setForceShowIcon(true)
                show()
            }
        }

        binding.signupButton.setOnClickListener {
            var image = if (binding.userImage.drawable.constantState != ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_user_placeholder,
                    null
                )?.constantState
            ) (binding.userImage.drawable.toBitmap()) else null
            viewModel.signUp(
                binding.emailInput,
                binding.passwordInput,
                binding.usernameInput,
                binding.phoneInput,
                binding.confirmPasswordInput,
                image
            ) { userId, error ->
                if (error != null) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to register user: ${error.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    findNavController().navigate(R.id.feedFragment)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as HomeActivity).showBottomNavBar()
        (activity as HomeActivity).showAddApartmentButton()
        (activity as HomeActivity).showToolbar()
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