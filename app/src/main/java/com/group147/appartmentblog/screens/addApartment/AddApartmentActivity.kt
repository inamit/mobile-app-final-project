package com.group147.appartmentblog.screens.addApartment

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.group147.appartmentblog.R
import com.group147.appartmentblog.database.post.PostDatabase
import com.group147.appartmentblog.databinding.ActivityAddApartmentBinding
import com.group147.appartmentblog.model.FirebaseModel
import com.group147.appartmentblog.permissions.LocationPermission
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.util.showSnackbar

class AddApartmentActivity : AppCompatActivity(), OnMenuItemClickListener {
    private lateinit var binding: ActivityAddApartmentBinding
    private lateinit var viewModel: AddApartmentViewModel
    private lateinit var storage: FirebaseStorage
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { galleryUri ->
            galleryUri?.let {
                binding.imagePreview.setImageURI(it)
            }
        }
    val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                binding.imagePreview.setImageBitmap(it)
            }
        }

    val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                binding.root.showSnackbar(
                    "Location permission is required to upload a post",
                    Snackbar.LENGTH_INDEFINITE, "I agree"
                ) {
                    getCurrentLocation()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddApartmentBinding.inflate(layoutInflater)
        storage = Firebase.storage
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.pickImage.setOnClickListener {
            PopupMenu(this, it).apply {
                setOnMenuItemClickListener(this@AddApartmentActivity)
                menuInflater.inflate(R.menu.image_picker_menu, menu)
                setForceShowIcon(true)
                show()
            }
        }

        binding.saveButton.setOnClickListener {
            viewModel.savePost {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        getCurrentLocation()

        val firebaseModel = FirebaseModel()
        val database = PostDatabase.getDatabase(this)
        val postDao = database.postDao()
        val postRepository = PostRepository(firebaseModel, postDao)
        viewModel =
            ViewModelProvider(this, AddApartmentViewModelFactory(binding, postRepository)).get(
                AddApartmentViewModel::class.java
            )

        viewModel.toastMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentLocation() {
        Log.i("AddApartmentActivity", "getCurrentLocation")

        if (!LocationPermission.checkLocationPermission(
                this,
                binding.root,
                requestPermissionLauncher
            )
        ) {
            return
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location ->
            viewModel.location = GeoPoint(location.latitude, location.longitude)
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}