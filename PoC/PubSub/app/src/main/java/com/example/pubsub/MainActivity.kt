package com.example.pubsub

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // Liste mit Namen wird erstellt, welche in der Activity gespeichert wird
    private val namen = mutableListOf<String>()
    private val name = "Mein Name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //SubButton
        val button = findViewById<Button>(R.id.subscribe_button)
        button.setOnClickListener {
            if (!namen.contains(name)) { //not contains
                addName(name)
            } else {
                removename(name)
            }
        }

        //PubButton
        val publishButton = findViewById<Button>(R.id.publish_button)
        publishButton.setOnClickListener {
            if (namen.contains(name)) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
            } else {
                for (name in namen) {
                    Toast.makeText(this, "Ein neuer Beitrag in Stadt XY von $name!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val clipData = data.clipData
            if (clipData != null) {
                val count = clipData.itemCount
                for (i in 0 until count) {
                    val imageUri = clipData.getItemAt(i).uri
                    Toast.makeText(this, "Bild ausgewählt: $imageUri", Toast.LENGTH_SHORT).show()
                }
            } else {
                val selectedImage = data.data
                Toast.makeText(this, "Bild ausgewählt: $selectedImage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectMultipleImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, 1)
    }

    private fun removename(name: String) {
        namen.remove(name)
        updateTextView("Subscribe")
        Toast.makeText(this, "Unsubscribed!", Toast.LENGTH_SHORT).show()
    }

    // Name wird zur Liste hinzugefügt
    private fun addName(name: String) {
        namen.add(name)
        updateTextView("Unsubscribe")
        Toast.makeText(this, "Subscribed!", Toast.LENGTH_SHORT).show()
    }

    //Aktualisieren der TextView, welche die Namen der Subscriber anzeigt
    private fun updateTextView(input: String) {
        val textView = findViewById<TextView>(R.id.subscribe_button)
        textView.text = input
    }





}




