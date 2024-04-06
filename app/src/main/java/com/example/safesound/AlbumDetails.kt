package com.example.safesound

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class AlbumDetails : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var albumPhoto: ImageView
    lateinit var albumName: String
    val albumSongs: ArrayList<MusicFiles> = arrayListOf()
    lateinit var albumDetailsAdapter: AlbumDetailsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_album_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView = findViewById(R.id.recyclerView)
        albumPhoto = findViewById(R.id.albumPhoto)
        albumName = intent.getStringExtra("albumName").toString()

        val musicFiles = intent.getParcelableArrayListExtra<MusicFiles>("albumSongs")

        if (musicFiles != null) {
            albumSongs.addAll(musicFiles.filter { it.album == albumName })
            Log.d("AlbumDetails", "el objeto musicFiles es NULL, amigo")
        }

        if (albumSongs.isNotEmpty()) {
            val image = MusicUtils.getAlbumArt(albumSongs[0].path)
            Log.d("AlbumDetails", "Ruta de imagen cargada: $image")
            if (image != null) {
                Glide.with(this).load(image).into(albumPhoto)
            } else {
                Glide.with(this).load(R.drawable.null_cover).into(albumPhoto)
            }
        } else {

            Glide.with(this).load(R.drawable.null_cover).into(albumPhoto)
        }

        albumDetailsAdapter = AlbumDetailsAdapter(this, albumSongs)
        recyclerView.adapter = albumDetailsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

}

