package com.group147.appartmentblog.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.group147.appartmentblog.R

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Request user location
        getUserLocation()

        showPosts()
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

                    // Add marker for user location
                    map.addMarker(
                        MarkerOptions()
                            .position(userLatLng)
                            .title("You are here")
                            .icon(resizeMapIcon(R.drawable.user_location_icon, 150, 150))
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

    fun showPosts() {
        map.clear()
        val markerMap = mutableMapOf<Marker, LatLng>()
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val title = document.getString("title")
                    val location =
                        document.getGeoPoint("location")  // Get the GeoPoint from Firestore

                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude

                        Log.d(
                            "MapFragment",
                            "Title: $title, Latitude: $latitude, Longitude: $longitude"
                        )

                        val position = LatLng(latitude, longitude)

                        // Add marker to the map
                        val marker = map.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(title)
                                .icon(resizeMapIcon(R.drawable.location_icon, 200, 200))
                        )
                        marker?.let { markerMap[it] = position }

                        map.setOnMarkerClickListener { marker ->
                            val locationData = markerMap[marker]
                            marker.showInfoWindow()
                            locationData?.let {
                                openDetailsScreen(it)
                            }
                            true
                        }
                    } else {
                        Log.e("MapFragment", "Invalid location in document: ${document.id}")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MapFragment", "Error getting documents: ", exception)
            }
    }

    private fun resizeMapIcon(imageRes: Int, width: Int, height: Int): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(context?.resources, imageRes)
        val smallBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        return BitmapDescriptorFactory.fromBitmap(smallBitmap)
    }

    // Function to open details screen of post
    private fun openDetailsScreen(locationData: LatLng) {
        // TODO: Implement navigation to post details screen
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
