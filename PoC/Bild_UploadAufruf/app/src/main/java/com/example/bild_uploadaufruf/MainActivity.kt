package com.example.bild_uploadaufruf

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import java.io.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val upladButton = findViewById<Button>(R.id.upload)
        upladButton.setOnClickListener { view ->

            //lesen
            val drawable = readFile(view)
            //convert bitmap (JPG?)
            val bitmap = (drawable as BitmapDrawable).bitmap
            //convert Base64 String
            val encodedImage = convertToBase64(bitmap)
            //convert to JSON
            val jsonObject = JSONObject()
            jsonObject.put("outputPic", encodedImage)
            //jsonObject.put("outputPic", "Test")
            //write jason or override
            //schreiben
            writeToJson(jsonObject)
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

    private fun convertToBase64(bitmap: Bitmap): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val encodedImage = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        return encodedImage
    }

    private fun readFile(view: View): Drawable? {
        val drawable = ContextCompat.getDrawable(this, R.drawable.muensterplatz_freiburg)
        val readFileString = drawable.toString()
        Snackbar.make(view, readFileString, Snackbar.LENGTH_LONG)
            .setAction("fehler", null).show()
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
}