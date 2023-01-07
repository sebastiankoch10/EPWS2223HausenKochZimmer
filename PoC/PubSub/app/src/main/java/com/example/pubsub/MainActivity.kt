package com.example.pubsub

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
            // Benachrichtigung an alle Namen in Liste
            for (name in namen) {
                Toast.makeText(this, "Ein neuer Beitrag in Stadt XY von $name!", Toast.LENGTH_SHORT)
                    .show()
            }
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