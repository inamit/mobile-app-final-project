package com.group147.appartmentblog.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.database.comment.CommentDao

import com.group147.appartmentblog.model.Comment
import com.group147.appartmentblog.model.FirebaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommentRepository private constructor(
    private val commentDao: CommentDao
) : AbsAppartmentBlogRepository<Comment>(commentDao) {
    companion object {
        const val TAG = "CommentRepository"

        @Volatile
        private var INSTANCE: CommentRepository? = null

        fun getRepository(commentDao: CommentDao): CommentRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = CommentRepository(commentDao)
                INSTANCE = instance
                instance
            }
        }
    }

    private val _commentsLiveData = MutableLiveData<List<Comment>>()
    val commentsLiveData: LiveData<List<Comment>> get() = _commentsLiveData

    override fun streamAllExistingEntities() {
        CoroutineScope(Dispatchers.IO).launch {
            val comments = commentDao.getAllComments()
            Log.d(TAG, "Comments from local DB: $comments")
            _commentsLiveData.postValue(comments)
        }
    }

    override fun handleDocumentsChanges(snapshot: QuerySnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            val updatedComments = mutableListOf<Comment>()
            val removedComments = mutableListOf<Comment>()

            snapshot.documentChanges.forEach { change ->
                try {
                    val comment = Comment.fromFirestore(change.document)

                    Log.d(TAG, "Processing document change (${change.type}): $comment")
                    when (change.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                            try {
                                insert(comment)
                            } catch (_: Exception) {
                                update(comment)
                            }
                            updatedComments.add(comment)
                        }

                        DocumentChange.Type.REMOVED -> {
                            delete(comment)
                            removedComments.add(comment)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing document change: ${change.document.id}", e)
                }
            }

            commentSortedComments()

            Log.d(
                TAG,
                "Processed Firestore changes: ${updatedComments.size} added/modified, ${removedComments.size} removed"
            )
        }
    }

    override fun handleDocumentChange(snapshot: DocumentSnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Processing document change: $snapshot")
            val comment = Comment.fromFirestore(snapshot)
            update(comment)
            commentSortedComments()
        }
    }

    private fun commentSortedComments() {
        val sortedComments = commentDao.getAllComments().sortedByDescending { it.updateTime }
        if (sortedComments.isNotEmpty()) {
            _commentsLiveData.postValue(sortedComments)
        } else {
            _commentsLiveData.postValue(emptyList())
        }
    }

    fun insertComment(comment: Comment, callback: (String?, Exception?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseModel.instance.add(Collections.COMMENTS, comment.json) { document, error ->
                if (document != null) {
                    comment.id = document.id
                    callback(comment.id, null)
                }
            }
        }
    }

    fun getLatestUpdatedTime(): Long {
        return commentDao.getLatestUpdateTime() ?: 0
    }

}