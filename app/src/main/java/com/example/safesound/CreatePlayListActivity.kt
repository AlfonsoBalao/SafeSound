package com.example.safesound

import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class CreatePlayListActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_play_list)

        val musicFiles = loadMusicFiles()
        setupRecyclerView(musicFiles)

    }

    private fun initSongsFragment() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Instanciar SongsFragment
        val songsFragment = SongsFragment()

        // Reemplazar el contenedor en el layout con el SongsFragment
        fragmentTransaction.replace(R.id.songsRecyclerView, songsFragment)
        fragmentTransaction.commit()
    }


    private fun loadMusicFiles(): List<MusicFiles> {
        val musicFilesList = mutableListOf<MusicFiles>()
        val contentResolver = contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        val cursor = contentResolver.query(uri, null, selection, null, sortOrder)

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val titleIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val artistIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val albumIndex = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                    val durationIndex = it.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val idIndex = it.getColumnIndex(MediaStore.Audio.Media._ID)

                    if (titleIndex != -1 && artistIndex != -1 && albumIndex != -1 && durationIndex != -1 && dataIndex != -1 && idIndex != -1) {
                        val title = it.getString(titleIndex)
                        val artist = it.getString(artistIndex)
                        val album = it.getString(albumIndex)
                        val duration = it.getString(durationIndex)
                        val path = it.getString(dataIndex)
                        val id = it.getString(idIndex)

                        val musicFile = MusicFiles(path, title, artist, album, duration, id)
                        musicFilesList.add(musicFile)
                    }
                } while (it.moveToNext())
            }
        }
        return musicFilesList
    }


    private fun setupRecyclerView(musicFiles: List<MusicFiles>) {
        val recyclerView = findViewById<RecyclerView>(R.id.songsRecyclerView) // Asegúrate de que este ID coincida con tu layout XML.

        val adapter = PlaylistAdapter(musicFiles) { musicFile, isChecked ->
            // Aquí manejas la selección de una canción, por ejemplo, actualizando una lista de seleccionados.
        }
        recyclerView.adapter = adapter
    }


}





