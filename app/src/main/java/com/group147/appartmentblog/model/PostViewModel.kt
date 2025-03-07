package com.group147.appartmentblog.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.group147.appartmentblog.model.service.dao.PostDatabase
import com.group147.appartmentblog.repositories.PostRepository
import kotlinx.coroutines.launch

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepository(PostDatabase.getDatabase(application).postDao())

    // LiveData to observe posts
    val allPosts: LiveData<List<Post>> = repository.allPosts

    fun addPost(post: Post) {
        viewModelScope.launch {
            repository.insertPost(post)
        }
    }
}
