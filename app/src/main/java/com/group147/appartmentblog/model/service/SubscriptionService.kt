package com.group147.appartmentblog.model.service

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.base.UPDATE_TIME_KEY
import com.group147.appartmentblog.model.FirebaseModel
import com.group147.appartmentblog.repositories.AbsAppartmentBlogRepository
import com.group147.appartmentblog.repositories.PostRepository.Companion.TAG

class SubscriptionService<T>(private val repository: AbsAppartmentBlogRepository<T>) {
    private var listenerRegistration: ListenerRegistration? = null

    fun listenForCollection(collection: Collections) {
        if (listenerRegistration != null) return

        Log.d(TAG, "Listening for collection ${collection.collectionName}")
        listenerRegistration =
            FirebaseModel.instance.database.collection(collection.collectionName)
                .orderBy(UPDATE_TIME_KEY, Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(
                            TAG,
                            "Error listening for ${collection.collectionName} updates",
                            error
                        )
                        return@addSnapshotListener
                    }

                    if (snapshot == null || snapshot.documentChanges.isEmpty()) {
                        Log.d(TAG, "No changes in ${collection.collectionName}")
                        return@addSnapshotListener
                    }

                    repository.handleDocumentChanges(snapshot)
                }
    }

    fun stopListeningForCollection() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}