package com.group147.appartmentblog.screens.addApartment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentAddApartmentBinding
import com.group147.appartmentblog.permissions.LocationPermission
import com.group147.appartmentblog.screens.home.HomeActivity
import com.group147.appartmentblog.util.showSnackbar

class AddApartmentFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private lateinit var binding: FragmentAddApartmentBinding
    private lateinit var viewModel: AddApartmentViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

    private var location: GeoPoint? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddApartmentBinding.inflate(inflater, container, false)

        (activity as HomeActivity).hideBottomNavBar()
        (activity as HomeActivity).hideAddApartmentButton()
        (activity as HomeActivity).showToolbarNavigationIcon()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        getCurrentLocation()

        viewModel =
            ViewModelProvider(
                requireActivity(),
                AddApartmentViewModelFactory(
                    binding,
                    (activity as HomeActivity).getPostRepository()
                )
            )[AddApartmentViewModel::class.java]

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
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
            viewModel.savePost(location) {
                findNavController().popBackStack()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        (activity as HomeActivity).showBottomNavBar()
        (activity as HomeActivity).showAddApartmentButton()
        (activity as HomeActivity).hideToolbarNavigationIcon()
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
            if (location == null) {
                return@addOnSuccessListener
            }

            this.location = GeoPoint(location.latitude, location.longitude)
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