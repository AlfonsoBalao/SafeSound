package com.example.safesound

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PlayListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnMyLists = findViewById<Button>(R.id.btnMyLists)
        val btnCreateList = findViewById<Button>(R.id.btnCreateList)


        btnMyLists.setOnClickListener {
            // Aquí se inicia la actividad DisplayListActivity
            val intent = Intent(this, DisplayListActivity::class.java)
            startActivity(intent)
        }

        btnCreateList.setOnClickListener {

            val intent = Intent(this, CreatePlayListActivity::class.java)
            startActivity(intent)
        }
    }

}
