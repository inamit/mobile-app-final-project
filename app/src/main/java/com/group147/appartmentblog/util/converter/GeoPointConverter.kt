package com.group147.appartmentblog.util.converter

import androidx.room.TypeConverter
import com.google.firebase.firestore.GeoPoint

class GeoPointConverter {

    @TypeConverter
    fun fromGeoPoint(geoPoint: GeoPoint?): String? {
        return geoPoint?.let {
            "${it.latitude},${it.longitude}"
        }
    }

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