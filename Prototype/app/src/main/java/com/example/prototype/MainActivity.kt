package com.example.prototype

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException

/* TODO
* Städteliste laden
* currentStadt anhand User festlegen
* Abfrage für Bild Metadaten einrichten
* Abruf der Notifications des Nutzers
* Userdaten abspeichern
* */



class MainActivity : AppCompatActivity() {
    lateinit var usernameText: EditText
    lateinit var passwordText: EditText
    lateinit var viewFlipper: ViewFlipper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        usernameText = findViewById(R.id.editTextUsername)
        passwordText = findViewById(R.id.editTextPassword)
        viewFlipper = findViewById(R.id.idViewFlipper)

        //userListe einlesen
        val usersJson =
            applicationContext.assets.open("Users.json").bufferedReader().use { it.readText() }
        val userList: List<User> = Json.decodeFromString(usersJson)

        //leeren aktuellen User & User initialisieren
        var currentUser = User("", "", "", mutableListOf(),"")
        var currentStadt = Stadt("","",mutableListOf(),mutableListOf(),mutableListOf())
        //login Button
        val loginButton = findViewById<Button>(R.id.login)
        loginButton.setOnClickListener {
            //Übernahme der Eingabe
            var username = usernameText.text.toString()
            var password = passwordText.text.toString()

            //Vergleich mit Userliste, setzt gefundenen User als currentUser und wechselt zum nächsten Layout
            for (user in userList) {
                if (username == user.username && password == user.password) {
                    currentUser = user
                    viewFlipper.showNext()
                }
            }
        }

        //Staedteliste einlesen
        val stadtJson =
            applicationContext.assets.open("Staedteliste.json").bufferedReader().use { it.readText() }
        val staedteliste: List<Stadt> = Json.decodeFromString(stadtJson)

        //setze currenStadt auf heimatstadt von currentUser
        for (stadt in staedteliste) {
            if (currentUser.heimatstadt==stadt.name) {
                currentStadt= stadt
            }
        }


        //Upload
        val upladButton = findViewById<Button>(R.id.upload)
        upladButton.setOnClickListener { view ->
            //lesen
            var drawable = readFile(view)
            //convert bitmap (JPG?)
            var bitmap = (drawable as BitmapDrawable).bitmap
            //convert Base64 String
            var encodedImage = convertToBase64(bitmap)
            //convert to JSON

            //Bildobjekt erzeugen
            var currentImage = Bild(
                "Test",
                2023,
                "",
                "",
                encodedImage,
                currentUser,
                false,
                mutableListOf<String>(),
                mutableListOf<User>(),
                ""
            )

            //Zum Stadtobjekt hinzufügen
            currentStadt.addBild(currentImage)

            //Aktuelle Stadt abspeichern
            val speicherString = Json.encodeToString(currentStadt)
            writeToJson(speicherString, "Städteliste.json")

            //Subscriber benachrichtigen
            currentStadt.notifySubs(currentStadt, userList)
        }

        //Bildaufruf
        val aufrufButton = findViewById<Button>(R.id.aufruf)
        aufrufButton.setOnClickListener {
            try {
                //Einrichten des imageViews
                val imageView = ImageView(this@MainActivity)
                imageView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
                linearLayout.addView(imageView)

                //decodieren und Anzeige des Bildes
                val encodedImage = currentStadt.bilder[0].Bilddaten
                val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                imageView.setImageBitmap(bitmap)

                //Exceptions
            } catch (e: FileNotFoundException) {
                println("File not found: ${e.message}")
            } catch (e: IOException) {
                println("IO Exception: ${e.message}")
            } catch (e: IllegalAccessError) {
                println("IllegalAccessError: ${e.message}")
            } catch (e: IllegalAccessException) {
                println("IllegalAccessException: ${e.message}")
            } catch (e: Exception) {
                println("Exception: ${e.message}")
            }
        }

        //SubButton
        val subButton = findViewById<Button>(R.id.subscribe_button)
        subButton.setOnClickListener {
            if (!currentStadt.subscribers.contains(currentUser.username)) {
                currentStadt.subscribe(currentUser)
            } else {
                currentStadt.unsubscribe(currentUser)
            }
        }

        //Logout
        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener {
            val speicherString = Json.encodeToString(userList)
            writeToJson(speicherString, "Users.json")
            viewFlipper.showPrevious()
        }
    }

    private fun readFile(view: View): Drawable? {
        val drawable = ContextCompat.getDrawable(this, R.drawable.muensterplatz_freiburg)
        val readFileString = drawable.toString()
        Snackbar.make(view, readFileString, Snackbar.LENGTH_LONG)
            .setAction("fehler", null).show()
        return drawable
    }

    private fun convertToBase64(bitmap: Bitmap): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val encodedImage = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        return encodedImage
    }

    private fun writeToJson(jsonString: String, filename: String) {
        try {
            val fOut = openFileOutput(filename, Context.MODE_PRIVATE)
            fOut.write(jsonString.toByteArray())
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}