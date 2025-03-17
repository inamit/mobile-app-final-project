package com.group147.appartmentblog.screens.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.repositories.PostRepository

class FeedViewModel(postRepository: PostRepository) : ViewModel() {
    val allPosts: LiveData<List<Post>> = postRepository.postsLiveData
    val loadingPosts: LiveData<Boolean> = postRepository.loadingPostsLiveData
}