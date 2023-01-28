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
                //     button.text.        //Unsubscribe
            }
        }

        //PubButton
        val publishButton = findViewById<Button>(R.id.publish_button)
        publishButton.setOnClickListener {
            if (namen.contains(name)) {
                // Öffnet den Bilderauswahl-Dialog
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
            } else {
                // Benachrichtigung an alle Namen in Liste
                for (name in namen) {
                    Toast.makeText(this, "Ein neuer Beitrag in Stadt XY von $name!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // Bild wurde ausgewählt, hier kann es verarbeitet werden
            val selectedImage = data.data
            // Beispiel:
            Toast.makeText(this, "Bild ausgewählt: $selectedImage", Toast.LENGTH_SHORT).show()
        }
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