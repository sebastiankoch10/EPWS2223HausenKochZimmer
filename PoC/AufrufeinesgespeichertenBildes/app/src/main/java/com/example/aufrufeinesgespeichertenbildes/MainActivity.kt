package com.example.aufrufeinesgespeichertenbildes

import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.aufrufeinesgespeichertenbildes.databinding.ActivityMainBinding
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_READ_EXTERNAL_STORAGE = 1


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener {
            //TODO()
            //TODO(Lese JSON von Device)

            /* val permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // Berechtigung wurde noch nicht gewährt: Anfordern während der Laufzeit
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            } else {

            */

            // Array mit den anzufordernden Berechtigungen
            // val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

// Anfordern der Berechtigungen
            // ActivityCompat.requestPermissions(this, permissions, REQUEST_READ_EXTERNAL_STORAGE)


            //try {

            //val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            //val fileName = "outputPic6385210105626963512.json"
            //val file = File(filePath, fileName)
            //val contentFile = BufferedReader(FileReader(file)).use { it.readText() }


          /*  val options = FirebaseOptions.Builder().setApiKey("key").setApplicationId("id")
                .setDatabaseUrl("https://epws2223hausenkochzimmer-default-rtdb.europe-west1.firebasedatabase.app")
                .build()

            FirebaseApp.initializeApp(applicationContext, options, "secondApp")

           */

            // Write a message to the database
            val database = Firebase.database
            val myRef = database.getReference("message")

            myRef.setValue("Hello, World!")

            var result = ""

            fun readFromDatabase(): String {
                var value = ""
                runBlocking {
                    val deferred = async {
                        val myRef = FirebaseDatabase.getInstance().getReference("message")
                        myRef.addValueEventListener(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                value = snapshot.getValue<String>().toString()
                                Log.d(TAG, "Value is: $value")
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.w(TAG, "Failed to read value.", error.toException())
                            }
                        })
                    }
                    deferred.await()
                }
                return value
            }

// use the function
            result = readFromDatabase()

// then use the result variable
            println(result)

            myRef.setValue("Hello, World!2")


            val jsonObject = JSONObject()
            runBlocking {
                val databaseValue = async { readDatabase() }.await()
                jsonObject.put("jsonImage", databaseValue)
            }

            val imageView = ImageView(this@MainActivity)
            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
            linearLayout.addView(imageView)


            val encodedimage = jsonObject.getString("jsonImage")

            val imageBytes = Base64.decode(encodedimage, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)
/*
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

 */


            //TODO(parsen des Bildes)
            //TODO(Aus gabe als Bild)
            //ImageView funtion
        }
    }


    private suspend fun readDatabase(): String {
        var value: String = ""
        // Read from the database
        val dataBase = FirebaseDatabase.getInstance()
        val myRef = dataBase.getReference("test")
        return suspendCancellableCoroutine { cont ->
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    value = snapshot.getValue(String::class.java).toString()
                    Log.d(TAG, "Value is: $value")
                    cont.resumeWith(Result.success(value))
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read value.", error.toException())
                    cont.resumeWith(Result.failure(error.toException()))
                }
            })
        }
    }

    fun readJson(filePath: String, fileName: String): Any {
        val objectMapper = ObjectMapper().registerModule(
            KotlinModule.Builder().withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false).build()
        )
        val file = File(filePath + fileName)
        return objectMapper.readValue(file, Any::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                // Überprüfen, ob die Berechtigung gewährt wurde
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val filePath =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    val fileName = "outputPic6385210105626963512.json"
                    val file = File(filePath, fileName)
                    val contentFile = BufferedReader(FileReader(file)).use { it.readText() }
                } else {
                    // Berechtigung wurde nicht gewährt: Benutzer benachrichtigen
                    Toast.makeText(
                        this, "Read external storage permission denied.", Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }
}




