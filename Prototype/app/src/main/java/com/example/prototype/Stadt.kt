package com.example.prototype
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

@Serializable
class Stadt (
        val name: String,
        val bundesland: String,
        var bilder: MutableList<String>,
        var geschichten: MutableList<Geschichte>,
        var subscribers: MutableList<String>,
        ){
        fun notifySubs (currentStadt:Stadt, userList: List<User>) {
                for (name in currentStadt.subscribers) {
                        for (user in userList) {
                                if (name == user.username) {
                                        user.notificationStadt(this.name)
                                }
                        }
                }

        }
        fun subscribe (user:User) {
                subscribers.add(user.username)
        }
        fun unsubscribe (user:User) {
                subscribers.remove(user.username)
        }

        fun addBild (bild:Bild) {
                this.bilder.add(bild.name)
        }

        fun addGeschichte (geschichte:Geschichte) {
                this.geschichten.add(geschichte)
        }
}