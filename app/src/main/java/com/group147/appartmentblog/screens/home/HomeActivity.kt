package com.group147.appartmentblog.screens.home

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.group147.appartmentblog.R
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.database.post.PostDatabase
import com.group147.appartmentblog.database.user.UserDatabase
import com.group147.appartmentblog.databinding.ActivityHomeBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.model.User
import com.group147.appartmentblog.model.service.SubscriptionService
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val viewModel by viewModels<HomeViewModel>()
    var navController: NavController? = null

    private var postSubscriptionService: SubscriptionService<Post>? = null
    private var userSubscriptionService: SubscriptionService<User>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navController = navHostFragment?.navController

        navController?.let { NavigationUI.setupWithNavController(binding.bottomNavigationView, it) }

        viewModel.currentUser.observe(this) {
            CoroutineScope(Dispatchers.IO).launch {
                if (it == null) {
                    postSubscriptionService?.stopListening()
                    userSubscriptionService?.stopListening()
                } else {
                    val postRepository = getPostRepository()
                    if (postSubscriptionService == null) {
                        postSubscriptionService = SubscriptionService(postRepository)
                    }
                    postSubscriptionService?.listenForCollection(
                        Collections.POSTS,
                        postRepository.getLatestUpdatedTime()
                    )

                    val userRepository = getUserRepository()
                    if (userSubscriptionService == null) {
                        userSubscriptionService = SubscriptionService(userRepository)
                    }
                    userSubscriptionService?.listenForEntity(Collections.USERS, it.id)
                }
            }
        }

        if (viewModel.authService.hasUser()) {
            goToApp()
        } else {
            goToLogin()
        }
    }

    fun getPostRepository(): PostRepository {
        val database = PostDatabase.getDatabase(this)
        val postDao = database.postDao()
        return PostRepository.getRepository(postDao)
    }

    fun getUserRepository(): UserRepository {
        val database = UserDatabase.getDatabase(this)
        val userDao = database.userDao()
        return UserRepository.getRepository(userDao)
    }

    private fun goToApp() {
        showAddApartmentButton()
        showBottomNavBar()

        binding.addApartmentButton.setOnClickListener {
            navController?.navigate(R.id.addApartmentFragment)
        }
    }

    private fun goToLogin() {
        hideAddApartmentButton()
        hideBottomNavBar()

        navController?.navigate(R.id.loginFragment)
    }

    fun showAddApartmentButton() {
        binding.addApartmentButton.show()
    }

    fun hideAddApartmentButton() {
        binding.addApartmentButton.hide()
    }

    fun showBottomNavBar() {
        Log.d("HomeActivity", "showBottomNavBar")
        binding.bottomNavigationView.visibility = View.VISIBLE
    }

    fun hideBottomNavBar() {
        Log.d("HomeActivity", "hideBottomNavBar")
        binding.bottomNavigationView.visibility = View.GONE
    }

    fun showToolbar() {
        binding.toolbar.visibility = View.VISIBLE
    }

    fun hideToolbar() {
        binding.toolbar.visibility = View.GONE
    }

    fun showToolbarNavigationIcon() {
        binding.toolbar.setNavigationIcon(R.drawable.back_icon)
        binding.toolbar.setNavigationOnClickListener {
            navController?.popBackStack()
        }
    }

    fun hideToolbarNavigationIcon() {
        binding.toolbar.navigationIcon = null
    }

    fun showProfileToolbarMenu(onMenuItemClickListener: Toolbar.OnMenuItemClickListener) {
        binding.toolbar.inflateMenu(R.menu.profile_toolbar_menu)
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClickListener)
    }

    fun hideToolbarMenu() {
        binding.toolbar.menu.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        navController?.let { NavigationUI.onNavDestinationSelected(item, it) }
        return true
    }
}