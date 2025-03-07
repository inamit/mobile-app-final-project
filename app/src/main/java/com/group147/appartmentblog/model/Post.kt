package com.group147.appartmentblog.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.GeoPoint
import com.group147.appartmentblog.util.GeoPointConverter


@Entity(tableName = "posts")
@TypeConverters(GeoPointConverter::class)
data class Post(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val title: String = "",
    val price: Double = 0.0,
    val rooms: Int = 0,
    val location: GeoPoint
)