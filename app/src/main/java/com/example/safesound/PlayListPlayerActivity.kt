package com.example.safesound


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class PlayListPlayerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistAdapter: SongsAdapter
    private lateinit var listNameTextView: TextView

    /************* variables para vincular el servicio de reproducción ****************/

    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MyBinder
            musicService = binder.getService()
            isBound = true

            // ahora el servicio debe estar vinculado
        val playlistId = intent.getLongExtra("PLAYLIST_ID", -1L)
        if (playlistId != -1L) {
            loadSongs(playlistId)
        } else {
            Log.e("PlayListPlayerActivity", "ID de lista no válido o no proporcionado en onServiceConnected")
        }
    }

    override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }
    /*********************************************************************************/



    /*********************************** ON CREATE ***********************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play_list_player)

        findViewById<ImageView>(R.id.play_pause).setOnClickListener {
            if (isBound && musicService?.isPlaying() == true) {
                musicService?.pauseSong()
            } else if (isBound && musicService?.songsList?.isNotEmpty() == true) {
                musicService?.resumeSong()
            } else {
                Log.e("PlayListPlayerActivity", "Intento de usar MusicService no vinculado, o no hay canciones disponibles")
            }
        }

        findViewById<ImageView>(R.id.id_next).setOnClickListener {
            musicService?.nextSong()
        }

        findViewById<ImageView>(R.id.id_prev).setOnClickListener {
            musicService?.previousSong()
        }

        findViewById<ImageView>(R.id.shuffle).setOnClickListener {
            musicService?.isShuffling = !(musicService?.isShuffling ?: false)
        }

        findViewById<ImageView>(R.id.id_repeat).setOnClickListener {
            musicService?.isRepeating = !(musicService?.isRepeating ?: false)
        }

        updatePlaylistDetails() // -> interfaz de usuario con los datos de la lista de reproducción

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById<RecyclerView>(R.id.playlistsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        playlistAdapter = SongsAdapter(mutableListOf()) { song ->
            if (isBound) {
                val songIndex = playlistAdapter.songs.indexOf(song)
                if (songIndex != -1) {
                    musicService?.playSong(songIndex)
                } else {
                    Log.e("PlayListPlayerActivity", "Índice de canción no válido")
                }

        }
            musicService?.playSong(playlistAdapter.songs.indexOf(song))
        }
        recyclerView.adapter = playlistAdapter

        setupViews()
        setupRecyclerView()

        listNameTextView = findViewById<TextView>(R.id.list_name)

        val playlistId = intent.getLongExtra("PLAYLIST_ID", -1)
        Log.d("PlayListPlayerActivity", "Id de lista recibido: $playlistId")

        val albumCoverUrl = intent.getStringExtra("ALBUM_COVER_URL")
        val playlistName = intent.getStringExtra("PLAYLIST_NAME") // -> recupera el nombre de la lista de reproducción del Intent
        if (playlistName != null) {
            listNameTextView.text = "Reproduciendo lista: $playlistName" // -> establecer el nombre de la lista en el TextView
        }

        if (albumCoverUrl != null) {
            Glide.with(this).load(albumCoverUrl).into(findViewById<ImageView>(R.id.cover_art))
        }

        if (playlistId != -1L) {
            loadSongs(playlistId)
        } else {
            Log.d("PlayListPlayerActivity", "Id de lista recibido NO VÁLIDO")
        }
        bindService(Intent(this, MusicService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /************************************ ON CREATE FIN ****************************************/
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }



    private fun setupViews() {
        // Obtener el ID de la lista de reproducción del intent
        val playlistId = intent.getLongExtra("PLAYLIST_ID", -1L)

        if (playlistId != -1L) {
            loadSongs(playlistId)
        } else {
            Log.d("PlayListPlayerActivity", "ID de lista no válido o no proporcionado")
        }
    }


    private fun loadSongs(playlistId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val songs = db.songDao().getSongsByPlaylistId(playlistId)
            Log.d("PlayListPlayerActivity", "Cargadas ${songs.size} canciones para la lista con id: $playlistId")
            runOnUiThread {
                playlistAdapter.updateSongs(songs)
                if (isBound && songs.isNotEmpty()) {
                    musicService?.setSongs(ArrayList(songs)) // -> cargar las canciones en el servicio
                } else {
                    Log.e("PlayListPlayerActivity", "El servicio no está vinculado o la lista de canciones está vacía")
                }
            }
        }
    }



    private fun updatePlaylistDetails() {
        val playlistId = intent.getLongExtra("PLAYLIST_ID", -1L)
        val playlistName = intent.getStringExtra("PLAYLIST_NAME")
        findViewById<TextView>(R.id.list_name).text = "Reproduciendo lista: $playlistName"
    }



    override fun onStart() {
        super.onStart()
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onResume() {
        super.onResume()

    }
    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

}