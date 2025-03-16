package com.group147.appartmentblog.screens.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.group147.appartmentblog.databinding.PostCardBinding
import com.group147.appartmentblog.model.Post
import com.squareup.picasso.Picasso

class PostAdapter(private val onPostClick: (Post) -> Unit) :
    ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post, onPostClick)
    }

    class PostViewHolder(private val binding: PostCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post, onPostClick: (Post) -> Unit) {
            binding.titleTextView.text = post.title
            binding.priceTextView.text = "Price: \$${post.price}"
            binding.roomsTextView.text = "Rooms: ${post.rooms}"
            Log.d("PostAdapter", "Binding post: ${post.image}")

            if (post.image != null) {
                Picasso.get().load(post.image).into(binding.imageView)
            }

            // Handle click event
            binding.root.setOnClickListener {
                onPostClick(post)
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}
