package com.group147.appartmentblog.database.post

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.group147.appartmentblog.model.Post

@Dao
interface PostDao {

    @Insert
    fun insertPost(post: Post)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updatePost(post: Post)

    @Delete
    fun deletePost(post: Post)

    @Query("SELECT * FROM posts")
    fun getAllPosts(): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostById(id: Long): LiveData<Post>
}