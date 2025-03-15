package com.group147.appartmentblog.screens.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentLoginBinding
import com.group147.appartmentblog.screens.home.HomeActivity

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        viewModel.onLoginWithGoogle(account, {
                            findNavController().navigate(R.id.feedFragment)
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

        viewModel = ViewModelProvider(
            requireActivity(),
            LoginViewModelFactory((activity as HomeActivity).getUserRepository())
        )[LoginViewModel::class.java]

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

    override fun onStop() {
        super.onStop()
        (activity as HomeActivity).showBottomNavBar()
        (activity as HomeActivity).showAddApartmentButton()
        (activity as HomeActivity).showToolbar()
    }

    private fun loginWithEmailAndPassword() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        viewModel.onLogin(
            email, password, {
                findNavController().navigate(R.id.feedFragment)
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
}