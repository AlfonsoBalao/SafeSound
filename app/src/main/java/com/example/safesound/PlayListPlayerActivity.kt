package com.example.safesound


import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayListPlayerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistAdapter: DisplayListAdapter
    private lateinit var listNameTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play_list_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        recyclerView = findViewById<RecyclerView>(R.id.playlistsRecyclerView)




        listNameTextView = findViewById<TextView>(R.id.list_name)

        val playlistId = intent.getLongExtra("PLAYLIST_ID", -1)
        val albumCoverUrl = intent.getStringExtra("ALBUM_COVER_URL")
        val playlistName = intent.getStringExtra("PLAYLIST_NAME") // Recuperar el nombre de la lista de reproducción del Intent
        if (playlistName != null) {
            listNameTextView.text ="Reproduciendo lista: $playlistName" // Establecer el nombre de la lista en el TextView
        }

        if (albumCoverUrl != null) {

            Glide.with(this).load(albumCoverUrl).into(findViewById<ImageView>(R.id.cover_art))
        }

        loadSongs(playlistId)
    }


    private fun setupRecyclerView() {

    }


    private fun loadSongs(playlistId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)

        }
    }





    override fun onResume() {
        super.onResume()

    }


}