package com.group147.appartmentblog.database.post

import androidx.room.Dao
import androidx.room.Query
import com.group147.appartmentblog.database.IDao
import com.group147.appartmentblog.model.Post

@Dao
interface PostDao : IDao<Post> {
    @Query("SELECT * FROM posts")
    fun getAllPosts(): List<Post>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostById(id: String): Post

    @Query("SELECT MAX(updateTime) FROM posts")
    fun getLatestUpdateTime(): Long?
}