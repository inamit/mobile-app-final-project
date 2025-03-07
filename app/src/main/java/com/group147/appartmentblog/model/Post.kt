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
    val rooms: Double = 0.0,
    val location: GeoPoint,
    val imageUrl: String = ""
) {
    companion object {

        const val ID_KEY = "id"
        const val TITLE_KEY = "title"
        const val IMAGE_URL_KEY = "imageUrl"
        const val PRICE_KEY = "price"
        const val ROOMS_KEY = "rooms"
        const val LOCATION_KEY = "location"

        fun fromJSON(json: Map<String, Any>): Post {
            val id = json[ID_KEY] as? Long ?: 0
            val title = json[TITLE_KEY] as? String ?: ""
            val imageUrl = json[IMAGE_URL_KEY] as? String ?: ""
            val rooms = json[ROOMS_KEY] as? Double ?: 0.0
            val price = json[PRICE_KEY] as? Double ?: 0.0
            val location = json[LOCATION_KEY] as? GeoPoint ?: GeoPoint(0.0, 0.0)

            return Post(
                id = id,
                title = title,
                imageUrl = imageUrl,
                rooms = rooms,
                price = price,
                location = location
            )
        }
    }

    val json: HashMap<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            TITLE_KEY to title,
            IMAGE_URL_KEY to imageUrl,
            ROOMS_KEY to rooms,
            PRICE_KEY to price,
            LOCATION_KEY to location
        )
}