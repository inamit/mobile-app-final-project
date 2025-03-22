package com.group147.appartmentblog.screens.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.group147.appartmentblog.R
import com.group147.appartmentblog.databinding.FragmentMapBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.screens.MainActivity
import com.group147.appartmentblog.screens.MainViewModel
import com.group147.appartmentblog.service.LocationService
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: FragmentMapBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mainViewModel: MainViewModel
    private lateinit var viewModel: MapViewModel

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (LocationService.arePermissionsGranted(permissions)) {
                getUserLocation()
            } else {
                Snackbar.make(
                    binding.root,
                    "Failed to get location. Please enable location permissions.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater, container, false)

        mainViewModel.startLoading()
        (activity as MainActivity).hideAddApartmentButton()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        viewModel = ViewModelProvider(
            requireActivity(),
            MapViewModelFactory((activity as MainActivity).getPostRepository())
        )[MapViewModel::class.java]

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).showAddApartmentButton()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        getUserLocation()
        observePosts()
        mainViewModel.stopLoading()
    }

    private fun getUserLocation() {
        lifecycleScope.launch {
            LocationService.getUserLocation(
                requireActivity(),
                requestPermissionLauncher,
                fusedLocationClient
            )?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
                val icon = BitmapDescriptorFactory.fromBitmap(
                    viewModel.getBitmapFromDrawable(
                        requireContext(),
                        R.drawable.user_location_icon
                    )
                )

                map.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title("You are here")
                        .icon(icon)
                )
            }
        }
    }

    private fun observePosts() {
        val markerMap = mutableMapOf<Marker, Post>()
        viewModel.allPosts.observe(viewLifecycleOwner) { posts ->
            posts?.forEach { post ->
                val position = LatLng(post.location.latitude, post.location.longitude)

                val marker = map.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(post.title)
                        .icon(viewModel.resizeMapIcon(context, R.drawable.location_icon, 200, 200))
                )
                marker?.let { markerMap[it] = post }
            }
            map.setOnMarkerClickListener { marker ->
                val locationData = markerMap[marker]
                marker.showInfoWindow()
                locationData?.let {
                    openPostFragment(it)
                }
                true
            }
        }
    }

    private fun openPostFragment(post: Post) {
        val action = MapFragmentDirections
            .actionFragmentMapFragmentToFragmentPostFragment(post.id)

        findNavController().navigate(action)
    }
}
