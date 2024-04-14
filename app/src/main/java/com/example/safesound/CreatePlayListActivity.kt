package com.example.safesound

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Bundle
import android.provider.MediaStore
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
        Log.d("CreatePlayListActivity", "onCreate: iniciado - extras del intent -> ${intent.extras}")


        recyclerView = findViewById<RecyclerView>(R.id.songsRecyclerView)
        playlistAdapter = PlaylistAdapter(listOf(), this::onSongSelected)
        recyclerView.adapter = playlistAdapter
        Log.d("CreatePlayListActivity", "extras del intent: ${intent.extras}")

        var selectedSongsIds: List<String> = listOf()

        intent.extras?.let {
            Log.d("CreatePlayListActivity", "Extras recibidos")
            playlistId = it.getInt("PLAYLIST_ID", -1).takeIf { it != -1 }
            val playlistName = it.getString("PLAYLIST_NAME", "")
            val songsJson = it.getString("SONGS_IDS_JSON", "[]")

            Log.d("CreatePlayListActivity", "Intent Extras: ID: $playlistId, Name: $playlistName, SongsJSON: $songsJson")


            val type = object : TypeToken<List<String>>() {}.type
            val selectedSongsIds: List<String> = Gson().fromJson(songsJson, type)

            findViewById<EditText>(R.id.playlistName).setText(playlistName)

            loadMusicFiles { musicFiles ->
                Log.d("CreatePlayListActivity", "Canciones cargadas: ${musicFiles.size}")
                setupRecyclerView(musicFiles)
                playlistAdapter.setSelectedSongs(selectedSongsIds)
            }
        } ?: Log.d("CreatePlayListActivity", "No se ha provisto de extras a la actividad")

        loadMusicFiles { musicFiles ->
            Log.d("CreatePlayListActivity", "Canciones cargadas: ${musicFiles.size}")
            setupRecyclerView(musicFiles)
            playlistAdapter.setSelectedSongs(selectedSongsIds)}

        findViewById<Button>(R.id.addSongsButton).setOnClickListener {
            savePlaylist()

        }

    }

    private fun onSongSelected(musicFile: MusicFiles, isChecked: Boolean) {

    }


    private fun loadMusicFiles(onMusicFilesLoaded: (List<MusicFiles>) -> Unit) {
        Log.d("CreatePlayList", "Se ha llamado a loadMusicFiles")

        lifecycleScope.launch(Dispatchers.IO) {
            val musicFilesList = mutableListOf<MusicFiles>()
            val contentResolver = contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            val cursor = contentResolver.query(uri, null, selection, null, sortOrder)

            cursor?.use {
                val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)

                Log.d("CreatePlayList", "Se ha llegado al método LoadMusicFiles")

                while (it.moveToNext()) {
                    val title = it.getString(titleIndex)
                    val artist = it.getString(artistIndex)
                    val album = it.getString(albumIndex)
                    val duration = it.getLong(durationIndex)
                    val path = it.getString(dataIndex)
                    val id = it.getLong(idIndex)

                    val musicFile = MusicFiles(path, title, artist, album, duration.toString(), id.toString())
                    musicFilesList.add(musicFile)
                }
            }

            withContext(Dispatchers.Main) {
                onMusicFilesLoaded(musicFilesList)
            }
        }
    }


    private fun setupRecyclerView(musicFiles: List<MusicFiles>) {
        playlistAdapter = PlaylistAdapter(musicFiles, this::onSongSelected)
        recyclerView.adapter = playlistAdapter
    }

    private fun savePlaylist() {
        val playlistName = findViewById<EditText>(R.id.playlistName).text.toString()
        if (playlistName.isNotEmpty()) {
            val selectedSongsIds = playlistAdapter.selectedSongsId
            val songCount = selectedSongsIds.size
            val songIdsJson = Gson().toJson(selectedSongsIds)

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(applicationContext)
                val playlist = PlayListEntity(id = playlistId ?: 0, name = playlistName, songs = songIdsJson, songCount = songCount)
                if (playlistId == null) {
                    db.playlistDao().insertPlaylist(playlist)
                } else {
                    db.playlistDao().updatePlaylist(playlist)
                }
                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }
    }
}
