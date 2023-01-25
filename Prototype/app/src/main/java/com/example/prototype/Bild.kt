package com.example.prototype
import android.net.Uri
import com.google.firebase.storage.StorageReference
import kotlinx.serialization.Serializable

@Serializable
class Bild(
    var name: String,
    var jahr: Int,
    var adresse: String,
    var rechteinhaber: String,
    val Bilddaten: String,
    val uploader: User,
    var isVerified: Boolean = false,
    var kommentare: MutableList<String>,
    var subscribers: MutableList<User>,
    var beschreibung: String
    ) {
    fun verify() {

    }

    fun subscribe (user:User) {

    }

}