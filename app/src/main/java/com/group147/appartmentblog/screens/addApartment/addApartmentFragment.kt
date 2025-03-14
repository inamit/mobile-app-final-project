package com.group147.appartmentblog.screens.addApartment

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentAddApartmentBinding
import com.group147.appartmentblog.permissions.LocationPermission
import com.group147.appartmentblog.util.showSnackbar
import java.io.ByteArrayOutputStream

class AddApartmentFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private lateinit var binding: FragmentAddApartmentBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: GeoPoint? = null
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { galleryUri ->
            galleryUri?.let {
                binding.imagePreview.setImageURI(it)
            }
        }
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                binding.imagePreview.setImageBitmap(it)
            }
        }
    private val requestPermissionLauncher =
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddApartmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storage = FirebaseStorage.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.feedFragment)
        }

        binding.pickImage.setOnClickListener {
            PopupMenu(requireContext(), it).apply {
                setOnMenuItemClickListener(this@AddApartmentFragment)
                menuInflater.inflate(R.menu.image_picker_menu, menu)
                setForceShowIcon(true)
                show()
            }
        }

        binding.saveButton.setOnClickListener {
            savePost()
        }

        getCurrentLocation()
    }

    private fun savePost() {
        if (location == null) {
            Toast.makeText(
                requireContext(),
                "Location is required to upload a post",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!validateForm()) {
            return
        }

        val post = hashMapOf(
            "userId" to FirebaseAuth.getInstance().currentUser?.uid,
            "title" to binding.titleEditText.text.toString(),
            "content" to binding.contentEditText.text.toString(),
            "floor" to binding.floorEditText.text.toString().toInt(),
            "rooms" to binding.roomsEditText.text.toString().toDouble(),
            "price" to binding.priceEditText.text.toString().toDouble(),
            "location" to location
        )
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .add(post)
            .addOnSuccessListener { documentReference ->
                val postId = documentReference.id
                val image = binding.imagePreview.drawable.toBitmap()
                uploadImage(image, "main", postId) {
                    if (it != null) {
                        Toast.makeText(
                            requireContext(),
                            "Post uploaded successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.feedFragment)
                    } else {
                        documentReference.delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to post apartment. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }
    }

    private fun getCurrentLocation() {
        Log.i("AddApartmentFragment", "getCurrentLocation")

        if (!LocationPermission.checkLocationPermission(
                requireActivity() as AppCompatActivity,
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
            this.location = GeoPoint(location.latitude, location.longitude)
        }
    }

    private fun uploadImage(image: Bitmap, name: String, postId: String, callback: (String?) -> Unit) {
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

    private fun validateForm(): Boolean {
        var valid = true

        val requiredEditTexts = listOf(
            Pair(binding.titleEditText, "Title is required"),
            Pair(binding.floorEditText, "Floor is required"),
            Pair(binding.roomsEditText, "Rooms is required"),
            Pair(binding.priceEditText, "Price is required"),
            Pair(binding.contentEditText, "Content is required"),
        )

        for ((editText, errorMessage) in requiredEditTexts) {
            if (editText.text.isEmpty()) {
                editText.error = errorMessage
                valid = false
            }
        }

        val image = binding.imagePreview.drawable

        if (image == null) {
            binding.pickImage.error = "Image is required"
            valid = false
        }

        return valid
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