package com.group147.appartmentblog.repositories

import androidx.lifecycle.LiveData
import com.group147.appartmentblog.database.post.PostDao
import com.group147.appartmentblog.model.Post

class PostRepository(private val postDao: PostDao) {
    val allPosts: LiveData<List<Post>> = postDao.getAllPosts()

    fun insertPost(post: Post) {
        postDao.insertPost(post)
    }

    fun updatePost(post: Post) {
        postDao.updatePost(post)
    }

    fun deletePost(post: Post) {
        postDao.deletePost(post)
    }

    fun getPostById(post: Long) {
        postDao.getPostById(post)
    }

}