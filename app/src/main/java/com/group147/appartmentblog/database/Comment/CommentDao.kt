package com.group147.appartmentblog.database.Comment

import androidx.room.Dao
import androidx.room.Query
import com.group147.appartmentblog.database.IDao
import com.group147.appartmentblog.model.Comment

@Dao
interface CommentDao : IDao<Comment> {

    @Query("SELECT * FROM comments WHERE postId = :postId")
    fun getCommentsByPost(postId: String): Comment?

    @Query("SELECT * FROM comments limit 1")
    fun getComment(): Comment?

    @Query("DELETE FROM comments")
    fun deleteExistingComment()

}