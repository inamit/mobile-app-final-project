package com.group147.appartmentblog.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentSnapshot

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey var id: String = "",
    val postId: String,
    val authorName: String,
    val review: String,
    val rate: Double,
) {
    companion object {
        const val POST_ID_KEY = "postId"
        const val AUTHOR_NAME_KEY = "authorName"
        const val REVIEW_KEY = "review"
        const val RATE_KEY = "rate"

        fun fromFirestore(documentSnapshot: DocumentSnapshot): Comment {
            return Comment(
                id = documentSnapshot.id,
                postId = documentSnapshot.getString(POST_ID_KEY) ?: "",
                authorName = documentSnapshot.getString(AUTHOR_NAME_KEY) ?: "",
                review = documentSnapshot.getString(REVIEW_KEY) ?: "",
                rate = documentSnapshot.getDouble(RATE_KEY) ?: 0.0
            )
        }
    }

    val json: HashMap<String, Any?>
        get() = hashMapOf(
            POST_ID_KEY to postId,
            AUTHOR_NAME_KEY to authorName,
            REVIEW_KEY to review,
            RATE_KEY to rate
        )
}

