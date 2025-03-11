package com.group147.appartmentblog.screens.signup

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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.group147.appartmentblog.R
import com.group147.appartmentblog.screens.login.LoginFragment

class SignUpFragment : Fragment() {

    private var selectedImageUri: Uri? = null
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton: Button = view.findViewById(R.id.back_button)
        val uploadImageButton: Button = view.findViewById(R.id.upload_image_button)
        val signupButton: Button = view.findViewById(R.id.signup_button)
        val emailInput: EditText = view.findViewById(R.id.email_input)
        val passwordInput: EditText = view.findViewById(R.id.password_input)
        val usernameInput: EditText = view.findViewById(R.id.username_input)
        val phoneInput: EditText = view.findViewById(R.id.phone_input)
        val confirmPasswordInput: EditText = view.findViewById(R.id.confirm_password_input)
        val userImage: ImageView = view.findViewById(R.id.user_image)

        backButton.setOnClickListener {
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.nav_host_fragment, LoginFragment())
                addToBackStack(null)
            }
        }

        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        signupButton.setOnClickListener {
            if (validateInputs(emailInput, passwordInput, usernameInput, phoneInput, confirmPasswordInput)) {
                val email = emailInput.text.toString()
                val password = passwordInput.text.toString()
                val username = usernameInput.text.toString()
                val phone = phoneInput.text.toString()
                val imageUri = selectedImageUri ?: Uri.parse("android.resource://${requireContext().packageName}/drawable/ic_user_placeholder")
                viewModel.registerUser(email, password, username, phone, imageUri)
            }
        }

        viewModel.registrationResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "User registered successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.nav_host_fragment, LoginFragment())
                    addToBackStack(null)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to register user", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            val userImage: ImageView = view?.findViewById(R.id.user_image) ?: return
            userImage.setImageURI(selectedImageUri)
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
            Toast.makeText(requireContext(), "Please fill out all the fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}