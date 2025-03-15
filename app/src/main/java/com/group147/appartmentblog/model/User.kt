package com.group147.appartmentblog.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.QueryDocumentSnapshot

@Entity(tableName = "posts")
data class User(
    @PrimaryKey val id: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val displayName: String = "",
    val imageUrl: String? = ""
) {
    companion object {
        const val EMAIL_KEY = "email"
        const val PHONE_NUMBER_KEY = "phoneNumber"
        const val DISPLAY_NAME_KEY = "displayName"
        const val IMAGE_URL_KEY = "imageUrl"

        fun fromFirestore(documentSnapshot: QueryDocumentSnapshot): User {
            return User(
                id = documentSnapshot.id,
                email = documentSnapshot.getString(EMAIL_KEY) ?: "",
                phoneNumber = documentSnapshot.getString(PHONE_NUMBER_KEY) ?: "",
                displayName = documentSnapshot.getString(DISPLAY_NAME_KEY) ?: "",
                imageUrl = documentSnapshot.getString(IMAGE_URL_KEY) ?: ""
            )
        }
    }

    val json: HashMap<String, Any?>
        get() = hashMapOf(
            EMAIL_KEY to email,
            PHONE_NUMBER_KEY to phoneNumber,
            DISPLAY_NAME_KEY to displayName,
            IMAGE_URL_KEY to imageUrl
        )
}
