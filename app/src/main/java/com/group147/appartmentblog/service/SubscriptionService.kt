package com.group147.appartmentblog.service

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.base.UPDATE_TIME_KEY
import com.group147.appartmentblog.model.FirebaseModel
import com.group147.appartmentblog.repositories.AbsAppartmentBlogRepository
import java.util.Date

class SubscriptionService<T>(private val repository: AbsAppartmentBlogRepository<T>) {
    companion object {
        val TAG = "SubscriptionService"
    }

    private var listenerRegistration: ListenerRegistration? = null

    fun listenForCollection(collection: Collections, fromDate: Long) {
        if (listenerRegistration != null) return

        Log.d(TAG, "Listening for collection ${collection.collectionName} from ${Date(fromDate)}")
        listenerRegistration =
            FirebaseModel.instance.database.collection(collection.collectionName)
                .whereGreaterThan(UPDATE_TIME_KEY, Timestamp(Date(fromDate)))
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
                        repository.streamAllExistingEntities()
                        return@addSnapshotListener
                    }

                    repository.handleDocumentsChanges(snapshot)
                }
    }

    fun listenForEntity(collection: Collections, entityId: String) {
        if (listenerRegistration != null) return

        Log.d(TAG, "Listening for entity $entityId in collection ${collection.collectionName}")
        listenerRegistration =
            FirebaseModel.instance.database.collection(collection.collectionName)
                .document(entityId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(
                            TAG,
                            "Error listening for ${collection.collectionName} updates",
                            error
                        )
                        return@addSnapshotListener
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        Log.d(TAG, "No changes in ${collection.collectionName}")
                        repository.streamAllExistingEntities()
                        return@addSnapshotListener
                    }

                    repository.handleDocumentChange(snapshot)
                }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}