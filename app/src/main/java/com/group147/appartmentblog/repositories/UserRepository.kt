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
import com.group147.appartmentblog.database.user.UserDao
import com.group147.appartmentblog.model.FirebaseModel
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
        CoroutineScope(Dispatchers.IO).launch {
            _userLiveData.postValue(userDao.getUser())
        }
    }

    override fun handleDocumentsChanges(snapshot: QuerySnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            snapshot.documentChanges.forEach { change ->
                try {
                    val user = User.fromFirestore(change.document)

                    Log.d(TAG, "Processing document change: $user")
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
                        TAG,
                        "Error processing document change: ${change.document.id}",
                        e
                    )
                }
            }

            _userLiveData.postValue(userDao.getUser())

            Log.d(
                TAG,
                "Processed Firestore user changes. User: ${userDao.getUser()}"
            )
        }
    }

    override fun handleDocumentChange(snapshot: DocumentSnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Processing document change: $snapshot")
            val user = User.fromFirestore(snapshot)

            if (userDao.getUser() == null) {
                insert(user)
            } else {
                update(user)
            }

            _userLiveData.postValue(userDao.getUser())
        }
    }

    fun deleteUser() {
        CoroutineScope(Dispatchers.IO).launch {
            userDao.deleteExistingUser()
        }
    }

    fun insertUser(user: User, image: Bitmap?, callback: TaskCallback<Void>) {
        if (image != null) {
            FirebaseModel.instance.uploadImage(
                image,
                Collections.USERS,
                user.id
            ) { imageUrl, error ->
                if (error != null) {
                    callback(null, error)
                    return@uploadImage
                }

                val user = User(
                    id = user.id,
                    email = user.email,
                    phoneNumber = user.phoneNumber,
                    displayName = user.displayName,
                    imageUrl = imageUrl
                )
                insertUserToFirebase(user) { userId, error ->
                    if (error != null) {
                        callback(null, error)
                        return@insertUserToFirebase
                    }

                    callback(null, null)
                }
            }
        } else {
            val user = User(
                id = user.id,
                email = user.email,
                phoneNumber = user.phoneNumber,
                displayName = user.displayName
            )
            insertUserToFirebase(user) { userId, error ->
                if (error != null) {
                    callback(null, error)
                    return@insertUserToFirebase
                }

                callback(null, null)
            }
        }
    }

    private fun insertUserToFirebase(user: User, callback: TaskCallback<String>) {
        FirebaseModel.instance.add(
            Collections.USERS,
            user.id,
            user.json
        ) { documentReference, error ->
            if (error != null) {
                callback(null, error)
                return@add
            }

            CoroutineScope(Dispatchers.IO).launch {
                if (userDao.getUser() == null) {
                    insert(user)
                } else {
                    update(user)
                }
            }
            callback(user.id, null)
        }
    }

    fun updateUser(user: User, image: Bitmap?, callback: TaskCallback<Void?>) {
        if (image != null) {
            FirebaseModel.instance.uploadImage(
                image,
                Collections.USERS,
                user.id
            ) { imageUrl, error ->
                if (error != null || imageUrl == null) {
                    callback(null, error)
                    return@uploadImage
                }

                user.imageUrl = imageUrl

                updateUserInFirestore(user, callback)
            }
        } else {
            updateUserInFirestore(user, callback)
        }
    }

    private fun updateUserInFirestore(
        user: User,
        callback: TaskCallback<Void?>
    ) {
        FirebaseModel.instance.update(
            Collections.USERS, user.id, user.json
        ) { _, error ->
            if (error != null) {
                callback(null, error)
                return@update
            }

            CoroutineScope(Dispatchers.IO).launch {
                update(user)
                callback(null, null)
            }
        }
    }
}