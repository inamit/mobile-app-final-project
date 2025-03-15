package com.group147.appartmentblog.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import com.group147.appartmentblog.database.user.UserDao
import com.group147.appartmentblog.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRepository private constructor(private val userDao: UserDao) :
    AbsAppartmentBlogRepository<User>(userDao) {

    companion object {
        const val TAG = "UserRepository"

        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getRepository(userDao: UserDao): UserRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserRepository(userDao)
                INSTANCE = instance
                instance
            }
        }
    }

    private val _userLiveData = MutableLiveData<User>()
    val userLiveData: LiveData<User> get() = _userLiveData

    override fun streamAllExistingEntities() {
        _userLiveData.postValue(userDao.getUser())
    }

    override fun handleDocumentChanges(snapshot: QuerySnapshot) {
        CoroutineScope(Dispatchers.IO).launch {

            snapshot.documentChanges.forEach { change ->
                try {
                    val user = User.fromFirestore(change.document)

                    Log.d(PostRepository.Companion.TAG, "Processing document change: $user")
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            userDao.deleteExistingUser()
                            insert(user)
                        }

                        DocumentChange.Type.MODIFIED -> {
                            update(user)
                        }

                        DocumentChange.Type.REMOVED -> {
                            delete(user)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        PostRepository.Companion.TAG,
                        "Error processing document change: ${change.document.id}",
                        e
                    )
                }
            }

            _userLiveData.postValue(userDao.getUser())

            Log.d(
                PostRepository.Companion.TAG,
                "Processed Firestore user changes. User: ${userDao.getUser()}"
            )
        }
    }
}