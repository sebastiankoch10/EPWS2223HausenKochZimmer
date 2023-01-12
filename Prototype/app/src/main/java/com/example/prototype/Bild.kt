package com.example.prototype
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.Base64
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import org.json.JSONObject
import java.io.*

@Serializable
class Bild (
    var name: String,
    var jahr: Int,
    var adresse: String,
    var rechteinhaber: String,
    val Bilddaten: String?,
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