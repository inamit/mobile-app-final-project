package com.group147.appartmentblog.screens.addApartment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.screens.MainViewModel

class AddApartmentViewModelFactory(
    private val mainViewModel: MainViewModel,
    private val postRepository: PostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddApartmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddApartmentViewModel(mainViewModel, postRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}