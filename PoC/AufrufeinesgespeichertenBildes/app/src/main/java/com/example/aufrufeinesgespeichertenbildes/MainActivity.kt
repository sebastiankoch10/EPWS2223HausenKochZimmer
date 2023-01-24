package com.example.aufrufeinesgespeichertenbildes

import android.os.Build
import android.os.Bundle
import android.util.Log
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

    @RequiresApi(Build.VERSION_CODES.O)
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
                readFromDatabase(myRef)
            } catch (e: Exception) {
                // handle error
                Log.e("MainActivity", "Error reading from database", e)
            }


        }
    }
}

fun readFromDatabase(myRef: DatabaseReference) {
    val listener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val result = dataSnapshot.getValue<String>()
            println(result)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // handle error
        }
    }
    myRef.addValueEventListener(listener)
}