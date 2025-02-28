package com.group147.appartmentblog.screens.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.group147.appartmentblog.R
import kotlin.collections.set

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap

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
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        //todo: get the user location
        val userLocation = LatLng(37.7749, -122.4194)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12f))

        showPosts()
    }

    fun resizeMapIcon(imageRes: Int, width: Int, height: Int): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(context?.resources, imageRes)
        val smallBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        return BitmapDescriptorFactory.fromBitmap(smallBitmap)
    }

    fun showPosts() {
        val locations = listOf(
            LocationData("Place 1", LatLng(37.7749, -122.4194)),
            LocationData("Place 2", LatLng(40.7128, -74.0060)),
            LocationData("Place 3", LatLng(34.0522, -118.2437))
        )
        val markerMap = mutableMapOf<Marker, LocationData>()

        for (location in locations) {
            val marker = map.addMarker(
                MarkerOptions()
                    .position(location.latLng)
                    .title(location.name)
                    .icon(resizeMapIcon(R.drawable.location_icon, 200, 200))
            )
            marker?.let { markerMap[it] = location } // Store the marker and its data
        }
        map.setOnMarkerClickListener { marker ->
            val locationData = markerMap[marker]
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            marker.showInfoWindow()
            locationData?.let {
                openDetailsScreen(it)
            }
            true
        }
    }

    // Function to open details screen of post
    fun openDetailsScreen(locationData: LocationData) {
        //  val intent = Intent(this, PostActivity::class.java)
        //  startActivity(intent)
    }
}
//removed when posts exist
data class LocationData(val name: String, val latLng: LatLng)