package com.example.prototype

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.StringReader

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

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
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
            val yourActivity = YourActivity()
            yourActivity.setOnBitmapListReadyCallback(object : OnBitmapListReadyCallback {
                override fun onBitmapListReady(bitmapList: List<Bitmap>) {
                    GlobalScope.launch {
                        val picLinks = ArrayList<String>()
                        for (bitmap in bitmapList) {
                            val picLink = writeToStorage(bitmap, "Test") // ToDo name des Bildes abfragen
                            picLinks.add(picLink)
                        }

                        //Bildobjekt erzeugen  ToDo urlPic könnte auch aus dem namen des Bildes gebildet werden
                        val currentImage = Bild(
                            "Test",
                            2023,
                            "",
                            "",
                            picLinks[0],
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
                        val jsonReader = JsonReader(StringReader(stringCity))
                        jsonReader.isLenient = true
                        val cityMap = gson.fromJson<Map<String, Any?>>(
                            stringCity,
                            object : TypeToken<Map<String, Any?>>() {}.type
                        )

                        //Aktuelle Stadt abspeichern
                        //val speicherString = Json.encodeToString(currentStadt)
                        //writeToJson(speicherString, "Städteliste.json")

                        writeToDatabase(cityMap, "Test")

                        //Subscriber benachrichtigen
                        currentStadt.notifySubs(currentStadt, userList)
                    }
                }
            })
            yourActivity.readPics()
        }


        //Bildaufruf
        val aufrufButton = findViewById<Button>(R.id.aufruf)
        aufrufButton.setOnClickListener {
            readFromDatabase(this, "Test")
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
            val gson = Gson()
            val stringCity = gson.toJson(currentStadt)
            //ToDo write pic to storage
            val jsonReader = JsonReader(StringReader(stringCity))
            jsonReader.isLenient = true
            val cityMap = gson.fromJson<Map<String, Any?>>(
                stringCity,
                object : TypeToken<Map<String, Any?>>() {}.type
            )
            writeToDatabase(cityMap, "Test")
            viewFlipper.showPrevious()
        }
    }

    //Callback Interface, um die Bilder bereitgestellt zu bekommen
    interface OnBitmapListReadyCallback {
        fun onBitmapListReady(bitmapList: List<Bitmap>)
    }

    //Aktivität, welche Bilder bereitstellt
    class YourActivity: AppCompatActivity() {
        //Callback, welcher das Interface implementiert
        private var callback: OnBitmapListReadyCallback? = null

        //Setzen des Callbacks
        fun setOnBitmapListReadyCallback(callback: OnBitmapListReadyCallback) {
            this.callback = callback
        }

        //Überschreiben von onActivityResult
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            //Wenn der RequestCode 1 ist und das Resultat OK und Daten vorliegen
            if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
                //ClipData abrufen
                val clipData = data.clipData
                //Wenn ClipData vorliegt
                if (clipData != null) {
                    //Anzahl der Bilder
                    val count = clipData.itemCount
                    //Liste der Bilder
                    val bitmapList = ArrayList<Bitmap>()
                    //Für alle Bilder
                    for (i in 0 until count) {
                        //Bild-URI abrufen
                        val imageUri = clipData.getItemAt(i).uri
                        //InputStream öffnen
                        val inputStream = contentResolver.openInputStream(imageUri)
                        //Bild dekodieren
                        val selectedImage = BitmapFactory.decodeStream(inputStream)
                        //Bild der Liste hinzufügen
                        bitmapList.add(selectedImage)
                    }
                    //Callback aufrufen und Liste bereitstellen
                    callback?.onBitmapListReady(bitmapList)
                } else {
                    //Wenn kein ClipData vorliegt, aber eine Bild-URI
                    val selectedImageUri = data.data
                    if (selectedImageUri != null) {
                        //InputStream öffnen
                        val inputStream = contentResolver.openInputStream(selectedImageUri)
                        //Bild dekodieren
                        val selectedImage = BitmapFactory.decodeStream(inputStream)
                        //Callback aufrufen und Liste bereitstellen
                        callback?.onBitmapListReady(listOf(selectedImage))
                    }
                }
            }
        }

        // Die Funktion readPics startet einen neuen Bildauswahl-Intent.
        fun readPics(){
            // Ein neuer Intent mit Action ACTION_GET_CONTENT wird erstellt.
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            // Der Typ des Inhalts, den der Intent auswählen soll, wird auf "image/*" festgelegt.
            intent.type = "image/*"
            // Es wird eine extra Option hinzugefügt, die es ermöglicht, mehrere Bilder auszuwählen.
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            // Der Intent wird gestartet und es wird ein "Select Picture"-Dialog angezeigt.
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
        }
    }






    private fun readFile(view: View): Drawable? {
        val drawable = ContextCompat.getDrawable(this, R.drawable.muensterplatz_freiburg)
        val readFileString = drawable.toString()
        Snackbar.make(view, readFileString, Snackbar.LENGTH_LONG).setAction("fehler", null).show()
        return drawable
    }

    /*private fun writeToJson(jsonString: String, filename: String) {
        try {
            val fOut = openFileOutput(filename, Context.MODE_PRIVATE)
            fOut.write(jsonString.toByteArray())
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

     */

    private suspend fun writeToStorage(
        pic: Bitmap,
        namesOfPic: String,
    ): String {
        val imagesRef = FirebaseStorage.getInstance().reference.child("cities").child(namesOfPic)

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

        val myRef = FirebaseDatabase.getInstance().reference.child("cities").child(nameCity)

        myRef.setValue(city)
    }


    private fun readFromDatabase(activity: MainActivity, nameCity: String) {
        val myRef = FirebaseDatabase.getInstance().reference.child("cities").child(nameCity)
        val listener = object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //val result = dataSnapshot.getValue<String>()
                readFromStorage("Test")
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun readFromStorage(namesOfPic: String) {
        val imagesRef = FirebaseStorage.getInstance().reference.child("cities").child(namesOfPic)

        imagesRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            showPic(bitmap, this)
        }.addOnFailureListener {
            // Handle any errors
            val toast =
                Toast.makeText(this, "Error reading from Storage", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showPic(result: Bitmap, activity: MainActivity) {
        val imageView = ImageView(activity)
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        val linearLayout = activity.findViewById<LinearLayout>(R.id.linear_layout)
        linearLayout.addView(imageView)
        imageView.setImageBitmap(result)
    }
}