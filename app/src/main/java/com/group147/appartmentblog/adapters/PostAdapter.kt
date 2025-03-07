package com.group147.appartmentblog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group147.appartmentblog.R
import com.group147.appartmentblog.model.Post
import com.squareup.picasso.Picasso

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class PostAdapter : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_card, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)  // Use getItem() from ListAdapter to get the post
        holder.title.text = post.title
        holder.price.text = "Price: \$${post.price}"

        // Load the post image using Picasso
        Picasso.get()
            .load(post.imageUrl)  // Replace with actual image URL or URI
            .into(holder.image)
    }

    override fun getItemCount(): Int = currentList.size  // Use currentList from ListAdapter

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
        val image: ImageView = itemView.findViewById(R.id.tvRooms)
    }

    // DiffUtil callback for efficient list updates
    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id  // Check for unique id or primary key
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem  // Check if content is the same
        }
    }
}
