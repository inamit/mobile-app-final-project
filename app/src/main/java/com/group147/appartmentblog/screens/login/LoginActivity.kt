package com.group147.appartmentblog.screens.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.group147.appartmentblog.R
import com.group147.appartmentblog.model.FirebaseModel
import com.group147.appartmentblog.screens.home.HomeActivity
import com.group147.appartmentblog.screens.signup.SignUpActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        checkCurrentUser()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val signUpButton = findViewById<View>(R.id.sign_up_button)
        signUpButton.setOnClickListener {
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            startActivity(signUpIntent)
        }

        val googleLoginButton = findViewById<View>(R.id.google_login_button)
        googleLoginButton.setOnClickListener {
            loginWithGoogle()
        }

        val loginButton = findViewById<View>(R.id.login_button)
        loginButton.setOnClickListener {
            loginWithEmailAndPassword()
        }

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        val email = account.email
                        val imageUrl = account.photoUrl.toString()

                        viewModel.onLoginWithGoogle(account, {
                            saveUserDataToFirestore(email, imageUrl)
                            val homeIntent = Intent(this, HomeActivity::class.java)
                            startActivity(homeIntent)
                            finish()
                        }, {
                            Toast.makeText(this, "Failed to login with Google", Toast.LENGTH_SHORT).show()
                        })
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Failed to login with Google", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loginWithEmailAndPassword() {
        val email = findViewById<EditText>(R.id.email_input).text.toString()
        val password = findViewById<EditText>(R.id.password_input).text.toString()
        viewModel.onLogin(email, password, {
            val homeIntent = Intent(this, HomeActivity::class.java)
            startActivity(homeIntent)
            finish()
        },
            { message ->
                Toast.makeText(this, "Failed to login. $message", Toast.LENGTH_SHORT).show()
            })
    }

    private fun loginWithGoogle() {
        val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.web_client_id))
            .build())

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
                    "imageUrl" to imageUrl
                )

                if (document.contains("username")) {
                    userUpdates["username"] = document.getString("username")
                }
                if (document.contains("phone")) {
                    userUpdates["phone"] = document.getString("phone")
                }

                userRef.set(userUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "User logged in successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to login user", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to login user", Toast.LENGTH_SHORT).show()
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
                    return true
                }
            }
        )
    }
}