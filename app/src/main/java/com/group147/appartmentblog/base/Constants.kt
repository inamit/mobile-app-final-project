package com.group147.appartmentblog.base

typealias TaskCallback<T> = (T?, Exception?) -> Unit
typealias EmptyCallback = () -> Unit

enum class Collections(val collectionName: String) {
    POSTS("posts")
}
