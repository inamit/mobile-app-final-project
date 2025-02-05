package com.group147.appartmentblog.screens.addApartment

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.group147.appartmentblog.R

class AddApartmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_apartment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        handleImagePicker()
    }

    private fun handleImagePicker() {
        val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
            {
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: $uri")
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }
        }

        findViewById<Button>(R.id.pickImage).setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(PickVisualMedia.ImageOnly)
                    .build()
            )
        }
    }
}