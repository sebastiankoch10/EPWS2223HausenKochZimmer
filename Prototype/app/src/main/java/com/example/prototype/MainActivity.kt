package com.example.prototype

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream
import java.io.StringReader


//TODO Aufruf eines beliebigen Bildes in Bilderliste


class MainActivity : AppCompatActivity() {
    lateinit var usernameText: EditText
    lateinit var passwordText: EditText
    lateinit var viewFlipper: ViewFlipper
    lateinit var notifications: TextView
    lateinit var layoutImage: LinearLayout
    lateinit var BildnameText: EditText
    lateinit var BildJahrText: EditText
    lateinit var BildAdresseText: EditText
    lateinit var BildRechteinhaberText: EditText
    lateinit var BildBeschreibungText: TextInputEditText
    lateinit var BildnameText2: EditText
    lateinit var BildJahrText2: EditText
    lateinit var BildAdresseText2: EditText
    lateinit var BildRechteinhaberText2: EditText
    lateinit var BildBeschreibungText2: TextInputEditText
    lateinit var changeCityText: EditText
    var staedteliste: List<Stadt> = emptyList()
    var drawable:Drawable? = null

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId") //ID of notificationsText seemingly cannot be found
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Views werden an Variablen gebunden für späteren Aufruf
        setContentView(R.layout.activity_main)
        usernameText = findViewById(R.id.editTextUsername)
        passwordText = findViewById(R.id.editTextPassword)
        viewFlipper = findViewById(R.id.idViewFlipper)
        notifications = findViewById(R.id.notificationsText)
        layoutImage = findViewById(R.id.LayoutImage)
        BildnameText = findViewById(R.id.editTextBildName)
        BildJahrText = findViewById(R.id.editTextJahr)
        BildAdresseText = findViewById(R.id.editTextAddresse)
        BildRechteinhaberText = findViewById(R.id.editTextRechteinhaber2)
        BildBeschreibungText = findViewById(R.id.textInputBeschreibung2)
        BildnameText2 = findViewById(R.id.editTextBildName2)
        BildJahrText2 = findViewById(R.id.editTextJahr2)
        BildAdresseText2 = findViewById(R.id.editTextAddresse2)
        BildRechteinhaberText2 = findViewById(R.id.editTextRechteinhaber2)
        BildBeschreibungText2 = findViewById(R.id.textInputBeschreibung2)
        changeCityText = findViewById(R.id.changeCity)
        val imageView = ImageView(this@MainActivity)
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        /*Viewflipper IDs:
        Login = 0
        Home = 1
        Upload = 2
        Aufruf = 3
        Edit = 4
         */

        //userListe einlesen
        val usersJson =
            applicationContext.assets.open("Users.json").bufferedReader().use { it.readText() }
        val userList: List<User> = Json.decodeFromString(usersJson)

        //leeren aktuellen User & Stadt initialisieren
        var currentUser = User("", "", "", mutableListOf(), "")
        var currentStadt = Stadt("", "", mutableListOf(), mutableListOf(), mutableListOf())
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
                    if (currentUser.notifications.isNotEmpty()) {
                        notifications.text = currentUser.notifications[0]
                        notifications.visibility = View.VISIBLE
                    }
                    //Staedteliste einlesen
                    /*val stadtJsonLocal =
                        applicationContext.assets.open("Staedteliste.json").bufferedReader().use { it.readText() }
                    val myRefCitiesLocal = FirebaseDatabase.getInstance().reference.child("cities")
                    myRefCitiesLocal.setValue(stadtJsonLocal)

                     */



                    val myRefCities = FirebaseDatabase.getInstance().reference.child("cities")

                    runBlocking {
                        val stadtJson = async { readFromDatabase(myRefCities){staedteliste} }.await()
                        staedteliste = Json.decodeFromString(stadtJson.toString())
                    }



                    //setze currenStadt auf heimatstadt von currentUser
                    for (stadt in staedteliste) {
                        if (currentUser.heimatstadt == stadt.name) {
                            currentStadt = stadt
                        }
                    }
                    //Einlesen der Bilderliste der Stadt anhand Stadtnamen
                    val myRefImage = FirebaseDatabase.getInstance().reference.child("images").child(currentStadt.name)
                    readFromDatabase(myRefImage) { bilderlisteJson ->
                        currentBilderliste = Json.decodeFromString(bilderlisteJson)
                    }
                    //Wechsel zum nächsten Layout
                    viewFlipper.setDisplayedChild(1)
                    val currentCity = findViewById<TextView>(R.id.textCurrentStadt)
                    currentCity.text = currentStadt.name
                }
            }
        }


        //Upload
        lateinit var bitmap: Bitmap
        val upladButton = findViewById<Button>(R.id.upload)
        upladButton.setOnClickListener { view ->
            runBlocking {
                //Bild einlesen
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
            }
            //convert bitmap (JPG?)
            bitmap = (drawable as BitmapDrawable).bitmap

            viewFlipper.setDisplayedChild(2)
        }

        //Bilddaten eingeben
        val finalizeButton = findViewById<Button>(R.id.buttonFinalize)
        finalizeButton.setOnClickListener { view ->

            GlobalScope.launch {
                var counter = 1  //TODO auch für ander Routin oder Alte Datenstände richtig?
                val picLink: String = if (BildnameText.text.isNullOrBlank()) {
                    counter++
                    writeToStorage(bitmap, currentStadt.name, "Bild$counter")
                } else {
                    writeToStorage(bitmap, currentStadt.name, BildnameText.text.toString())
                }


                //Bildobjekt erzeugen
                val currentImage = Bild(
                    BildnameText.text.toString(),
                    BildJahrText.text.toString().toIntOrNull() ?: 0,
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
                val stringCity = gson.toJson(currentStadt)
                val stringBilder = gson.toJson(currentImage)

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
                writeToDatabaseBilder(BilderMap, currentStadt.name, currentImage.name)

                //Subscriber benachrichtigen
                currentStadt.notifySubs(currentStadt, userList)

                withContext(Dispatchers.Main) {
                    viewFlipper.setDisplayedChild(1)

                    //Feedback

                    notifications.text = "Bild wurde hochgeladen"
                    notifications.visibility = View.VISIBLE
                }
            }
        }

        //Bildaufruf
        //TODO work with a selected Image
        val aufrufButton = findViewById<Button>(R.id.aufruf)
        aufrufButton.setOnClickListener {
            viewFlipper.setDisplayedChild(3)
            var name = findViewById<TextView>(R.id.BildName)
            var jahr = findViewById<TextView>(R.id.BildJahr)
            var adresse = findViewById<TextView>(R.id.BildAdresse)
            var rechteinhaber = findViewById<TextView>(R.id.BildRechteinhaber)
            var uploader = findViewById<TextView>(R.id.BildUploader)
            var beschreibung = findViewById<TextView>(R.id.BildBeschreibung)
            var verified = findViewById<TextView>(R.id.BildIsVerified)
            name.text = currentBilderliste[0].name
            jahr.text = currentBilderliste[0].jahr.toString()
            adresse.text = currentBilderliste[0].adresse
            rechteinhaber.text = currentBilderliste[0].rechteinhaber
            uploader.text = currentBilderliste[0].uploader.username
            beschreibung.text = currentBilderliste[0].beschreibung
            verified.text = currentBilderliste[0].isVerified.toString()
            readFromStorage(currentBilderliste[0].name, currentStadt.name)
            //readFromDatabase(this, currentStadt.name, currentBilderliste)
        }

        //Flip back to Home
        val backButton = findViewById<Button>(R.id.BildBack)
        backButton.setOnClickListener {
            viewFlipper.setDisplayedChild(1)
            layoutImage.removeView(imageView)
        }

        //Flip to Edit View
        val editButton = findViewById<Button>(R.id.BildEdit)
        editButton.setOnClickListener {
            viewFlipper.setDisplayedChild(4)

        }

        //Finalize for Edit
        //TODO work with a selected Image
        val finalizeButton2 = findViewById<Button>(R.id.buttonFinalize2)
        finalizeButton2.setOnClickListener {
            if (BildnameText2.text.toString() != "") {
                currentBilderliste[0].name = BildnameText2.text.toString()
            }
            if (BildJahrText2.text.toString() != "") {
                currentBilderliste[0].jahr = BildJahrText2.text.toString().toIntOrNull() ?:0
            }
            if (BildAdresseText2.text.toString() != "") {
                currentBilderliste[0].adresse = BildAdresseText2.text.toString()
            }
            if (BildRechteinhaberText2.text.toString() != "") {
                currentBilderliste[0].rechteinhaber = BildRechteinhaberText2.text.toString()
            }
            if (BildBeschreibungText2.text.toString() != "") {
                currentBilderliste[0].beschreibung = BildBeschreibungText2.text.toString()
            }
            val gson = Gson()
            val stringBilder = gson.toJson(currentBilderliste[0])
            var jsonReader = JsonReader(StringReader(stringBilder))
            jsonReader.isLenient = true
            val BilderMap = gson.fromJson<Map<String, Any?>>(
                stringBilder,
                object : TypeToken<Map<String, Any?>>() {}.type
            )
            writeToDatabaseBilder(BilderMap, currentStadt.name, currentBilderliste[0].name)
            viewFlipper.setDisplayedChild(1)

            notifications.text = "Bild wurde bearbeitet"
            notifications.visibility = View.VISIBLE

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

        val changeCityButton = findViewById<Button>(R.id.buttonChangeCity)
        changeCityButton.setOnClickListener {
            for (stadt in staedteliste) {
                if (changeCityText.text.toString() == stadt.name) {
                    currentStadt = stadt
                    val myRefImage = FirebaseDatabase.getInstance().reference.child("images").child(currentStadt.name)
                    readFromDatabase(myRefImage) { bilderlisteJson ->
                        currentBilderliste = Json.decodeFromString(bilderlisteJson)
                    }
                    val currentCity = findViewById<TextView>(R.id.textCurrentStadt)
                    currentCity.text = currentStadt.name
                }
            }
        }

        //Logout
        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener {
            //Speichern von Veränderung an Usern (Notifications)
            val speicherString = Json.encodeToString(userList)
            writeToJson(speicherString, "Users.json")

            //Rückkehr auf Login Layout
            viewFlipper.setDisplayedChild(0)
            //Remove/Hide temporary Views
            notifications.visibility = View.INVISIBLE

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
        nameCity: String,
        namesOfPic: String
    ): String {
        val imagesRef = FirebaseStorage.getInstance().reference.child("images").child(nameCity)
            .child(namesOfPic)

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

    private fun writeToDatabaseBilder(Bilder: Map<String, Any?>, nameStadt: String, nameBild : String) {

        val myRef = FirebaseDatabase.getInstance().reference.child("images").child(nameStadt)
            .child(nameBild)

        myRef.setValue(Bilder)
    }


    private fun readFromDatabase(myRef: DatabaseReference, callback: (String) -> Unit) {
        val jsonList = mutableListOf<String>()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val json = childSnapshot.value.toString()
                    jsonList.add(json)
                }
                val jsonString = "[${jsonList.joinToString(",")}]"
                callback(jsonString)
            }

            override fun onCancelled(error: DatabaseError) {
                // Hier können Sie auf Fehler reagieren, wenn nötig
            }
        })
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun readFromStorage(namesOfPic: String, currentStadt: String) {
        val imagesRef = FirebaseStorage.getInstance().reference.child("images").child(currentStadt)
            .child(namesOfPic)


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
        layoutImage.addView(imageView)
        imageView.setImageBitmap(result)
    }

    //Ergebnis der Bildauswahl
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val clipData = data.clipData
            if (clipData != null) {
                val count = clipData.itemCount
                for (i in 0 until count) {
                    val imageUri = clipData.getItemAt(i).uri
                    drawable = Drawable.createFromPath(imageUri?.path)
                    Toast.makeText(this, "Bild ausgewählt: $imageUri", Toast.LENGTH_SHORT).show()
                }
            } else {
                val selectedImage = data.data
                drawable = Drawable.createFromPath(selectedImage?.path)
                Toast.makeText(this, "Bild ausgewählt: $selectedImage", Toast.LENGTH_SHORT).show()
            }
        }
    }

}