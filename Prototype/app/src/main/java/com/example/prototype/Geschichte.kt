package com.example.prototype

import kotlinx.serialization.Serializable

@Serializable
class Geschichte(
    var name: String,
    var jahr: Int,
    var author: User,
    var text: String,
    var isVerfied: Boolean = false,
    var kommentare: MutableList<String>,
    var subscribers: MutableList<User>,
) {
    fun verify() {

    }

    fun subscribe(user: User) {

    }

    fun notifySubs() {

    }
}