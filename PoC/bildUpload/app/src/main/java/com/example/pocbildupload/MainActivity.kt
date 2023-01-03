package com.example.pocbildupload

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.pocbildupload.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.metadata.ImageMetadata
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
            val drawable = ContextCompat.getDrawable(this, R.drawable.muensterplatz_freiburg)
            val test = drawable.toString()
            Snackbar.make(view, test, Snackbar.LENGTH_LONG)
                .setAction("fehler", null).show()
            //convert bitmap (JPG?)
            val bitmap = (drawable as BitmapDrawable).bitmap
            //convert Base64 String
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val encodedImage = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            //convert to JSON
            val jsonObject = JSONObject()
            //jsonObject.put("outputPic", encodedImage)
            jsonObject.put("outputPic","Test")
            //write jason or override
            //schreiben

            //TODO(Schreiben von JSON)

            /*try {
                val context = Context.MODE_PRIVATE
                val outputStreamWriter =
                    OutputStreamWriter(openFileOutput("outputPic.json", MODE_PRIVATE))
                val inputStream: InputStream = context.openFileInput("outputPic.json")


                if (inputStream != null) {
                    //file.writeText("")
                }
                else (inputStream == null) {
                        val outputStreamWriter =
                            OutputStreamWriter(openFileOutput("outputPic.json", MODE_PRIVATE))
                        outputStreamWriter.write("")
                        outputStreamWriter.close()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }*/
            //TODO(verbesserte image lese variante)
            //https://sksamuel.github.io/scrimage/

            val image = ImmutableImage.loader().fromFile("") //TODO([file] anpassen und gifs seperat)
            image.metadata
            //val imageMetadata = ImageMetadata("/sg.txt") //TODO(aus path möglich)
            //val meta = ImageMetadata.fromStream(stream)
            //meta.tags().asScala.foreach { tag =>
            //    println(tag)
            //}  //TODO(Tags erreichbar, add nicht klar)

        }
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