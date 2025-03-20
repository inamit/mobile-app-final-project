package com.group147.appartmentblog.model

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.base.TaskCallback
import java.io.ByteArrayOutputStream

class FirebaseModel {
    val database = Firebase.firestore
    val storage = Firebase.storage

    companion object {
        val instance = FirebaseModel()
    }

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }
        database.firestoreSettings = settings
    }

    fun getById(
        collection: Collections,
        documentId: String,
        callback: TaskCallback<DocumentSnapshot>
    ) {
        database.collection(collection.collectionName).document(documentId)
            .get()
            .addOnSuccessListener {
                callback(it, null)
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
                callback(null, it)
            }
    }

    fun add(
        collection: Collections,
        documentId: String,
        data: Map<String, Any?>,
        callback: TaskCallback<Void>
    ) {
        database.collection(collection.collectionName)
            .document(documentId)
            .set(data)
            .addOnSuccessListener {
                callback(it, null)
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
                callback(null, it)
            }
    }

    fun add(
        collection: Collections,
        data: Map<String, Any?>,
        callback: TaskCallback<DocumentReference>,
    ): Task<DocumentReference> {
        return database.collection(collection.collectionName).add(data)
            .addOnSuccessListener {
                callback(it, null)
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
                callback(null, it)
            }
    }

    fun delete(collection: Collections, documentId: String): Task<Void> {
        return database.collection(collection.collectionName).document(documentId).delete()
    }

    fun update(
        collection: Collections,
        documentId: String,
        data: Map<String, Any?>,
        callback: TaskCallback<Void?>
    ) {
        database.collection(collection.collectionName).document(documentId)
            .update(data)
            .addOnSuccessListener {
                callback(it, null)
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
                callback(null, it)
            }
    }

    fun uploadImage(
        image: Bitmap,
        path: Collections,
        name: String,
        callback: TaskCallback<String>
    ) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("${path.collectionName}/$name")
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener { error ->
            callback(null, error)
        }.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString(), null)
            }
        }
    }

    fun deleteImage(path: Collections, name: String): Task<Void> {
        val storageRef = storage.reference
        val imageRef = storageRef.child("${path.collectionName}/$name")
        return imageRef.delete()
    }
}