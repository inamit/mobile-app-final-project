package com.group147.appartmentblog.screens.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.coroutineScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.group147.appartmentblog.ERROR_TAG
import com.group147.appartmentblog.R
import com.group147.appartmentblog.screens.home.HomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        checkCurrentUser()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val googleLoginButton = findViewById<View>(R.id.google_login_button)
        googleLoginButton.setOnClickListener {
            try {
                loginWithGoogle()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to login with Google", Toast.LENGTH_SHORT).show()
                Log.e(ERROR_TAG, "Failed to login with Google", e)
            }
        }

    }

    private fun loginWithGoogle() {
        val context = this
        val coroutineScope = lifecycle.coroutineScope
        val credentialManager = CredentialManager.create(context)

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(true)
            .setServerClientId(getString(R.string.web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(request = request, context = context)
                viewModel.onSignUpWithGoogle(result.credential, {
                    val homeIntent = Intent(context, HomeActivity::class.java)
                    startActivity(homeIntent)
                    finish()
                },
                    {
                        Toast.makeText(context, "Failed to login with Google", Toast.LENGTH_SHORT)
                            .show()
                    })
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to login with Google", Toast.LENGTH_SHORT)
                    .show()
                Log.e(ERROR_TAG, "Failed to get Google credential", e)
            }
        }
    }

    private fun checkCurrentUser() {
        val homeIntent = Intent(this, HomeActivity::class.java)

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (viewModel.isLoggedIn()) {
                        startActivity(homeIntent)
                        finish()
                    }

                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true;
                }
            }
        )
    }
}