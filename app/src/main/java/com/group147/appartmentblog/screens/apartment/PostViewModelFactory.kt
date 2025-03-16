package com.group147.appartmentblog.screens.apartment


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group147.appartmentblog.databinding.FragmentAddApartmentBinding
import com.group147.appartmentblog.databinding.FragmentPostBinding
import com.group147.appartmentblog.repositories.PostRepository
import com.group147.appartmentblog.screens.addApartment.AddApartmentViewModel

class PostViewModelFactory( private val binding: FragmentPostBinding) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(binding) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}