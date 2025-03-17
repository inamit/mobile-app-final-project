package com.group147.appartmentblog.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.group147.appartmentblog.R
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.screens.MainActivity

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).showLoadingOverlay()
        (activity as MainActivity).hideAddApartmentButton()

        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

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
        (activity as MainActivity).hideLoadingOverlay()
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
                    val icon = BitmapDescriptorFactory.fromBitmap(
                        viewModel.getBitmapFromDrawable(
                            requireContext(),
                            R.drawable.user_location_icon
                        )
                    )

                    // Add marker for user location
                    map.addMarker(
                        MarkerOptions()
                            .position(userLatLng)
                            .title("You are here")
                            .icon(icon)
                    )
                } else {
                    Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("MapFragment", "Error getting location", e)
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
            .actionFragmentMapFragmentToFragmentPostFragment(
                post.id,
                post.title,
                post.content,
                post.price.toFloat(),
                post.rooms.toFloat(),
                post.floor,
                post.image.toString(),
                floatArrayOf(post.location.latitude.toFloat(), post.location.longitude.toFloat())
            )

        findNavController().navigate(action)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
