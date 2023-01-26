package com.example.prototype

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException

/*
* TODO Städteliste laden
* TODO currentStadt anhand User festlegen
* TODO Abfrage für Bild Metadaten einrichten
* TODO Abruf der Notifications des Nutzers
* TODO Userdaten abspeichern
* */



class MainActivity : AppCompatActivity() {
    private lateinit var usernameText: EditText
    private lateinit var passwordText: EditText
    private lateinit var viewFlipper: ViewFlipper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        usernameText = findViewById(R.id.editTextUsername)
        passwordText = findViewById(R.id.editTextPassword)
        viewFlipper = findViewById(R.id.idViewFlipper)
        val storageRef = Firebase.storage.reference

        //userListe einlesen
        val usersJson =
            applicationContext.assets.open("Users.json").bufferedReader().use { it.readText() }
        val userList: List<User> = Json.decodeFromString(usersJson)

        //leeren aktuellen User initialisieren
        var currentUser = User("", "", "", mutableListOf())

        //login Button
        val loginButton = findViewById<Button>(R.id.login)
        loginButton.setOnClickListener {
            //Übernahme der Eingabe
            val username = usernameText.text.toString()
            val password = passwordText.text.toString()

            //Vergleich mit Userliste, setzt gefundenen User als currentUser und wechselt zum nächsten Layout
            for (user in userList) {
                if (username == user.username && password == user.password) {
                    currentUser = user
                    viewFlipper.showNext()
                }
            }
        }

        //Erzeugt Stadtobjekt, to be replaced by loading city data from JSON
        val currentStadt = Stadt(
            "Gummersbach", "NRW", Forum(), mutableListOf(), mutableListOf(), mutableListOf()
        )


        //Upload
        val upladButton = findViewById<Button>(R.id.upload)
        upladButton.setOnClickListener { view ->
            //lesen
            val drawable = readFile(view)
            //convert bitmap (JPG?)

            val bitmap = (drawable as BitmapDrawable).bitmap

            //convert to JSON

            //Bild in Firebase storage speichern  ToDo name des Bildes abfragen
            GlobalScope.launch {
                val picLink = writeToStorage(bitmap, "myImage", storageRef)


                //Bildobjekt erzeugen  ToDo urlPic könnte auch aus dem namen des Bildes gebildet werden
                val currentImage = Bild(
                    "Test",
                    2023,
                    "",
                    "",
                    picLink,
                    currentUser,
                    false,
                    mutableListOf(),
                    mutableListOf(),
                    ""
                )

                //Zum Stadtobjekt hinzufügen
                currentStadt.addBild(currentImage)

                //convert Stadt zu JsonObject zu Map

                val gson = Gson()
                val stringCity = gson.toJson(currentStadt)
                //ToDo veraltet
                val jsonObjectCity = JsonParser().parse(stringCity).asJsonObject
                val cityMap: Map<String, Any?> =
                    gson.fromJson(jsonObjectCity, object : TypeToken<Map<String, Any?>>() {}.type)

                //Aktuelle Stadt abspeichern
                //val speicherString = Json.encodeToString(currentStadt)
                //writeToJson(speicherString, "Städteliste.json")

                writeToDatabase(cityMap, "Test")

                //Subscriber benachrichtigen
                currentStadt.notifySubs(currentStadt, userList)
            }
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
        Snackbar.make(view, readFileString, Snackbar.LENGTH_LONG).setAction("fehler", null).show()
        return drawable
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

    suspend fun writeToStorage(
        pic: Bitmap,
        namesOfPic: String,
        storageRef: StorageReference
    ): String {
        val imagesRef = storageRef.child("images/$namesOfPic")

        val stream = ByteArrayOutputStream()
        pic.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val data = stream.toByteArray()
        val uploadTask = imagesRef.putBytes(data)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imagesRef.downloadUrl
        }
        return urlTask.await().toString()
    }


    private fun writeToDatabase(city: Map<String, Any?>, nameCity: String) {

        val database = Firebase.database
        val myRef = database.getReference("cities/$nameCity")

        myRef.setValue(city)
    }

    //ToDo einbindung von Usercontent anzeige
    fun readFromDatabase(myRef: DatabaseReference, activity: MainActivity) {
        val listener = object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val result = dataSnapshot.getValue<String>()
                //showPic(result, activity)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
                Log.e("MainActivity", "Database error: ${databaseError.message}")
                val toast =
                    Toast.makeText(activity, "Error reading from database", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        myRef.addValueEventListener(listener)
    }

    //ToDo umschreiben für storage
    fun readFromStorage(myRef: DatabaseReference, activity: MainActivity) {
        val listner = object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val result = dataSnapshot.getValue<String>()
                //showPic(result, activity)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
                Log.e("MainActivity", "Database error: ${databaseError.message}")
                val toast =
                    Toast.makeText(activity, "Error reading from database", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
        myRef.addValueEventListener(listner)

    }
}