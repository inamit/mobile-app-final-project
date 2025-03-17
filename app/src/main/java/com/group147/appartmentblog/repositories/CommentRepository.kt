package com.group147.appartmentblog.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.group147.appartmentblog.base.Collections
import com.group147.appartmentblog.base.TaskCallback
import com.group147.appartmentblog.database.Comment.CommentDao
import com.group147.appartmentblog.model.Comment
import com.group147.appartmentblog.model.FirebaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommentRepository private constructor(private val commentDao: CommentDao) :
    AbsAppartmentBlogRepository<Comment>(commentDao) {

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

    private val _commentLiveData = MutableLiveData<Comment>()
    val commentLiveData: LiveData<Comment> get() = _commentLiveData

    override fun streamAllExistingEntities() {
        CoroutineScope(Dispatchers.IO).launch {
            _commentLiveData.postValue(commentDao.getComment())
        }
    }

    override fun handleDocumentsChanges(snapshot: QuerySnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            snapshot.documentChanges.forEach { change ->
                try {
                    val comment = Comment.fromFirestore(change.document)

                    Log.d(TAG, "Processing document change: $comment")
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            commentDao.deleteExistingComment()
                            insert(comment)
                        }

                        DocumentChange.Type.MODIFIED -> {
                            update(comment)
                        }

                        DocumentChange.Type.REMOVED -> {
                            delete(comment)
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

            _commentLiveData.postValue(commentDao.getComment())

            Log.d(
                TAG,
                "Processed Firestore comment changes. Comment: ${commentDao.getComment()}"
            )
        }
    }

    override fun handleDocumentChange(snapshot: DocumentSnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Processing document change: $snapshot")
            val comment = Comment.fromFirestore(snapshot)

            if (commentDao.getComment() == null) {
                insert(comment)
            } else {
                update(comment)
            }

            _commentLiveData.postValue(commentDao.getComment())
        }
    }

    fun deleteComment() {
        CoroutineScope(Dispatchers.IO).launch {
            commentDao.deleteExistingComment()
        }
    }

    fun insertComment(comment: Comment, callback: TaskCallback<String>) {

            insertCommentToFirebase(comment) { commentId, error ->
                if (error != null) {
                    callback(null, error)
                    return@insertCommentToFirebase
                }

                callback(commentId, null)
            }

    }

    private fun insertCommentToFirebase(comment: Comment, callback: TaskCallback<String>) {
        FirebaseModel.instance.add(
            Collections.COMMENTS,
            comment.id,
            comment.json
        ) { documentReference, error ->
            if (error != null) {
                callback(null, error)
                return@add
            }

            CoroutineScope(Dispatchers.IO).launch {
                if (commentDao.getComment() == null) {
                    insert(comment)
                } else {
                    update(comment)
                }
            }
            callback(comment.id, null)
        }
    }

    private fun updateCommentInFirestore(
        comment: Comment,
        callback: TaskCallback<Void?>
    ) {
        FirebaseModel.instance.update(
            Collections.COMMENTS, comment.id, comment.json
        ) { _, error ->
            if (error != null) {
                callback(null, error)
                return@update
            }

            CoroutineScope(Dispatchers.IO).launch {
                update(comment)
                callback(null, null)
            }
        }
    }
}