package com.group147.appartmentblog.model

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.base.EmptyCallback
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

    fun add(
        collection: Collections,
        data: Map<String, Any?>,
        callback: TaskCallback<DocumentReference>,
    ) {
        database.collection(collection.collectionName).add(data)
            .addOnSuccessListener {
                callback(it, null)
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
                callback(null, it)
            }
    }

    fun delete(post: Post, callback: EmptyCallback) {
        database.collection(Collections.POSTS.collectionName).document(post.id.toString()).delete()
            .addOnSuccessListener {
                callback()
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
            }
    }

    fun update(post: Post, callback: TaskCallback<Void>) {
        database.collection(Collections.POSTS.collectionName).document(post.id.toString())
            .update(post.json)
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

    fun deleteImage(path: Collections, name: String, callback: TaskCallback<Void>) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("${path.collectionName}/$name")
        imageRef.delete()
            .addOnSuccessListener {
                callback(it, null)
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
                callback(null, it)
            }
    }
}