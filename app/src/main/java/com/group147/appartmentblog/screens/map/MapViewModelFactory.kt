package com.group147.appartmentblog.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group147.appartmentblog.repositories.PostRepository

class MapViewModelFactory (private val postRepository: PostRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(postRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}