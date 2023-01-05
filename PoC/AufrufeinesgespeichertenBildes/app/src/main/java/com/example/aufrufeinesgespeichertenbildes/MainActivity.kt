package com.example.aufrufeinesgespeichertenbildes

import android.graphics.BitmapFactory
import android.opengl.ETC1.decodeImage
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.aufrufeinesgespeichertenbildes.databinding.ActivityMainBinding
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.json.JSONObject
import java.io.*
import android.widget.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

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
            val filePath =
            "/storage/emulated/0/Documents/"
            val fileName = "outputPic6385210105626963512.json"
            val storageDir = Environment.getExternalStoragePublicDirectory(filePath) //ToDo
            val file = File(storageDir, fileName)
            val contentFile = BufferedReader(FileReader(file)).use { it.readText() }

            val jsonObject = JSONObject()
            jsonObject.put("jsonImage" ,contentFile)
            val imageView = ImageView(this)
            imageView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val linearLayout = findViewById<LinearLayout>(R.id.linear_layout)
            linearLayout.addView(imageView)


            val encodedimage = jsonObject.getString("jsonImage")
            val imageBytes = Base64.decode(encodedimage, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)










            //TODO(parsen des Bildes)
            //TODO(Aus gabe als Bild)
            //ImageView funtion

        }
    }

    fun readJson(filePath: String, fileName: String): Any {
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val file = File(filePath+ fileName)
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
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}