package com.group147.appartmentblog.base

typealias TaskCallback<T> = (T?, Exception?) -> Unit
typealias EmptyCallback = () -> Unit

const val UPDATE_TIME_KEY = "updateTime"
const val ID_KEY = "id"

enum class Collections(val collectionName: String) {
    POSTS("posts"),
    USERS("users"),
    COMMENTS("comments")
}
