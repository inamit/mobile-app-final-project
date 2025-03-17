package com.group147.appartmentblog.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.group147.appartmentblog.base.UPDATE_TIME_KEY
import java.util.Date

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey var id: String = "",
    val userId: String?,
    val title: String,
    val content: String,
    val price: Double,
    val rooms: Int,
    val floor: Int,
    val location: GeoPoint,
    var image: String? = null,
    val updateTime: Long = Date().time
) {

    companion object {
        const val USER_ID_KEY = "userId"
        const val TITLE_KEY = "title"
        const val CONTENT_KEY = "content"
        const val PRICE_KEY = "price"
        const val ROOMS_KEY = "rooms"
        const val FLOOR_KEY = "floor"
        const val LOCATION_KEY = "location"
        const val IMAGE_URL_KEY = "imageUrl"


        fun fromFirestore(documentSnapshot: DocumentSnapshot): Post {
            return Post(
                id = documentSnapshot.id,
                userId = documentSnapshot.getString(USER_ID_KEY) ?: "",
                title = documentSnapshot.getString(TITLE_KEY) ?: "",
                content = documentSnapshot.getString(CONTENT_KEY) ?: "",
                rooms = documentSnapshot.getLong(ROOMS_KEY)?.toInt() ?: 0,
                floor = documentSnapshot.getLong(FLOOR_KEY)?.toInt() ?: 0,
                price = documentSnapshot.getDouble(PRICE_KEY) ?: 0.0,
                location = documentSnapshot.getGeoPoint(LOCATION_KEY) ?: GeoPoint(0.0, 0.0),
                updateTime = documentSnapshot.getTimestamp(UPDATE_TIME_KEY)?.toDate()?.time
                    ?: Date().time,
                image = documentSnapshot.getString(IMAGE_URL_KEY) ?: ""
            )
        }
    }

    val json: HashMap<String, Any?>
        get() = hashMapOf(
            USER_ID_KEY to userId,
            TITLE_KEY to title,
            CONTENT_KEY to content,
            ROOMS_KEY to rooms,
            FLOOR_KEY to floor,
            PRICE_KEY to price,
            LOCATION_KEY to location,
            UPDATE_TIME_KEY to Timestamp(Date(updateTime)),
            IMAGE_URL_KEY to image
        )
}