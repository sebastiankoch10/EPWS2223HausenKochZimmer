package com.example.pocbildupload

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.pocbildupload.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.io.*


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
            //lesen
            val drawable = readFile(view)
            //convert bitmap (JPG?)
            val bitmap = (drawable as BitmapDrawable).bitmap
            //convert Base64 String
            val encodedImage = convertToBase64(bitmap)
            //convert to JSON
            val jsonObject = JSONObject()
            jsonObject.put("outputPic", encodedImage)
            //write jason or override
            //schreiben
            writeToJson(jsonObject)
            //writeTODatabase(encodedImage.toString())

            //TODO(verbesserte image lese variante)
            //https://sksamuel.github.io/scrimage/

            /*
            val image = ImmutableImage.loader().fromFile("") //TODO([file] anpassen)
            image.metadata
            //val imageMetadata = ImageMetadata("/sg.txt") //TODO(aus path möglich)
            //val meta = ImageMetadata.fromStream(stream)
            //meta.tags().asScala.foreach { tag =>
            //    println(tag)
            //}  //TODO(Tags erreichbar, add nicht klar)
            */
        }
    }

    private fun writeToJson(jsonObject: JSONObject) {
        val jsonString = jsonObject.toString()
        val output: Writer
        //val internalFile = CreateInternalFile()
        val shardFile = CreateSharedFile()
        output = BufferedWriter(FileWriter(shardFile))
        output.write(jsonString)
        output.close()
    }

    private fun writeTODatabase(pic: String) {

        val database = Firebase.database
        val myRef = database.getReference("outputPic")

        myRef.setValue("pic")
    }

    private fun convertToBase64(bitmap: Bitmap): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val encodedImage = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        return encodedImage
    }

    private fun readFile(view: View): Drawable? {
        val drawable = ContextCompat.getDrawable(this, R.drawable.muensterplatz_freiburg)
        if (drawable != null) {
            Snackbar.make(view, "Das Bild ist hochgeladen.", Snackbar.LENGTH_LONG).show()
        } else {
            Snackbar.make(
                view,
                "Das Bild konnte nicht hochgeladen werden, bitte versuchen Sie es zu einem späteren Zeitpunkt nocheinmal.",
                Snackbar.LENGTH_LONG
            ).setAction("Aktion", null).show()
        }
        return drawable
    }

    private fun CreateInternalFile(): File {

        val fileName = "outputPic"

        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (storageDir != null) {
            if (!storageDir.exists()) {
                storageDir.mkdir()
            }
        }
        return File.createTempFile(fileName, ".json", storageDir)
    }

    private fun CreateSharedFile(): File {

        val fileName = "outputPic"


        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (storageDir != null) {
            if (!storageDir.exists()) {
                FileOutputStream(fileName)
            }
        }

        return File.createTempFile(fileName, ".json", storageDir)
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