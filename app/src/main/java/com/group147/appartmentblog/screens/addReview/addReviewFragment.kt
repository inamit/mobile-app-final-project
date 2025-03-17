package com.group147.appartmentblog.screens.addReview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.group147.appartmentblog.databinding.FragmentAddReviewBinding
import com.group147.appartmentblog.screens.MainActivity

class AdReviewFragment : Fragment() {
    private lateinit var binding: FragmentAddReviewBinding
    private lateinit var viewModel: AddReviewViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddReviewBinding.inflate(inflater, container, false)

        (activity as MainActivity).hideBottomNavBar()
        (activity as MainActivity).hideAddApartmentButton()
        (activity as MainActivity).showToolbarNavigationIcon()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProvider(
                requireActivity(),
                AddReviewViewModelFactory(
                    binding,
                    (activity as MainActivity).getCommentRepository()
                )
            )[AddReviewViewModel::class.java]
        binding.saveButton.setOnClickListener {
            viewModel.saveComment() {
                findNavController().popBackStack()
            }
        }
        viewModel.toastMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).showBottomNavBar()
        (activity as MainActivity).showAddApartmentButton()
        (activity as MainActivity).hideToolbarNavigationIcon()
    }

}