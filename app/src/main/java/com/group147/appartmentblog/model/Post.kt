package com.group147.appartmentblog.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.group147.appartmentblog.base.UPDATE_TIME_KEY
import java.util.Date

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey var id: String = "",
    val userId: String? = null,
    val title: String = "",
    val content: String = "",
    val price: Double = 0.0,
    val rooms: Int = 0,
    val floor: Int = 0,
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    var image: String? = null,
    val updateTime: Long = Date().time
) {
    // No-argument constructor for Firebase
    constructor() : this(
        id = "",
        userId = null,
        title = "",
        content = "",
        price = 0.0,
        rooms = 0,
        floor = 0,
        location = GeoPoint(0.0, 0.0),
        image = null,
        updateTime = Date().time
    )

    companion object {
        const val USER_ID_KEY = "userId"
        const val TITLE_KEY = "title"
        const val CONTENT_KEY = "content"
        const val PRICE_KEY = "price"
        const val ROOMS_KEY = "rooms"
        const val FLOOR_KEY = "floor"
        const val LOCATION_KEY = "location"
        const val IMAGE_URL_KEY = "imageUrl"

        fun fromFirestore(documentSnapshot: QueryDocumentSnapshot): Post {
            return Post(
                id = documentSnapshot.id,
                userId = documentSnapshot.getString(USER_ID_KEY) ?: "",
                title = documentSnapshot.getString(TITLE_KEY) ?: "",
                content = documentSnapshot.getString(CONTENT_KEY) ?: "",
                rooms = documentSnapshot.getLong(ROOMS_KEY)?.toInt() ?: 0,
                floor = documentSnapshot.getLong(FLOOR_KEY)?.toInt() ?: 0,
                price = documentSnapshot.getDouble(PRICE_KEY) ?: 0.0,
                location = documentSnapshot.getGeoPoint(LOCATION_KEY) ?: GeoPoint(0.0, 0.0),
                updateTime = documentSnapshot.getTimestamp(UPDATE_TIME_KEY)?.toDate()?.time ?: Date().time,
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