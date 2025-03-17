package com.group147.appartmentblog.repositories

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.base.TaskCallback
import com.group147.appartmentblog.database.post.PostDao
import com.group147.appartmentblog.model.FirebaseModel
import com.group147.appartmentblog.model.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    fun getLatestUpdatedTime(): Long {
        return postDao.getLatestUpdateTime() ?: 0
    }

    override fun streamAllExistingEntities() {
        CoroutineScope(Dispatchers.IO).launch {
            _postsLiveData.postValue(postDao.getAllPosts())
        }
    }

    override fun handleDocumentsChanges(snapshot: QuerySnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            val updatedPosts = mutableListOf<Post>()
            val removedPosts = mutableListOf<Post>()

            snapshot.documentChanges.forEach { change ->
                try {
                    val post = Post.fromFirestore(change.document)
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                            postDao.insert(post)
                            updatedPosts.add(post)
                        }
                        DocumentChange.Type.REMOVED -> {
                            postDao.delete(post)
                            removedPosts.add(post)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing document change: ${change.document.id}", e)
                }
            }

            postSortedPosts()

            Log.d(TAG, "Processed Firestore changes: ${updatedPosts.size} added/modified, ${removedPosts.size} removed")
        }
    }

    override fun handleDocumentChange(snapshot: DocumentSnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Processing document change: $snapshot")
            val post = Post.fromFirestore(snapshot)

            if (postDao.getPostById(post.id) == null) {
                postDao.insert(post)
            } else {
                postDao.update(post)
            }

            _postsLiveData.postValue(postDao.getAllPosts())
        }
    }

    private fun postSortedPosts() {
        val sortedPosts = postDao.getAllPosts().sortedByDescending { it.updateTime }
        if (sortedPosts.isNotEmpty()) {
            _postsLiveData.postValue(sortedPosts)
        } else {
            _postsLiveData.postValue(emptyList())
        }
    }

    fun updatePost(post: Post, image: Bitmap?, callback: TaskCallback<Void?>) {
        if (image != null) {
            FirebaseModel.instance.uploadImage(image, Collections.POSTS, post.id) { imageUrl, error ->
                if (error != null || imageUrl == null) {
                    callback(null, error)
                    return@uploadImage
                }

                post.image = imageUrl
                updatePostInFirestore(post, callback)
            }
        } else {
            updatePostInFirestore(post, callback)
        }
    }

    private fun updatePostInFirestore(post: Post, callback: TaskCallback<Void?>) {
        FirebaseModel.instance.update(Collections.POSTS, post.id, post.json) { _, error ->
            if (error != null) {
                callback(null, error)
                return@update
            }

            CoroutineScope(Dispatchers.IO).launch {
                postDao.update(post)
                callback(null, null)
            }
        }
    }

    fun insertPost(post: Post, image: Bitmap?, callback: TaskCallback<String>) {
        if (image != null) {
            FirebaseModel.instance.uploadImage(image, Collections.POSTS, post.id) { imageUrl, error ->
                if (error != null) {
                    callback(null, error)
                    return@uploadImage
                }

                post.image = imageUrl
                insertPostInFirestore(post, callback)
            }
        } else {
            insertPostInFirestore(post, callback)
        }
    }

    private fun insertPostInFirestore(post: Post, callback: TaskCallback<String>) {
        FirebaseModel.instance.add(Collections.POSTS, post.id, post.json) { documentReference, error ->
            if (error != null) {
                callback(null, error)
                return@add
            }

            CoroutineScope(Dispatchers.IO).launch {
                postDao.insert(post)
                callback(post.id, null)
            }
        }
    }
}