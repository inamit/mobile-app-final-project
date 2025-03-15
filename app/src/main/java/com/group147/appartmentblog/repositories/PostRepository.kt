package com.group147.appartmentblog.repositories

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.database.post.PostDao
import com.group147.appartmentblog.model.FirebaseModel
import com.group147.appartmentblog.model.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class PostRepository private constructor(
    private val postDao: PostDao
) : AbsAppartmentBlogRepository<Post>(postDao) {
    companion object {
        const val TAG = "PostRepository"

        @Volatile
        private var INSTANCE: PostRepository? = null

        fun getRepository(postDao: PostDao): PostRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = PostRepository(postDao)
                INSTANCE = instance
                instance
            }
        }
    }

    private val _postsLiveData = MutableLiveData<List<Post>>()
    val postsLiveData: LiveData<List<Post>> get() = _postsLiveData

    override fun handleDocumentChanges(snapshot: QuerySnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            val updatedPosts = mutableListOf<Post>()
            val removedPosts = mutableListOf<Post>()

            snapshot.documentChanges.forEach { change ->
                try {
                    val post = Post.fromFirestore(change.document)

                    Log.d(TAG, "Processing document change: $post")
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                            insert(post)
                            updatedPosts.add(post)
                        }

                        DocumentChange.Type.REMOVED -> {
                            delete(post)
                            removedPosts.add(post)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error processing document change: ${change.document.id}",
                        e
                    )
                }
            }

            val sortedPosts = postDao.getAllPosts().sortedByDescending { it.updateTime }
            if (sortedPosts.isNotEmpty()) {
                _postsLiveData.postValue(sortedPosts)
            } else {
                _postsLiveData.postValue(emptyList())
            }

            Log.d(
                TAG,
                "Processed Firestore changes: ${updatedPosts.size} added/modified, ${removedPosts.size} removed"
            )
        }
    }

    fun insertPost(post: Post, image: Bitmap, callback: (String?, Exception?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Uploading image for new post")
                val uid = UUID.randomUUID().toString()
                FirebaseModel.instance.uploadImage(
                    image,
                    Collections.POSTS,
                    uid
                ) { imageUrl, error ->
                    if (error != null || imageUrl == null) {
                        callback(null, error)
                        return@uploadImage
                    }

                    post.image = imageUrl

                    FirebaseModel.instance.add(Collections.POSTS, post.json) { document, error ->
                        if (error != null) {
                            FirebaseModel.instance.deleteImage(Collections.POSTS, uid) { _, error ->
                                if (error != null) {
                                    Log.d(TAG, "Failed to upload post, deleting image")
                                }

                                callback(null, error)
                            }
                            return@add
                        }

                        if (document != null) {
                            post.id = document.id
                            callback(post.id, null)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during post insertion", e)
            }
        }
    }
}