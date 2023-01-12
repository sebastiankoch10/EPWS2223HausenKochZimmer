package com.example.prototype
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

@Serializable
class User(
    val email: String,
    val password: String,
    val username: String,
    var notifications: MutableList<String>
) {

    fun notificationStadt (absender: Stadt) {
        val text = "Es gibt einen neuen Beitrag in $absender"
        notifications.add(text)
    }
    fun removeNotification (index: Int) {
        notifications.removeAt(index)
    }
}
