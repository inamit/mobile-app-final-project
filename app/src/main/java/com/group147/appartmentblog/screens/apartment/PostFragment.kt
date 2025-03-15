package com.group147.appartmentblog.screens.apartment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.firestore.GeoPoint
import com.group147.appartmentblog.databinding.FragmentPostBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.R
import com.group147.appartmentblog.screens.home.HomeActivity
import kotlinx.coroutines.launch
import java.util.Date

class PostFragment : Fragment() {

    private lateinit var binding: FragmentPostBinding
    private val viewModel: PostViewModel by viewModels()
    private val args: PostFragmentArgs by navArgs()

    private var isEditMode = false

    private lateinit var titleTextView: TextView
    private lateinit var titleEditText: EditText
    private lateinit var contentTextView: TextView
    private lateinit var contentEditText: EditText
    private lateinit var priceTextView: TextView
    private lateinit var priceEditText: EditText
    private lateinit var roomsTextView: TextView
    private lateinit var roomsEditText: EditText
    private lateinit var floorTextView: TextView
    private lateinit var floorEditText: EditText
    private lateinit var addressTextView: TextView
    private lateinit var editButton: Button
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        (activity as HomeActivity).hideBottomNavBar()
        (activity as HomeActivity).hideAddApartmentButton()
        (activity as HomeActivity).showToolbarNavigationIcon()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val post = args.toPost()
        viewModel.setPost(post)

        observePost()
        setupEditButton()
        initViews(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (activity as HomeActivity).showBottomNavBar()
        (activity as HomeActivity).showAddApartmentButton()
        (activity as HomeActivity).hideToolbarNavigationIcon()
    }

    private fun initViews(view: View) {
        titleTextView = view.findViewById(R.id.titleTextView)
        titleEditText = view.findViewById(R.id.titleEditText)
        contentTextView = view.findViewById(R.id.contentTextView)
        contentEditText = view.findViewById(R.id.contentEditText)
        priceTextView = view.findViewById(R.id.priceTextView)
        priceEditText = view.findViewById(R.id.priceEditText)
        roomsTextView = view.findViewById(R.id.roomsTextView)
        roomsEditText = view.findViewById(R.id.roomsEditText)
        floorTextView = view.findViewById(R.id.floorTextView)
        floorEditText = view.findViewById(R.id.floorEditText)
        addressTextView = view.findViewById(R.id.addressTextView)
        editButton = view.findViewById(R.id.editButton)
        saveButton = view.findViewById(R.id.saveButton)
    }

    private fun observePost() {
        viewModel.post.observe(viewLifecycleOwner) { post ->
            bindPostData(post)
        }
    }

    private fun bindPostData(post: Post) {
         viewLifecycleOwner.lifecycleScope.launch {
            var address =viewModel.getAddressFromGeo(post)
            binding.addressTextView.text = "Address: ${address}"
        }

        binding.apply {
            titleTextView.text = post.title
            contentTextView.text = post.content
            priceTextView.text = "Price: ${post.price}$"
            roomsTextView.text = "Number of Rooms: ${post.rooms}"
            floorTextView.text = "Floor: ${post.floor}"


            post.image?.let {
                Glide.with(this@PostFragment)
                    .load(it)
                    .placeholder(R.drawable.account_icon)
                    .into(postImageView)
            }

            titleEditText.setText(post.title)
            contentEditText.setText(post.content)
            priceEditText.setText(post.price.toString())
            roomsEditText.setText(post.rooms.toString())
            floorEditText.setText(post.floor.toString())
        }
    }

    private fun setupEditButton() {
        binding.editButton.setOnClickListener {
            toggleEditMode(true)
        }

        binding.saveButton.setOnClickListener {
            if (validateInput()) {
                updatePost()
                toggleEditMode(false)
            }
        }
    }

    private fun toggleEditMode(editMode: Boolean) {
        isEditMode = editMode

        titleTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        contentTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        priceTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        roomsTextView.visibility = if (editMode) View.GONE else View.VISIBLE
        floorTextView.visibility = if (editMode) View.GONE else View.VISIBLE

        titleEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        contentEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        priceEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        roomsEditText.visibility = if (editMode) View.VISIBLE else View.GONE
        floorEditText.visibility = if (editMode) View.VISIBLE else View.GONE

        editButton.visibility = if (editMode) View.GONE else View.VISIBLE
        saveButton.visibility = if (editMode) View.VISIBLE else View.GONE
    }

    private fun updatePost() {
        val updatedPost = viewModel.post.value?.copy(
            title = binding.titleEditText.text.toString(),
            content = binding.contentEditText.text.toString(),
            price = binding.priceEditText.text.toString().toDoubleOrNull() ?: 0.0,
            rooms = binding.roomsEditText.text.toString().toIntOrNull() ?: 0,
            floor = binding.floorEditText.text.toString().toIntOrNull() ?: 0,
            updateTime = Date().time
        )

        updatedPost?.let {
            viewModel.updatePost(it)
        }
    }

    private fun validateInput(): Boolean {
        return binding.titleEditText.text.isNotEmpty() &&
                binding.contentEditText.text.isNotEmpty() &&
                binding.priceEditText.text.isNotEmpty() &&
                binding.roomsEditText.text.isNotEmpty() &&
                binding.floorEditText.text.isNotEmpty()
    }

    private fun PostFragmentArgs.toPost(): Post {
        return Post(
            id = this.id,
            title = this.title,
            content = this.content,
            price = this.price.toDouble(),
            rooms = this.rooms.toInt(),
            floor = this.floor,
            location = GeoPoint(location[0].toDouble(), location[1].toDouble()),
            image = this.image,
            updateTime = Date().time,
            userId = this.id
        )
    }
}
