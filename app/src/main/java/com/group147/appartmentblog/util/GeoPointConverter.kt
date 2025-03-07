package com.group147.appartmentblog.util

import androidx.room.TypeConverter
import com.google.firebase.firestore.GeoPoint

class GeoPointConverter {

    // Convert GeoPoint to a String (you can also store it as latitude and longitude as separate fields if needed)
    @TypeConverter
    fun fromGeoPoint(geoPoint: GeoPoint?): String? {
        return geoPoint?.let {
            "${it.latitude},${it.longitude}"
        }
    }

    // Convert String back to GeoPoint
    @TypeConverter
    fun toGeoPoint(data: String?): GeoPoint? {
        return data?.let {
            val latLng = it.split(",")
            if (latLng.size == 2) {
                GeoPoint(latLng[0].toDouble(), latLng[1].toDouble())
            } else {
                null
            }
        }
    }
}
