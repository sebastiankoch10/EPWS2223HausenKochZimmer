package com.example.prototype
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

@Serializable
class Forum {

    @Serializable
    data class Forumpost (
        val text: String,
        val author: User
        )
    @Serializable
    data class Thread (
        val posts: MutableList<Forumpost>,
        val op: User
            )

    val threads: MutableList<Thread> = mutableListOf()

    // fun createThread()

    // fun createPost()
}