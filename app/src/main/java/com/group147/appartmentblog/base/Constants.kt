package com.group147.appartmentblog.base

typealias TaskCallback<T> = (T?, Exception?) -> Unit
typealias EmptyCallback = () -> Unit

const val UPDATE_TIME_KEY = "updateTime"

enum class Collections(val collectionName: String) {
    POSTS("posts")
}
