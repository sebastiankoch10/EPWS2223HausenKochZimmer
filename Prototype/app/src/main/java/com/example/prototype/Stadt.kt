package com.example.prototype
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

@Serializable
class Stadt (
        val name: String,
        val bundesland: String,
        var forum: Forum,
        var bilder: MutableList<Bild>,
        var geschichten: MutableList<Geschichte>,
        var subscribers: MutableList<User>,
        ){
        fun notifySubs (){
                for (user in subscribers) {
                        user.notificationStadt(this)
                }

        }
        fun subscribe (user:User) {
                subscribers.add(user)
        }
        fun unsubscribe (user:User) {
                subscribers.remove(user)
        }

        fun addBild (bild:Bild) {
                this.bilder.add(bild)
        }

        fun addGeschichte (geschichte:Geschichte) {
                this.geschichten.add(geschichte)
        }
}