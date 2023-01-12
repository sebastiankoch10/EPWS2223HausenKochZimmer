package com.example.aufrufeinesgespeichertenbildes

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.aufrufeinesgespeichertenbildes.databinding.ActivityMainBinding
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.json.JSONObject
import java.io.*

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

        binding.fab.setOnClickListener { view ->
            //TODO()
            //TODO(Lese JSON von Device)

            val permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // Berechtigung wurde noch nicht gewährt: Anfordern während der Laufzeit
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            } else {

            // Array mit den anzufordernden Berechtigungen
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

// Anfordern der Berechtigungen
            ActivityCompat.requestPermissions(this, permissions, REQUEST_READ_EXTERNAL_STORAGE)




            try {

                // Read from the database
                myRef.addValueEventListener(object: ValueEventListener() {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        val value = snapshot.getValue<String>()
                        Log.d(TAG, "Value is: " + value)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Failed to read value.", error.toException())
                    }

                //val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                //val fileName = "outputPic6385210105626963512.json"
                //val file = File(filePath, fileName)
                val contentFile = BufferedReader(FileReader(file)).use { it.readText() }


                val jsonObject = JSONObject()
                jsonObject.put("jsonImage", contentFile)


                val imageView = ImageView(this)
                imageView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
                linearLayout.addView(imageView)


                val encodedimage = jsonObject.getString("jsonImage")

                val imageBytes = Base64.decode(encodedimage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                imageView.setImageBitmap(bitmap)

            } catch (e: FileNotFoundException) {
                println("File not found: ${e.message}")
            } catch (e: IOException) {
                println("IO Exception: ${e.message}")
            }catch (e: IllegalAccessError)  {
                println("IllegalAccessError: ${e.message}")
            } catch (e: IllegalAccessException) {
                println("IllegalAccessException: ${e.message}")
            }
            catch (e: Exception) {
                println("Exception: ${e.message}")
            }


            //TODO(parsen des Bildes)
            //TODO(Aus gabe als Bild)
            //ImageView funtion

        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                // Überprüfen, ob die Berechtigung gewährt wurde
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    val fileName = "outputPic6385210105626963512.json"
                    val file = File(filePath, fileName)
                    val contentFile = BufferedReader(FileReader(file)).use { it.readText() }
                } else {
                    // Berechtigung wurde nicht gewährt: Benutzer benachrichtigen
                    Toast.makeText(this, "Read external storage permission denied.", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}



