package com.group147.appartmentblog.model

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group147.appartmentblog.base.Constants
import com.group147.appartmentblog.base.EmptyCallback
import com.group147.appartmentblog.base.PostsCallback
import java.io.ByteArrayOutputStream

class FirebaseModel {
    private val database = Firebase.firestore
    private val storage = Firebase.storage

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings { })
        }
        database.firestoreSettings = settings
    }

    fun getAllPosts(callback: PostsCallback) {
        database.collection(Constants.Collections.POSTS)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val posts: MutableList<Post> = mutableListOf()
                        for (json in it.result) {
                            posts.add(Post.fromJSON(json.data))
                        }
                        Log.d("TAG", posts.size.toString())
                        callback(posts)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun add(post: Post, callback: EmptyCallback) {
        database.collection(Constants.Collections.POSTS).document(post.id.toString()).set(post.json)
            .addOnCompleteListener {
                callback()
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
            }
    }

    fun delete(post: Post, callback: EmptyCallback) {
        database.collection(Constants.Collections.POSTS).document(post.id.toString()).delete()
            .addOnCompleteListener {
                callback()
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
            }
    }

    fun update(post: Post, callback: EmptyCallback) {
        database.collection(Constants.Collections.POSTS).document(post.id.toString()).update(post.json)
            .addOnCompleteListener {
                callback()
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
            }
    }

    fun uploadImage(image: Bitmap, name: String, callback: (String?) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/$name.jpg")
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            callback(null)
        }.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }
        }
    }
}