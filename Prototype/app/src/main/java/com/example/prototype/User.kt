package com.example.prototype

import kotlinx.serialization.Serializable

@Serializable
class User(
    val email: String,
    val password: String,
    val username: String,
    var notifications: MutableList<String>,
    val heimatstadt: String
) {

    fun notificationStadt(absender: String) {
        val text = "Es gibt einen neuen Beitrag in $absender"
        notifications.add(text)
    }

    fun removeNotification(index: Int) {
        notifications.removeAt(index)
    }
}
