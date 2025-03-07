package com.group147.appartmentblog.base

import com.group147.appartmentblog.model.Post

typealias PostsCallback = (List<Post>) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {
    object Collections {
        const val POSTS = "posts"
    }
}