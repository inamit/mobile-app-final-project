package com.group147.appartmentblog.screens.addApartment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentAddApartmentBinding
import com.group147.appartmentblog.screens.MainActivity
import com.group147.appartmentblog.service.LocationService
import kotlinx.coroutines.launch

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
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (LocationService.arePermissionsGranted(permissions)) {
                getCurrentLocation()
            } else {
                Snackbar.make(
                    binding.root,
                    "Location permission is required to upload a post",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    private var location: GeoPoint? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddApartmentBinding.inflate(inflater, container, false)

        (activity as MainActivity).hideBottomNavBar()
        (activity as MainActivity).hideAddApartmentButton()
        (activity as MainActivity).showToolbarNavigationIcon()

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
                    (activity as MainActivity).getPostRepository()
                )
            )[AddApartmentViewModel::class.java]

        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            if (it) {
                (activity as MainActivity).showLoadingOverlay()
            } else {
                (activity as MainActivity).hideLoadingOverlay()
            }
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
            viewModel.savePost(binding, location) {
                findNavController().popBackStack()
            }
        }

    }

    private fun getCurrentLocation() {
        lifecycleScope.launch {
            LocationService.getUserLocation(
                requireActivity(),
                requestPermissionLauncher,
                fusedLocationClient
            )?.let {
                location = GeoPoint(it.latitude, it.longitude)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        (activity as MainActivity).showBottomNavBar()
        (activity as MainActivity).showAddApartmentButton()
        (activity as MainActivity).hideToolbarNavigationIcon()
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