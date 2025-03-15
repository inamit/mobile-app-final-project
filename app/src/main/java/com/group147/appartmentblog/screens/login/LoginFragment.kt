package com.group147.appartmentblog.screens.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentLoginBinding
import com.group147.appartmentblog.model.FirebaseModel
import com.group147.appartmentblog.screens.home.HomeActivity

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        val email = account.email
                        val imageUrl = account.photoUrl.toString()

                        viewModel.onLoginWithGoogle(account, {
                            saveUserDataToFirestore(email, imageUrl)
                            val homeIntent = Intent(activity, HomeActivity::class.java)
                            startActivity(homeIntent)
                            activity?.finish()
                        }, {
                            Toast.makeText(
                                requireContext(),
                                "Failed to login with Google",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }
                } catch (e: ApiException) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to login with Google",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        (activity as HomeActivity).hideBottomNavBar()
        (activity as HomeActivity).hideAddApartmentButton()
        (activity as HomeActivity).hideToolbar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.signUpButton.setOnClickListener {
            findNavController().navigate(R.id.signUpFragment)
        }

        binding.googleLoginButton.setOnClickListener {
            loginWithGoogle()
        }

        binding.loginButton.setOnClickListener {
            loginWithEmailAndPassword()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as HomeActivity).showBottomNavBar()
        (activity as HomeActivity).showAddApartmentButton()
        (activity as HomeActivity).showToolbar()
    }

    private fun loginWithEmailAndPassword() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        viewModel.onLogin(
            email, password, {
                val homeIntent = Intent(activity, HomeActivity::class.java)
                startActivity(homeIntent)
                activity?.finish()
            },
            { message ->
                Toast.makeText(requireContext(), "Failed to login. $message", Toast.LENGTH_SHORT)
                    .show()
            })
    }

    private fun loginWithGoogle() {
        val googleSignInClient = GoogleSignIn.getClient(
            requireActivity(), GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.web_client_id))
                .build()
        )

        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun saveUserDataToFirestore(email: String?, imageUrl: String?) {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        val firestore = FirebaseModel().database
        val userRef = firestore.collection("users").document(uid)

        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val userUpdates = hashMapOf<String, Any?>(
                    "email" to email,
                )

                if (document.contains("username")) {
                    userUpdates["username"] = document.getString("username")
                }
                if (document.contains("phone")) {
                    userUpdates["phone"] = document.getString("phone")
                }
                if (document.contains("imageUrl")) {
                    userUpdates["imageUrl"] = document.getString("imageUrl")
                } else {
                    userUpdates["imageUrl"] = imageUrl
                }

                userRef.set(userUpdates)
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to login user", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to login user", Toast.LENGTH_SHORT).show()
        }
    }
}