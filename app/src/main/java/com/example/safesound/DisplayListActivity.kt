package com.example.safesound

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class DisplayListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistAdapter: DisplayListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_list)

        recyclerView = findViewById<RecyclerView>(R.id.playlistsRecyclerView)
        loadPlaylists()
    }

    private fun loadPlaylists() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val playlists = db.playlistDao().getAllPlaylists().toMutableList()
            launch(Dispatchers.Main) {
                setupRecyclerView(playlists)
            }
        }
    }

    private fun playPlaylist(playlist: PlayListEntity) {
        val intent = Intent(this, PlayListPlayerActivity::class.java).apply {

            putExtra("PLAYLIST_ID", playlist.id.toLong())
            putExtra("PLAYLIST_NAME", playlist.name)
        }
        Log.d("DisplayListActivity", "Id de la playlist enviada en el intent: ${playlist.id} ")
        startActivity(intent)
    }


    private fun setupRecyclerView(playlists: MutableList<PlayListEntity>) {
        playlistAdapter = DisplayListAdapter(playlists, this::editPlaylist, this::deletePlaylist, this::playPlaylist)
        recyclerView.adapter = playlistAdapter
    }

    private fun editPlaylist(playlist: PlayListEntity) {

    }

    private fun deletePlaylist(playlist: PlayListEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            db.playlistDao().deletePlaylist(playlist.id)
            loadPlaylists()  // -> recargar las listas después de borrar
        }
    }

    override fun onResume() {
        super.onResume()
        loadPlaylists()
    }

}
