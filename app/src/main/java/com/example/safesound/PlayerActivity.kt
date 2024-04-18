package com.example.safesound

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.example.safesound.models.LyricsApiService
import com.example.safesound.models.LyricsResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.random.Random
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlayerActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener, ActionPlaying, ServiceConnection {

    private lateinit var songName: TextView
    private lateinit var artistName: TextView
    private lateinit var playedDuration: TextView
    private lateinit var totalDuration: TextView
    private lateinit var coverArt: ImageView
    private lateinit var nextBtn: ImageView
    private lateinit var prevBtn: ImageView
    private lateinit var backBtn: ImageView
    private lateinit var shuffleBtn: ImageView
    private lateinit var repeatBtn: ImageView
    private lateinit var playPauseBtn: FloatingActionButton
    private lateinit var seekBar: SeekBar
    var position: Int = -1
    var songsList = arrayListOf<MusicFiles>()
    private lateinit var uri: Uri
    private lateinit var mediaPlayer: MediaPlayer
    private val handler = Handler()
    private lateinit var playThread: Thread
    private lateinit var prevThread: Thread
    private lateinit var nextThread: Thread
    var shuffling: Boolean = false;
    var repeating: Boolean = false;
    private var musicService: MusicService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mContainer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        getIntentMethod()

        val lyricsButton: FloatingActionButton = findViewById(R.id.fab_show_lyrics)
        lyricsButton.setOnClickListener {
            val currentSong = songsList[position]
            // Usar lifecycleScope para iniciar una coroutina
            lifecycleScope.launch {
                fetchAndDisplayLyrics(currentSong.artist, currentSong.title)
            }
        }


        songName.setText(songsList[position].title)
        artistName.setText(songsList[position].artist)
        mediaPlayer.setOnCompletionListener(this)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (::mediaPlayer.isInitialized && fromUser) {
                    mediaPlayer.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        shuffleBtn.setOnClickListener {
            if (shuffling) {
                shuffling = false
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_off)
            } else {
                shuffling = true
                shuffleBtn.setImageResource(R.drawable.ic_shuffle)
            }
        }
        repeatBtn.setOnClickListener {
            if (repeating) {
                repeating = false
                repeatBtn.setImageResource(R.drawable.ic_repeat_off)
            } else {
                repeating = true
                repeatBtn.setImageResource(R.drawable.ic_repeat_on)
            }
        }
    }

    override fun onResume() {
        val intent: Intent = Intent(this, MusicService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)

        playThreadBtn()
        nextThreadBtn()
        prevThreadBtn()
        super.onResume()
    }

    private fun prevThreadBtn() {

        prevBtn.setOnClickListener {
            prevBtnClicked()
        }
    }

    private fun nextThreadBtn() {
        nextBtn.setOnClickListener {
            nextBtnClicked()
        }
    }

    override fun prevBtnClicked() {
        if (mediaPlayer.isPlaying) {

            mediaPlayer.stop()
            mediaPlayer.release()
            position = if ((position - 1) < 0) songsList.size - 1 else position - 1
            uri = Uri.parse(songsList[position].path)
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            metaData(uri)
            songName.setText(songsList[position].title)
            artistName.setText(songsList[position].artist)
            seekBar.setMax(mediaPlayer.duration / 1000)

            runOnUiThread {
                if (::mediaPlayer.isInitialized) {
                    val mCurrentPosition = mediaPlayer.currentPosition / 1000
                    seekBar.progress = mCurrentPosition

                }
                handler.postDelayed({ updateSeekBar() }, 1000)
            }
            mediaPlayer.setOnCompletionListener(this)
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause)
            mediaPlayer.start()
        } else {
            mediaPlayer.stop()
            mediaPlayer.release()
            position = if ((position - 1) < 0) songsList.size - 1 else position - 1
            uri = Uri.parse(songsList[position].path)
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            metaData(uri)
            songName.setText(songsList[position].title)
            artistName.setText(songsList[position].artist)
            seekBar.setMax(mediaPlayer.duration / 1000)

            runOnUiThread {
                if (::mediaPlayer.isInitialized) {
                    val mCurrentPosition = mediaPlayer.currentPosition / 1000
                    seekBar.progress = mCurrentPosition

                }
                handler.postDelayed({ updateSeekBar() }, 1000)
            }
            mediaPlayer.setOnCompletionListener(this)
            playPauseBtn.setBackgroundResource(R.drawable.ic_play)
        }
    }

    override fun nextBtnClicked() {
        runOnUiThread {
            mediaPlayer.stop()
            mediaPlayer.release()

            if (shuffling) {
                position = randomizer(songsList.size - 1)
            } else if (!repeating) {
                position = (position + 1) % songsList.size
            }

            loadSong()
        }
    }

    private fun loadSong() {
        uri = Uri.parse(songsList[position].path)
        mediaPlayer = MediaPlayer.create(applicationContext, uri)
        metaData(uri)
        songName.setText(songsList[position].title)
        artistName.setText(songsList[position].artist)
        seekBar.setMax(mediaPlayer.duration / 1000)

        runOnUiThread {
            if (::mediaPlayer.isInitialized) {
                val mCurrentPosition = mediaPlayer.currentPosition / 1000
                seekBar.progress = mCurrentPosition
            }
            handler.postDelayed({ updateSeekBar() }, 1000)
        }
        mediaPlayer.setOnCompletionListener(this)
        playPauseBtn.setBackgroundResource(if (mediaPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        mediaPlayer.start()
    }

    private fun playThreadBtn() {
        playThread = Thread {


            playPauseBtn.setOnClickListener {
                playPauseBtnClicked()
            }
        }
        playThread.start()
    }

    override fun playPauseBtnClicked() {
        if (mediaPlayer.isPlaying) {

            playPauseBtn.setImageResource(R.drawable.ic_play)
            mediaPlayer.pause()
            seekBar.setMax(mediaPlayer.duration / 1000)

            runOnUiThread {
                if (::mediaPlayer.isInitialized) {
                    val mCurrentPosition = mediaPlayer.currentPosition / 1000
                    seekBar.progress = mCurrentPosition

                }
                handler.postDelayed({ updateSeekBar() }, 1000)
            }
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_pause)
            mediaPlayer.start()
            seekBar.setMax(mediaPlayer.duration / 1000)
            runOnUiThread {
                if (::mediaPlayer.isInitialized) {
                    val mCurrentPosition = mediaPlayer.currentPosition / 1000
                    seekBar.progress = mCurrentPosition

                }
                handler.postDelayed({ updateSeekBar() }, 1000)
            }
        }
    }

    private fun formattedTime(mCurrentPosition: Int): String {
        val seconds = (mCurrentPosition % 60).toString()
        val minutes = (mCurrentPosition / 60).toString()
        val totalOut = "$minutes:$seconds"
        val totalNew = "$minutes:0$seconds"
        return if (seconds.length == 1) {
            totalNew
        } else {
            totalOut
        }
    }

    private fun getIntentMethod() {
        position = intent.getIntExtra("position", -1)
        val sender = intent.getStringExtra("sender")


        if (sender != null && sender.equals("albumDetailsAdapter")){
            songsList = intent.getParcelableArrayListExtra<MusicFiles>("albumFiles") ?: arrayListOf()
        }
        else{
            songsList = intent.getParcelableArrayListExtra<MusicFiles>("musicFiles") ?: arrayListOf()
        }

        if (songsList.isNotEmpty() && position in songsList.indices) {
            playPauseBtn.setImageResource(R.drawable.ic_pause)
            val uri = Uri.parse(songsList[position].path)

            // inicia mediaPlayer aquí si aún no se ha hecho
            if (!::mediaPlayer.isInitialized) {
                mediaPlayer = MediaPlayer()
            }

            mediaPlayer.apply {
                stop()
                reset()
                setDataSource(applicationContext, uri)
                setOnPreparedListener {
                    start()
                    seekBar.max = mediaPlayer.duration / 1000
                    metaData(uri)
                    updateSeekBar()
                }
                prepareAsync()
            }


        } else {
            Toast.makeText(
                this,
                "La canción seleccionada no es válida o la lista está vacía.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateSeekBar() {
        runOnUiThread {
            if (::mediaPlayer.isInitialized) {
                val mCurrentPosition = mediaPlayer.currentPosition / 1000
                seekBar.progress = mCurrentPosition
                playedDuration.text = formattedTime(mCurrentPosition)
            }
            handler.postDelayed({ updateSeekBar() }, 1000)
        }
    }

    private fun initViews() {
        songName = findViewById(R.id.song_name)
        artistName = findViewById(R.id.artist_name)
        playedDuration = findViewById(R.id.playedDuration)
        totalDuration = findViewById(R.id.totalDuration)
        coverArt = findViewById(R.id.cover_art)
        nextBtn = findViewById(R.id.id_next)
        prevBtn = findViewById(R.id.id_prev)
        backBtn = findViewById(R.id.back_btn)
        shuffleBtn = findViewById(R.id.shuffle)
        repeatBtn = findViewById(R.id.id_repeat)
        playPauseBtn = findViewById(R.id.play_pause)
        seekBar = findViewById(R.id.seekBar)
    }

    private fun metaData(uri: Uri) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(this, uri)
            val total_duration = songsList[position].duration.toInt() / 1000
            totalDuration.text = formattedTime(total_duration)
            val coverArtBytes = retriever.embeddedPicture

            var bitmap: Bitmap = if (coverArtBytes != null) {
                BitmapFactory.decodeByteArray(coverArtBytes, 0, coverArtBytes.size)
            } else {
                // usaremos el archivo null_cover si no hay metadatos de imagen de portada
                BitmapFactory.decodeResource(resources, R.drawable.null_cover)
            }

            // animación exista en metadatos una imagen o sea la del archivo null_cover
            imageAnimation(this, coverArt, bitmap)

            // uso de la paleta para gradiente si existe la imagen o establecer colores predeterminados si no
            Palette.from(bitmap).generate { palette ->
                val swatch = palette?.dominantSwatch ?: return@generate
                val gradient: ImageView = findViewById(R.id.imageViewGradient)
                val mContainer: RelativeLayout = findViewById(R.id.mContainer)

                val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(swatch.rgb, 0x00000000)
                )
                gradient.background = gradientDrawable

                val gradientDrawableBg = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(swatch.rgb, swatch.rgb)
                )
                mContainer.background = gradientDrawableBg

                songName.setTextColor(swatch.titleTextColor)
                artistName.setTextColor(swatch.bodyTextColor)
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar metadatos", Toast.LENGTH_SHORT).show()
        } finally {
            retriever.release() //libera los recursos
        }
    }


    /* Animación fade in & fade out al cambiar de canción */
    fun imageAnimation(context: Context, imageView: ImageView, bitmap: Bitmap) {
        val animOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        val animIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)

        animOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                Glide.with(context).load(bitmap).into(imageView)
                animIn.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {}

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                imageView.startAnimation(animIn)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        imageView.startAnimation(animOut)
    }

    /* *********************** Fin de animación *********************************/

    override fun onCompletion(mp: MediaPlayer?) {
        if (repeating) {
            mediaPlayer.start() // repite la misma canción
        } else {
            nextBtnClicked() // va a la siguiente canción
        }
    }


    private fun randomizer(i: Int): Int {
        return Random.nextInt(i + 1)

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val myBinder: MusicService.MyBinder = service as MusicService.MyBinder
        musicService = myBinder.getService()

        Toast.makeText(this, "Conectado " + musicService, Toast.LENGTH_SHORT).show()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }


    override fun onPause(): Unit {
        super.onPause()
        unbindService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer.release()

        }
    }


    /*************** RETROFIT API CALL **************/

    private suspend fun fetchAndDisplayLyrics(artist: String, title: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.service.getLyrics(artist, title)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val lyrics = response.body()?.lyrics ?: "Letras no disponibles"
                        displayLyrics(lyrics)
                    } else {
                        Toast.makeText(applicationContext, "Error al obtener las letras", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error en la red", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun displayLyrics(lyrics: String) {
        // CÓMO SE MUESTRAN LOS LYRICS,AQUI
        AlertDialog.Builder(this)
            .setTitle("Letras")
            .setMessage(lyrics)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    /*********** RETROFIT API CALL TERMINADA ***********/



    /*************** RETROFIT CONFIGURACION **************/

    object RetrofitClient {
        private const val BASE_URL = "https://api.lyrics.ovh/"

        val service: LyricsApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LyricsApiService::class.java)
        }
    }




    /*********** RETROFIT CONFIGURACION FIN ***********/

}


