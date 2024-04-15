package com.example.safesound

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.withContext

class CreatePlayListActivity : AppCompatActivity() {

    private var playlistId: Int? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_play_list)
        recyclerView = findViewById<RecyclerView>(R.id.songsRecyclerView)
        playlistAdapter = PlaylistAdapter(listOf(), this::onSongSelected)
        recyclerView.adapter = playlistAdapter

        Log.d("CreatePlaylistActivity", "onCreate llamado")

        var selectedSongsIds: List<Long> = listOf()

        intent.extras?.let {
            Log.d("CreatePlaylistActivity", "los extras del intent NO son nulos")
            playlistId = it.getInt("PLAYLIST_ID", -1).takeIf { it != -1 }
            val playlistName = it.getString("PLAYLIST_NAME", "")
            val songsJson = it.getString("SONGS_IDS_JSON", "[]")
            val type = object : TypeToken<List<Long>>() {}.type
            selectedSongsIds = Gson().fromJson(songsJson, type)
            findViewById<EditText>(R.id.playlistName).setText(playlistName)
            loadMusicFiles { songs ->
                Log.d("CreatePlaylistActivity", "callback a loadMusicFiles efectuado")
                setupRecyclerView(songs)
                playlistAdapter.setSelectedSongs(selectedSongsIds)
            }
        } ?: Log.d("CreatePlaylistActivity", "los extras del intent SON nulos")

        findViewById<Button>(R.id.addSongsButton).setOnClickListener {
            savePlaylist()
        }
    }

    private fun onSongSelected(song: Song, isChecked: Boolean) {

    }

    private fun loadMusicFiles(onSongsLoaded: (List<Song>) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {

            val db = AppDatabase.getInstance(applicationContext)
            val songsList = db.songDao().getAllSongs()
            Log.d("CreatePlaylistActivity", "Cargadas ${songsList.size} canciones desde la base de datos")
            withContext(Dispatchers.Main) {
                onSongsLoaded(songsList)
            }
        }
    }

    private fun setupRecyclerView(songs: List<Song>) {
        playlistAdapter = PlaylistAdapter(songs, this::onSongSelected)
        recyclerView.adapter = playlistAdapter
        playlistAdapter.notifyDataSetChanged()
    }


    private fun savePlaylist() {
        val playlistName = findViewById<EditText>(R.id.playlistName).text.toString()
        if (playlistName.isNotEmpty()) {
            val selectedSongsIds = playlistAdapter.selectedSongsId.toList() //-> convertir a lista para JSON
            val songCount = selectedSongsIds.size
            val songIdsJson = Gson().toJson(selectedSongsIds)
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(applicationContext)
                val playlist = PlayListEntity(id = playlistId ?: 0, name = playlistName, songs = songIdsJson, songCount = songCount)

                if (playlistId == null) {
                val newId = db.playlistDao().insertPlaylist(playlist)
                    //Vamos a poblar la tabla intermedia ahora
                    selectedSongsIds.forEach { songId ->
                        val playlistSong = PlaylistSong(playlistId = newId, songId = songId)
                        db.songDao().insertPlaylistSong(playlistSong)
                        playlistId = newId.toInt()
                    }
                } else {
                    //Vamos a actualizar la tabla intermedia ahora
                    val updatePlaylist = PlayListEntity(id = playlistId!!, name = playlistName, songs = "", songCount = songCount)
                    db.playlistDao().updatePlaylist(updatePlaylist) // -> Actualiza aquí
                    db.songDao().deleteSongsFromPlaylist(playlistId!!) // -> Elimina asociaciones antiguas obsoletas

                    selectedSongsIds.forEach { songId ->
                        db.songDao().insertPlaylistSong(PlaylistSong(playlistId = playlistId!!.toLong(), songId = songId)) // -> Reinsertar nuevas canciones
                    }

                }
                withContext(Dispatchers.Main) {
                    finish()
                    val intent = Intent(this@CreatePlayListActivity, DisplayListActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

}

