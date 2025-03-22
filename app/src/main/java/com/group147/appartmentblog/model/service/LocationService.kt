package com.group147.appartmentblog.model.service

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class LocationService {
    companion object {
        const val TAG = "LocationPermission"
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        fun arePermissionsGranted(grantResult: Map<String, Boolean>): Boolean {
            return LOCATION_PERMISSIONS.map { grantResult[it] }.all { it == true }
        }

        suspend fun getUserLocation(
            activity: Activity,
            requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
            fusedLocationClient: FusedLocationProviderClient
        ): Location? {
            if (ActivityCompat.checkSelfPermission(
                    activity.applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    activity.applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(LOCATION_PERMISSIONS)
                return null
            }

            return fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .await()
        }
    }
}