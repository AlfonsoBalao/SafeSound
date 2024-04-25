package com.example.safesound


import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.TimeUtils.formatDuration
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList



class PlayListPlayerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistAdapter: SongsAdapter
    private lateinit var listNameTextView: TextView
    private lateinit var playPauseBtn: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var playedDuration: TextView
    private lateinit var totalDuration: TextView
    private lateinit var repeatBtn: ImageView
    private lateinit var shuffleBtn: ImageView
    var shuffling: Boolean = false;
    var repeating: Boolean = false;
    private var isReceiverRegistered = false



    private val handler = Handler(Looper.getMainLooper())


    /************* variables para vincular el servicio de reproducción ****************/

    private var musicService: MusicService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MyBinder
            musicService = binder.getService()
            isBound = true

            musicService?.setShuffleState(shuffling)
            musicService?.setRepeatState(repeating)


            musicService?.setSongChangeCallback(object : MusicService.SongChangeCallback {
                override fun onSongChanged(position: Int) {
                    onSongPlay(position)
                }
            })

            binder.setOnPreparedListener {
                updatePlayPauseButton()
                if (musicService?.isPlaying() == true) {
                    playPauseBtn.setImageResource(R.drawable.ic_pause)
                } else {
                    playPauseBtn.setImageResource(R.drawable.ic_play)
                }
                updateSeekBarWithCurrentSong()
                startUpdatingSeekBar()

        }

            updatePlayPauseButton()

            val playlistId = intent.getLongExtra("PLAYLIST_ID", -1L)
            Log.d("PlayListPlayerActivity", "serviceConnection: Check 1 de ID de lista -> $playlistId")
            if (playlistId != -1L) {
                loadSongs(playlistId)
                if (musicService?.songsList?.isNotEmpty() == true) {
                    updateSeekBarWithCurrentSong()
                    Log.e("PlayListPlayerActivity", "serviceConnection: Check 2 de ID de lista -> $playlistId")
                }
            } else {
                Log.e("PlayListPlayerActivity", "serviceConnection: ID de lista no válido o no proporcionado -> $playlistId")
            }


            if (musicService?.songsList?.isNotEmpty() == true) {
                updateSeekBarWithCurrentSong()
            }
        }  override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            musicService = null;
            updatePlayPauseButton()
        }
    }

    private fun updateSeekBarWithCurrentSong() {
        if (isBound && musicService != null) {
            val currentDuration = musicService?.getDuration() ?: 0
            seekBar.max = currentDuration
            totalDuration.text = formatDuration(currentDuration)
            handler.post(updateSeekBarTask)
        }}
    /*********************************************************************************/



    /*********************************** ON CREATE ***********************************/
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     /*   val filter = IntentFilter()
        filter.addAction("com.example.safesound.PLAYBACK_STATE_CHANGED")
        filter.addAction("com.example.safesound.MEDIAPLAYER_READY")
        registerReceiver(playbackStateReceiver, filter, RECEIVER_NOT_EXPORTED)*/


        enableEdgeToEdge()

        setContentView(R.layout.activity_play_list_player)
        seekBar = findViewById(R.id.seekBar)
        playedDuration = findViewById(R.id.playedDuration)
        totalDuration = findViewById(R.id.totalDuration)

        playPauseBtn = findViewById<ImageView>(R.id.play_pause).apply {
            setOnClickListener {
                togglePlayPause()
            }
        }
        configureSeekBar()

        //backBtn
        val backBtn = findViewById<ImageView>(R.id.back_btn)
        backBtn.setOnClickListener { onBackPressed() }


        findViewById<ImageView>(R.id.id_next).setOnClickListener {
            musicService?.nextSong()
        }

        findViewById<ImageView>(R.id.id_prev).setOnClickListener {
            musicService?.previousSong()
        }

        shuffleBtn = findViewById(R.id.shuffle)
        shuffleBtn.setOnClickListener {
            shuffling = !shuffling
            shuffleBtn.setImageResource(if (shuffling) R.drawable.ic_shuffle else R.drawable.ic_shuffle_off)
            Log.d("Shuffle State", "Shuffling ahora está en $shuffling")
            musicService?.setShuffleState(shuffling)
        }

        repeatBtn = findViewById(R.id.id_repeat)
        repeatBtn.setOnClickListener {
            repeating = !repeating
            repeatBtn.setImageResource(if (repeating)R.drawable.ic_repeat_on else R.drawable.ic_repeat_off)
            Log.d("Repeat State", "Repeating ahora está en $repeating")
            musicService?.setRepeatState(repeating)

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
            val songIndex = playlistAdapter.songs.indexOf(song)
            if (isBound && musicService != null) {
                if (songIndex != -1) {
                    musicService?.playSong(songIndex)
                    onSongPlay(songIndex)
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
            Log.d("PlayListPlayerActivity", "Seguimiento de Id de lista recibido playlistname: $playlistId")
        }

        if (albumCoverUrl != null) {
            Glide.with(this).load(albumCoverUrl).into(findViewById<ImageView>(R.id.cover_art))
            listNameTextView.text = "Reproduciendo lista: $playlistName" // -> establecer el nombre de la lista en el TextView
            Log.d("PlayListPlayerActivity", "Seguimiento de Id de lista recibido albumcoverurl: $playlistId")
        }

        if (playlistId != -1L && isBound && musicService != null) {
            loadSongs(playlistId)
            Log.d("PlayListPlayerActivity", "OnCreate: musicService activo y enlazado")
        }
                else if (playlistId != -1L && isBound) {
                Log.d("PlayListPlayerActivity", "OnCreate: musicService ENLAZADO PERO NO ACTIVO")
            } else if (playlistId != -1L && musicService != null) {
                Log.d("PlayListPlayerActivity", "OnCreate: musicService ACTIVO PERO NO ENLAZADO")

        } else {
            Log.d("PlayListPlayerActivity", "OnCreate: Id de lista recibido NO VÁLIDO -> $playlistId")
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

        if ((playlistId != -1L && isBound && musicService != null)) {
            loadSongs(playlistId)
        } else {
            Log.d("PlayListPlayerActivity", "SetupViews: ID de lista no válido o no proporcionado")
        }
    }


    private fun loadSongs(playlistId: Long) {
        if (!isBound) {
            Log.e("PlayListPlayerActivity", "El servicio aún no está vinculado.")
            return
        }

        if (musicService == null) {
            Log.e("PlayListPlayerActivity", "Servicio vinculado pero nulo.")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val songs = db.songDao().getSongsByPlaylistId(playlistId)
            runOnUiThread {
                playlistAdapter.updateSongs(songs)
                musicService?.setSongs(ArrayList(songs))
                updateSeekBar()
            }
        }
    }




    fun formatDuration(duration: Int): String {
            val minutes = duration / 1000 / 60
            val seconds = (duration / 1000) % 60
            return String.format("%d:%02d", minutes, seconds)
        }



    private fun updatePlaylistDetails() {
            val playlistId = intent.getLongExtra("PLAYLIST_ID", -1L)
            val playlistName = intent.getStringExtra("PLAYLIST_NAME")
            findViewById<TextView>(R.id.list_name).text = "Reproduciendo lista: $playlistName"
        }



    override fun onStart() {
        super.onStart()
        // enlace con el MusicService
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }


        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction("com.example.safesound.PLAYBACK_STATE_CHANGED")
                addAction("com.example.safesound.MEDIAPLAYER_READY")
            }
            registerReceiver(playbackStateReceiver, filter)
            isReceiverRegistered = true
            Log.d("PlayListPlayerActivity", "Registrando receiver")
        }


        if (isBound && musicService?.isPlaying() == true) {
            startUpdatingSeekBar()
        }
    }


    override fun onStop() {
        super.onStop()
        if (isReceiverRegistered) {
            unregisterReceiver(playbackStateReceiver)
            isReceiverRegistered = false
        }
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun togglePlayPause() {
            if (isBound && musicService != null) {
                if (musicService?.isPlaying() == true) {
                    musicService?.pauseSong()
                    playPauseBtn.setImageResource(R.drawable.ic_play)
                    stopUpdatingSeekBar()
                } else {
                    musicService?.resumeSong()
                    playPauseBtn.setImageResource(R.drawable.ic_pause)
                    startUpdatingSeekBar()
                }
                updatePlayPauseButton()
            }
        }

    private fun updatePlayPauseButton() {
            if (isBound && musicService?.isPlaying() == true) {
                playPauseBtn.setImageResource(R.drawable.ic_pause)
            } else {
                playPauseBtn.setImageResource(R.drawable.ic_play)
            }
        }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction("com.example.safesound.PLAYBACK_STATE_CHANGED")
            addAction("com.example.safesound.MEDIAPLAYER_READY")
        }
        registerReceiver(playbackStateReceiver, filter, RECEIVER_NOT_EXPORTED)
        isReceiverRegistered = true
        if (isBound && musicService?.isPlaying() == true) {
            startUpdatingSeekBar()
        }
    }
    override fun onDestroy() {
            super.onDestroy()
            if (isBound) {
                unbindService(serviceConnection)
                isBound = false

            }
        musicService = null

        }

    private fun configureSeekBar() {
            seekBar = findViewById<SeekBar>(R.id.seekBar)
            playedDuration = findViewById<TextView>(R.id.playedDuration)
            totalDuration = findViewById<TextView>(R.id.totalDuration)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        Log.d("SeekBar", "Progress: $progress")
                        val formattedDuration = formatDuration(progress)
                        Log.d("SeekBar", "Formatted Duration: $formattedDuration")
                        playedDuration.text = formattedDuration
                        musicService?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })
        }

    private fun stopUpdatingSeekBar() {
        handler.removeCallbacks(updateSeekBarTask)
    }

    override fun onPause() {
        super.onPause()

        if (isReceiverRegistered) {
            unregisterReceiver(playbackStateReceiver)
            isReceiverRegistered = false
        }

        stopUpdatingSeekBar()
    }


    private fun startUpdatingSeekBar() {

        handler.post(updateSeekBarTask)

    }
    private fun updateSeekBar() {
        if (isBound && musicService != null) {
            val currentPosition = musicService!!.getCurrentPosition()
            val totalDuration = musicService!!.getDuration()


            seekBar.max = totalDuration
            seekBar.progress = currentPosition

            this.totalDuration.text = formatDuration(totalDuration)
            playedDuration.text = formatDuration(currentPosition)

            //Log.d("MusicService", "UpdateSeekBar -> Posición actual: $currentPosition")
            //Log.d("MusicService", "UpdateSeekBar -> Duración total: $totalDuration")
        }
    }

    private val updateSeekBarTask = object : Runnable {

        override fun run() {
            //Log.d("SeekBar", "seekBar: se ha entrado en UpdateSeekBarTask")
            if (isBound && musicService?.isPlaying() == true) {
                val currentPosition = musicService?.getCurrentPosition() ?: 0
                val totalDuration = musicService?.getDuration() ?: 0

                seekBar.max = totalDuration
                seekBar.progress = currentPosition
                updateSeekBar()
                playedDuration.text = formatDuration(currentPosition)
                handler.postDelayed(this, 1000)
            } else {
                //Log.d("PlayListPlayerActivity", "UpdateSeekBarTask: MediaPlayer no funciona o no está enlazado.")
                handler.removeCallbacks(this)
            }
        }
    }


    private val playbackStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.safesound.PLAYBACK_STATE_CHANGED" -> {
                    val coverArtPath = intent.getStringExtra("coverArtPath" ?: "")
                    Log.d("PlayListPlayerActivity", "Ruta de carátula recibida: $coverArtPath")
                    if (coverArtPath != null) {
                        updateCoverArt(coverArtPath ?: "")
                    }
                    updateSeekBar()
                }
                "com.example.safesound.MEDIAPLAYER_READY" -> {
                    updatePlayPauseButton()
                    updateSeekBarWithCurrentSong()
                    if (musicService?.isPlaying() == true) {
                        startUpdatingSeekBar()
                    }
                }
            }
        }
    }

    fun updateCoverArt(imagePath: String) {
        Log.d("PlayListPlayerActivity", "Actualizando portada con ruta: $imagePath")
        val imageView = findViewById<ImageView>(R.id.cover_art)
        if (imagePath.isNotEmpty()) {
            Glide.with(this)
                .load(imagePath)
                .signature(ObjectKey(System.currentTimeMillis()))
                .error(R.drawable.null_cover)
                .into(imageView)
        } else {
            Glide.with(this)
                .load(R.drawable.null_cover)
                .into(imageView)
        }
    }

    fun onSongPlay(position: Int) {
        playlistAdapter.setCurrentPlayingPosition(position)

        playlistAdapter.notifyDataSetChanged()
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }

}
