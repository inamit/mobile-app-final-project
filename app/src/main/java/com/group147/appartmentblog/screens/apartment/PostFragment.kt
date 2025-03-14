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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.GeoPoint
import com.group147.appartmentblog.databinding.FragmentPostBinding
import com.group147.appartmentblog.model.Post
import com.group147.appartmentblog.R
import java.util.Date

class PostFragment : Fragment() {

    private lateinit var binding: FragmentPostBinding
    private val viewModel: PostViewModel by viewModels()

    // Declare a variable to track edit mode
    private var isEditMode = false

    // Views
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
    private lateinit var editButton: Button
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val post = arguments?.let { bundleToPost(it) }
        post?.let { viewModel.setPost(it) }

        observePost()
        setupEditButton()
        initViews(view)
        setupBackButton()
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
        editButton = view.findViewById(R.id.editButton)
        saveButton = view.findViewById(R.id.saveButton)
    }

    private fun observePost() {
        viewModel.post.observe(viewLifecycleOwner) { post ->
            bindPostData(post)
        }
    }

    private fun bindPostData(post: Post) {
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

            // Pre-populate EditText fields in edit mode
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
            Log.d("dddddddddddddddddddddddddddddddddddd","dddddd")
        }

        binding.editButton.setOnClickListener {
            if (validateInput()) {
                updatePost()
                toggleEditMode(false)
                Log.d("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","aaaaaaaaaaaaa")
            } else {
                // Display error message
                //binding.errorTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun toggleEditMode(editMode: Boolean) {
        isEditMode = editMode

        // Toggle visibility based on edit mode
        if (editMode) {
            // Switch to edit mode: show EditTexts, hide TextViews, and show the Save button
            titleTextView.visibility = View.GONE
            contentTextView.visibility = View.GONE
            priceTextView.visibility = View.GONE
            roomsTextView.visibility = View.GONE
            floorTextView.visibility = View.GONE

            titleEditText.visibility = View.VISIBLE
            contentEditText.visibility = View.VISIBLE
            priceEditText.visibility = View.VISIBLE
            roomsEditText.visibility = View.VISIBLE
            floorEditText.visibility = View.VISIBLE

            editButton.visibility = View.GONE
            saveButton.visibility = View.VISIBLE

        } else {
            // Switch to view mode: show TextViews, hide EditTexts, and show the Edit button
            titleTextView.visibility = View.VISIBLE
            contentTextView.visibility = View.VISIBLE
            priceTextView.visibility = View.VISIBLE
            roomsTextView.visibility = View.VISIBLE
            floorTextView.visibility = View.VISIBLE

            titleEditText.visibility = View.GONE
            contentEditText.visibility = View.GONE
            priceEditText.visibility = View.GONE
            roomsEditText.visibility = View.GONE
            floorEditText.visibility = View.GONE

            //todo: the edit and save button should be visible to the user only
            editButton.visibility = View.VISIBLE
            saveButton.visibility = View.GONE

        }
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

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            // Use Navigation Component's navigateUp for consistency
            findNavController().navigateUp()
        }
    }

    private fun validateInput(): Boolean {
        // Validate fields before updating the post
        val title = binding.titleEditText.text.toString()
        val content = binding.contentEditText.text.toString()
        val price = binding.priceEditText.text.toString()
        val rooms = binding.roomsEditText.text.toString()
        val floor = binding.floorEditText.text.toString()

        return title.isNotEmpty() && content.isNotEmpty() && price.isNotEmpty() && rooms.isNotEmpty() && floor.isNotEmpty()
    }

    private fun bundleToPost(bundle: Bundle): Post {
        return Post(
            id = bundle.getString("id", ""),
            userId = bundle.getString("userId"),
            title = bundle.getString("title", ""),
            content = bundle.getString("content", ""),
            price = bundle.getDouble("price", 0.0),
            rooms = bundle.getInt("rooms", 0),
            floor = bundle.getInt("floor", 0),
            location = GeoPoint(
                bundle.getDouble("latitude"),
                bundle.getDouble("longitude")
            ),
            image = bundle.getString("image"),
            updateTime = bundle.getLong("updateTime", Date().time)
        )
    }

    companion object {
        fun newInstance(post: Post) = PostFragment().apply {
            arguments = Bundle().apply {
                putString("id", post.id)
                putString("userId", post.userId)
                putString("title", post.title)
                putString("content", post.content)
                putDouble("price", post.price)
                putInt("rooms", post.rooms)
                putInt("floor", post.floor)
                putDouble("latitude", post.location.latitude)
                putDouble("longitude", post.location.longitude)
                putString("image", post.image)
                putLong("updateTime", post.updateTime)
            }
        }
    }
}
