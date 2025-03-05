package com.group147.appartmentblog.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.group147.appartmentblog.util.checkSelfPermissionCompat
import com.group147.appartmentblog.util.requestPermissionsCompat
import com.group147.appartmentblog.util.shouldShowRequestPermissionRationaleCompat
import com.group147.appartmentblog.util.showSnackbar

class LocationPermission {
    companion object {
        const val TAG = "LocationPermission"
        fun checkLocationPermission(activity: AppCompatActivity, view: View, permissionLauncher: ActivityResultLauncher<String>): Boolean {
            if (activity.checkSelfPermissionCompat(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "requesting location permission")
                if (activity.shouldShowRequestPermissionRationaleCompat(Manifest.permission.CAMERA)) {
                    view.showSnackbar(
                        "Location permission is required to upload a post",
                        Snackbar.LENGTH_INDEFINITE, "OK"
                    ) {
                        activity.requestPermissionsCompat(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            permissionLauncher
                        )
                    }

                }
                activity.requestPermissionsCompat(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    permissionLauncher
                )
                return false
            }

            return true
        }
    }
}