package com.group147.appartmentblog.screens.addApartment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group147.appartmentblog.databinding.FragmentAddApartmentBinding
import com.group147.appartmentblog.repositories.PostRepository

class AddApartmentViewModelFactory(
    private val binding: FragmentAddApartmentBinding,
    private val postRepository: PostRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddApartmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddApartmentViewModel(binding, postRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}