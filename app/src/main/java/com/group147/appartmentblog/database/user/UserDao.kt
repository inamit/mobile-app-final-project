package com.group147.appartmentblog.database.user

import androidx.room.Dao
import androidx.room.Query
import com.group147.appartmentblog.database.IDao
import com.group147.appartmentblog.model.User

@Dao
interface UserDao : IDao<User> {
    @Query("SELECT * FROM users limit 1")
    fun getUser(): User?

    @Query("DELETE FROM users")
    fun deleteExistingUser()
}