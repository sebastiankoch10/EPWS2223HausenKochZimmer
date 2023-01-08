package com.example.bild_uploadaufruf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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

            val aufrufButton = findViewById<Button>(R.id.aufruf)
            aufrufButton.setOnClickListener {
                try {
                    val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    val fileName = "outputPic6385210105626963512.json"
                    val file = File(filePath, fileName)
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
            }
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