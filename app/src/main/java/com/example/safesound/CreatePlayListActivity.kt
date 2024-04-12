package com.example.safesound
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CreatePlayListActivity : AppCompatActivity() {

    private var playlistId: Int? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_play_list)

        intent.extras?.let {
            playlistId = it.getInt("PLAYLIST_ID", -1).takeIf { it != -1 }
            val playlistName = it.getString("PLAYLIST_NAME", "")
            val songsJson = it.getString("SONGS_IDS_JSON", "[]")
            val type = object : TypeToken<List<String>>() {}.type
            val selectedSongsIds: List<String> = Gson().fromJson(songsJson, type)

            findViewById<EditText>(R.id.playlistName).setText(playlistName)


            val musicFiles = loadMusicFiles()
            setupRecyclerView(musicFiles)
            playlistAdapter.setSelectedSongs(selectedSongsIds)
        }
            val addButton = findViewById<Button>(R.id.addSongsButton)
            addButton.setOnClickListener {
                savePlaylist()


        }
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
        recyclerView = findViewById<RecyclerView>(R.id.songsRecyclerView)

        playlistAdapter= PlaylistAdapter(musicFiles) { musicFile, isChecked ->
            // Aquí manejas la selección de una canción, por ejemplo, actualizando una lista de seleccionados.
        }
        recyclerView.adapter = playlistAdapter
    }



    /*private fun savePlaylist() {
        val playlistNameEditText = findViewById<EditText>(R.id.playlistName)
        val playlistName = playlistNameEditText.text.toString()

        if (playlistName.isNotEmpty()) {
            val selectedSongsIds = (recyclerView.adapter as PlaylistAdapter).selectedSongsId
            val songCount = selectedSongsIds.size
            val songIdsJson = Gson().toJson(selectedSongsIds)

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(applicationContext)
                if (playlistId == null) {
                    // Crear nueva lista
                    val playlist = PlayListEntity(name = playlistName, songs = songIdsJson, songCount = songCount)
                    db.playlistDao().insertPlaylist(playlist)
                } else {
                    // Actualizar lista existente
                    val playlist = PlayListEntity(id = playlistId!!, name = playlistName, songs = songIdsJson, songCount = songCount)
                    db.playlistDao().updatePlaylist(playlist)
                }
                launch(Dispatchers.Main) {
                    finish()  // Cerrar esta actividad y volver a la anterior
                }
            }
        }
    }*/


    private fun savePlaylist() {
        val playlistNameEditText = findViewById<EditText>(R.id.playlistName)
        val playlistName = playlistNameEditText.text.toString()

        if (playlistName.isNotEmpty()) {
            val selectedSongsIds = (recyclerView.adapter as PlaylistAdapter).selectedSongsId
            val songCount = selectedSongsIds.size
            val songIdsJson = Gson().toJson(selectedSongsIds)

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(applicationContext)
                if (playlistId == null) {
                    // Crear nueva lista
                    val playlist = PlayListEntity(name = playlistName, songs = songIdsJson, songCount = songCount)
                    db.playlistDao().insertPlaylist(playlist)
                } else {
                    // Actualizar lista existente
                    val playlist = PlayListEntity(id = playlistId!!, name = playlistName, songs = songIdsJson, songCount = songCount)
                    db.playlistDao().updatePlaylist(playlist)
                }
                launch(Dispatchers.Main) {
                    finish()  // Cerrar esta actividad y volver a la anterior
                }
            }
        }
    }
}





