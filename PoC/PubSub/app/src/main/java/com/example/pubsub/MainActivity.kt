package com.example.pubsub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.subscribe_button)
        button.setOnClickListener {
            Toast.makeText(this, "Subscribed!", Toast.LENGTH_SHORT).show()
        }

        val publishButton = findViewById<Button>(R.id.publish_button)
        publishButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            val PICK_IMAGE_REQUEST = 0
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }
}