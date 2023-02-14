package com.example.prototype

import android.annotation.SuppressLint
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
import com.google.android.material.textfield.TextInputEditText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.StringReader
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.tasks.await

/* TODO
Merge Branches
Aufruf eines beliebigen Bildes in Bilderliste
Anzeige der Bild Metadaten
* */



class MainActivity : AppCompatActivity() {
    lateinit var usernameText: EditText
    lateinit var passwordText: EditText
    lateinit var viewFlipper: ViewFlipper
    lateinit var notifications: TextView
    lateinit var linearLayout: LinearLayout
    lateinit var BildnameText: EditText
    lateinit var BildJahrText: EditText
    lateinit var BildAdresseText: EditText
    lateinit var BildRechteinhaberText: EditText
    lateinit var BildBeschreibungText: TextInputEditText
    lateinit var staedteliste : List<Stadt>

    @SuppressLint("MissingInflatedId") //ID of notificationsText seemingly cannot be found
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Views werden an Variablen gebunden für späteren Aufruf
        setContentView(R.layout.activity_main)
        usernameText = findViewById(R.id.editTextUsername)
        passwordText = findViewById(R.id.editTextPassword)
        viewFlipper = findViewById(R.id.idViewFlipper)
        notifications = findViewById(R.id.notificationsText)
        linearLayout = findViewById(R.id.linear_layout)
        BildnameText = findViewById(R.id.editTextBildName)
        BildJahrText = findViewById(R.id.editTextJahr)
        BildAdresseText = findViewById(R.id.editTextAddresse)
        BildRechteinhaberText = findViewById(R.id.editTextRechteinhaber)
        BildBeschreibungText = findViewById(R.id.TextInputBeschreibung)
        val imageView = ImageView(this@MainActivity)
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        //userListe einlesen
        val usersJson =
            applicationContext.assets.open("Users.json").bufferedReader().use { it.readText() }
        val userList: List<User> = Json.decodeFromString(usersJson)

        //leeren aktuellen User & Stadt initialisieren
        var currentUser = User("", "", "", mutableListOf(),"")
        var currentStadt = Stadt("","",mutableListOf(),mutableListOf(),mutableListOf())
        var currentBilderliste: MutableList<Bild> = mutableListOf()


        //Login
        val loginButton = findViewById<Button>(R.id.login)
        loginButton.setOnClickListener {
            //Übernahme der Eingabe
            var username = usernameText.text.toString()
            var password = passwordText.text.toString()

            //Vergleich mit Userliste, setzt gefundenen User als currentUser
            for (user in userList) {
                if (username == user.username && password == user.password) {
                    currentUser = user
                    //falls notifications vorhanden, zeige Erste
                    if (currentUser.notifications.isNotEmpty()){
                        notifications.text = currentUser.notifications[0]
                        notifications.visibility = View.VISIBLE
                    }
                    //Staedteliste einlesen
                    val stadtJson =
                        applicationContext.assets.open("Staedteliste.json").bufferedReader().use { it.readText() }
                    staedteliste = Json.decodeFromString(stadtJson)

                    //setze currenStadt auf heimatstadt von currentUser
                    for (stadt in staedteliste) {
                        if (currentUser.heimatstadt==stadt.name) {
                            currentStadt= stadt
                        }
                    }
                    //Einlesen der Bilderliste der Stadt anhand Stadtnamen
                    var bilderlisteJson = applicationContext.assets.open(currentStadt.name+".json").bufferedReader().use {it.readText()}
                    currentBilderliste = Json.decodeFromString(bilderlisteJson)
                    //Wechsel zum nächsten Layout
                    viewFlipper.showNext()
                }
            }
        }




        //Upload
        lateinit var bitmap:Bitmap
        val upladButton = findViewById<Button>(R.id.upload)
        upladButton.setOnClickListener { view ->

            //Bild einlesen
            var drawable = readFile(view)
            //convert bitmap (JPG?)
             bitmap = (drawable as BitmapDrawable).bitmap

            viewFlipper.showNext() }

        //Bilddaten eingeben
        val finalizeButton = findViewById<Button>(R.id.buttonFinalize)
        finalizeButton.setOnClickListener { view ->

            GlobalScope.launch {
                val picLink = writeToStorage(bitmap,currentStadt.name, BildnameText.text.toString())

                //Bildobjekt erzeugen
                var currentImage = Bild(
                    BildnameText.text.toString(),  //TODO required DB
                    BildJahrText.text.toString().toInt(), //TODO null save
                    BildAdresseText.text.toString(),
                    BildRechteinhaberText.text.toString(),
                    picLink,
                    currentUser,
                    false,
                    mutableListOf<String>(),
                    mutableListOf<User>(),
                    BildBeschreibungText.text.toString()
                )

                //Zum Stadtobjekt hinzufügen
                currentBilderliste.add(currentImage)
                currentStadt.addBild(currentImage)

                //Aktuelle Stadt abspeichern TODO to Städteliste
                val gson = Gson()
                val stringCity = gson.toJson(staedteliste)
                val stringBilder = gson.toJson(currentBilderliste)

                var jsonReader = JsonReader(StringReader(stringCity))
                jsonReader.isLenient = true
                val cityMap = gson.fromJson<Map<String, Any?>>(
                    stringCity,
                    object : TypeToken<Map<String, Any?>>() {}.type
                )
                jsonReader = JsonReader(StringReader(stringBilder))
                jsonReader.isLenient = true
                val BilderMap = gson.fromJson<Map<String, Any?>>(
                    stringBilder,
                    object : TypeToken<Map<String, Any?>>() {}.type
                )
                writeToDatabaseCity(cityMap)
                writeToDatabaseBilder(BilderMap, currentStadt.name)

                //Subscriber benachrichtigen
                currentStadt.notifySubs(currentStadt, userList)

                viewFlipper.showPrevious()

                //Feedback
                notifications.text = "Bild wurde hochgeladen"
                notifications.visibility = View.VISIBLE
            }
        }

        //Bildaufruf
        val aufrufButton = findViewById<Button>(R.id.aufruf)
        aufrufButton.setOnClickListener {
            readFromDatabase(this, currentStadt.name)
        }

        //SubButton
        val subButton = findViewById<Button>(R.id.subscribe_button)
        subButton.setOnClickListener {
            if (!currentStadt.subscribers.contains(currentUser.username)) {
                currentStadt.subscribe(currentUser)
                notifications.text = "Subscribe erfolgreich"
                notifications.visibility = View.VISIBLE
            } else {
                currentStadt.unsubscribe(currentUser)
                notifications.text = "Unsubscribe erfolgreich"
                notifications.visibility = View.VISIBLE
            }
        }

        //Logout
        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener {
            //Speichern von Veränderung an Usern (Notifications)
            val speicherString = Json.encodeToString(userList)
            writeToJson(speicherString, "Users.json")

            //Rückkehr auf Login Layout
            viewFlipper.showPrevious()
            //Remove/Hide temporary Views
            notifications.visibility = View.INVISIBLE
            linearLayout.removeView(imageView)
        }
    }

    //einlesen des Bildes als Drawable
    private fun readFile(view: View): Drawable? {
        val drawable = ContextCompat.getDrawable(this, R.drawable.muensterplatz_freiburg)
        val readFileString = drawable.toString()
        Snackbar.make(view, readFileString, Snackbar.LENGTH_LONG)
            .setAction("fehler", null).show()
        return drawable
    }

    //Bild in DB speichern
    private suspend fun writeToStorage(
        pic: Bitmap,
        namesOfPic: String,
        nameCity:String
    ): String {
        val imagesRef = FirebaseStorage.getInstance().reference.child("images").child(nameCity).child(namesOfPic)

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

    //enkodieren des Bildes zu String
    private fun convertToBase64(bitmap: Bitmap): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val encodedImage = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        return encodedImage
    }

    //speichern to Json in internem Speicher des Devices
    private fun writeToJson(jsonString: String, filename: String) {
        try {
            val fOut = openFileOutput(filename, Context.MODE_PRIVATE)
            fOut.write(jsonString.toByteArray())
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun writeToDatabaseCity(city: Map<String, Any?>) {

        val myRef = FirebaseDatabase.getInstance().reference.child("cities")

        myRef.setValue(city)
    }
    private fun writeToDatabaseBilder(Bilder: Map<String, Any?>, nameBilder: String) {

        val myRef = FirebaseDatabase.getInstance().reference.child("images").child(nameBilder)

        myRef.setValue(Bilder)
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
        linearLayout.addView(imageView)
        imageView.setImageBitmap(result)
    }


}