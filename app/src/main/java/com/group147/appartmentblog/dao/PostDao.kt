package com.group147.appartmentblog.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.group147.appartmentblog.model.Post

@Dao
interface PostDao {
    // Insert a new Post
    @Insert
    fun insertPost(post: Post)

    // Get all posts
    @Query("SELECT * FROM posts")
    fun getAllPosts(): LiveData<List<Post>>

}