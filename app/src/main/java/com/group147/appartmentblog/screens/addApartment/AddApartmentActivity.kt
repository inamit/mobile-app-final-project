package com.group147.appartmentblog.screens.addApartment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.ActivityAddApartmentBinding
import java.io.ByteArrayOutputStream

class AddApartmentActivity : AppCompatActivity(), OnMenuItemClickListener {
    lateinit var binding: ActivityAddApartmentBinding
    lateinit var storage: FirebaseStorage
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

        binding.pickImage.setOnClickListener {
            PopupMenu(this, it).apply {
                setOnMenuItemClickListener(this@AddApartmentActivity)
                menuInflater.inflate(R.menu.image_picker_menu, menu)
                setForceShowIcon(true)
                show()
            }
        }

        binding.saveButton.setOnClickListener {
            savePost()
        }
    }

    fun savePost() {
        val post = hashMapOf(
            "userId" to Firebase.auth.currentUser?.uid,
            "title" to binding.titleEditText.text.toString(),
            "content" to binding.contentEditText.text.toString()
        )
        val db = Firebase.firestore
        db.collection("posts")
            .add(post)
            .addOnSuccessListener { documentReference ->
                val postId = documentReference.id
                val image = binding.imagePreview.drawable.toBitmap()
                uploadImage(image, "main", postId) {
                    if (it != null) {
                        onBackPressedDispatcher.onBackPressed()
                    } else {
                        documentReference.delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Failed post apartment. Please try again.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                    }
                }
            }
    }

    fun uploadImage(image: Bitmap, name: String, postId: String, callback: (String?) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("posts/$postId/$name.jpg")

        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)
        uploadTask
            .addOnFailureListener {
                callback(null)
            }
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener {
                    callback(it.toString())
                }
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
}