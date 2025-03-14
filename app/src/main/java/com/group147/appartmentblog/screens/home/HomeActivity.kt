package com.group147.appartmentblog.screens.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.group147.appartmentblog.R
import com.group147.appartmentblog.screens.login.LoginFragment

class HomeActivity : AppCompatActivity() {
    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val addApartmentButton: FloatingActionButton = findViewById(R.id.add_apartment_button)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        if (FirebaseAuth.getInstance().currentUser == null) {
            addApartmentButton.hide()
            bottomNavigationView.visibility = View.GONE
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.nav_host_fragment, LoginFragment())
            }
        } else {
            addApartmentButton.show()
            bottomNavigationView.visibility = View.VISIBLE
            val navHostFragment: NavHostFragment? =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            navController = navHostFragment?.navController

            navController?.let { NavigationUI.setupWithNavController(bottomNavigationView, it) }

            addApartmentButton.setOnClickListener {
                navController?.navigate(R.id.addApartmentFragment)
            }
        }
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