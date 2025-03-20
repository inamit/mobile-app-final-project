package com.group147.appartmentblog.database.comment

import androidx.room.Dao
import androidx.room.Query
import com.group147.appartmentblog.database.IDao
import com.group147.appartmentblog.model.Comment

@Dao
interface CommentDao : IDao<Comment> {

    @Query("SELECT * FROM comments")
    fun getAllComments(): List<Comment>

    @Query("DELETE FROM comments")
    fun deleteExistingComment()

    @Query("SELECT MAX(updateTime) FROM comments")
    fun getLatestUpdateTime(): Long?

}