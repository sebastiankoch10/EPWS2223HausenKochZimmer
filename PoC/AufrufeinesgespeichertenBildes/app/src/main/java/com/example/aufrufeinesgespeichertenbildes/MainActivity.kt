package com.example.aufrufeinesgespeichertenbildes

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.aufrufeinesgespeichertenbildes.databinding.ActivityMainBinding
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.*
import java.util.*

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

        val myRef = FirebaseDatabase.getInstance().getReference("outputPic")

        binding.fab.setOnClickListener {

            try {
                readFromDatabase(myRef, this)
            } catch (e: Exception) {
                // handle error
                Log.e("MainActivity", "Error reading from database", e)
            }


        }
    }
}

fun readFromDatabase(myRef: DatabaseReference, activity: MainActivity) {
    val listener = object : ValueEventListener {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val result = dataSnapshot.getValue<String>()
            showPic(result, activity)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // handle error
            Log.e("MainActivity", "Database error: ${databaseError.message}")
            val toast = Toast.makeText(activity, "Error reading from database", Toast.LENGTH_SHORT)
            toast.show()
        }
    }
    myRef.addValueEventListener(listener)
}

@RequiresApi(Build.VERSION_CODES.O)
fun showPic(result : String?, activity: MainActivity) {
    val imageBytes = Base64.getDecoder().decode(result)
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    val imageView = ImageView(activity)
    imageView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    imageView.setImageBitmap(bitmap)
    val linearLayout = findViewById<LinearLayout> (R.id.linear_layout)
    linearLayout.addView(imageView)
}