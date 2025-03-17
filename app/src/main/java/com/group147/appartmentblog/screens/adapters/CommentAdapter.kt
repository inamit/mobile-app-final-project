package com.group147.appartmentblog.screens.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.group147.appartmentblog.R
import com.group147.appartmentblog.model.Comment


class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val textTextView: TextView = itemView.findViewById(R.id.reviewEditText)
        val rate: TextView = itemView.findViewById(R.id.ratingTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val currentComment = comments[position]
        holder.authorTextView.text = currentComment.authorName
        holder.textTextView.text = currentComment.review
        holder.rate.text = currentComment.review
    }

    override fun getItemCount() = comments.size
}