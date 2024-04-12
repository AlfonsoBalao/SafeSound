package com.example.safesound

import android.os.Bundle
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

    private fun setupRecyclerView(playlists: MutableList<PlayListEntity>) {
        playlistAdapter = DisplayListAdapter(playlists, this::editPlaylist, this::deletePlaylist)
        recyclerView.adapter = playlistAdapter
    }

    private fun editPlaylist(playlist: PlayListEntity) {

    }

    private fun deletePlaylist(playlist: PlayListEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            db.playlistDao().deletePlaylist(playlist.id)
            loadPlaylists()  // Recargar las listas después de borrar
        }
    }

    override fun onResume() {
        super.onResume()
        loadPlaylists()
    }

}
