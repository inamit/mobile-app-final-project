package com.group147.appartmentblog.screens.addReview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.group147.appartmentblog.databinding.FragmentAddReviewBinding
import com.group147.appartmentblog.model.Comment
import com.group147.appartmentblog.model.User
import com.group147.appartmentblog.screens.MainActivity
import kotlin.getValue

class AddReviewFragment : Fragment() {
    private lateinit var binding: FragmentAddReviewBinding
    private lateinit var viewModel: AddReviewViewModel

    private val args: AddReviewFragmentArgs by navArgs()
    private var user: User? = null

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
                    (activity as MainActivity).getCommentRepository(),
                    (activity as MainActivity).getUserRepository(),
                )
            )[AddReviewViewModel::class.java]
        user = viewModel.user.value
        binding.saveButton.setOnClickListener {
            val comment = args.toComment()
            viewModel.saveComment(comment) {
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
        (activity as MainActivity).hideToolbarNavigationIcon()
    }

    private fun AddReviewFragmentArgs.toComment(): Comment {
        return Comment(
            postId = postId,
            authorName = user?.displayName ?: "" ,
            review = binding.reviewEditText.text.toString(),
            rate = binding.ratingBar.rating.toDouble(),
            )
    }
}