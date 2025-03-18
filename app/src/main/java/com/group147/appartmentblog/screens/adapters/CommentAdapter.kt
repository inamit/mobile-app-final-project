package com.group147.appartmentblog.screens.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.group147.appartmentblog.databinding.CommentItemBinding
import com.group147.appartmentblog.model.Comment


class CommentAdapter(private val onCommentClick: (Comment) -> Unit) :
    ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CommentItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.bind(comment, onCommentClick)
    }

    class CommentViewHolder(private val binding: CommentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment, onCommentClick: (Comment) -> Unit) {
            binding.usernameTextView.text = comment.authorName
            binding.ratingTextView.text = "Rate: ${comment.rate}"
            binding.commentTextView.text = "Review: ${comment.review}"

            // Handle click event
            binding.root.setOnClickListener {
                onCommentClick(comment)
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment) = oldItem == newItem
    }
}
