package com.example.safesound

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton


class PlayerActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener{

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

        songName.setText(songsList[position].title)
        artistName.setText(songsList[position].artist)
        mediaPlayer.setOnCompletionListener(this)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (::mediaPlayer.isInitialized && fromUser) {
                    mediaPlayer.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Código que se ejecuta cuando el usuario comienza a tocar el SeekBar.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Código que se ejecuta cuando el usuario deja de tocar el SeekBar.
            }
        })

    }

    override fun onResume() {
        playThreadBtn()
        nextThreadBtn()
        prevThreadBtn()
        super.onResume()
    }

    private fun prevThreadBtn() {
        prevThread = Thread {
            // En Kotlin, no es necesario llamar a super.run() ya que estamos
            // definiendo el comportamiento del hilo directamente.

            prevBtn.setOnClickListener {
                prevBtnClicked()
            }
        }
        prevThread.start()
    }

    private fun prevBtnClicked() {
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

    private fun nextThreadBtn() {
        nextThread = Thread {

            nextBtn.setOnClickListener {
                nextBtnClicked()
            }
        }
        nextThread.start()
    }

    private fun nextBtnClicked() {
        if (mediaPlayer.isPlaying) {

            mediaPlayer.stop()
            mediaPlayer.release()
            position = (position + 1) % songsList.size
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
            position = (position + 1) % songsList.size
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

    private fun playThreadBtn() {
        playThread = Thread {
            // En Kotlin, no es necesario llamar a super.run() ya que estamos
            // definiendo el comportamiento del hilo directamente.

            playPauseBtn.setOnClickListener {
                playPauseBtnClicked()
            }
        }
        playThread.start()
    }

    private fun playPauseBtnClicked() {
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
        songsList = intent.getParcelableArrayListExtra<MusicFiles>("musicFiles") ?: arrayListOf()

        if (songsList.isNotEmpty() && position in songsList.indices) {
            playPauseBtn.setImageResource(R.drawable.ic_pause)
            val uri = Uri.parse(songsList[position].path)

            // Inicializa mediaPlayer aquí si aún no se ha hecho
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
            lateinit var bitmap: Bitmap

            if (coverArtBytes != null) {
                /* Identificar y usar la paleta de colores recogida de la carátula para gradiente */
                bitmap =
                    coverArtBytes.let { BitmapFactory.decodeByteArray(coverArtBytes, 0, it.size)
                        }
                imageAnimation(this, coverArt, bitmap)
                Palette.from(bitmap).generate { palette ->
                    val swatch = palette?.dominantSwatch
                    if (swatch != null) {
                        val gradient: ImageView = findViewById(R.id.imageViewGradient)
                        val mContainer: RelativeLayout = findViewById(R.id.mContainer)
                        gradient.setBackgroundResource(R.drawable.gradient_bg)
                        mContainer.setBackgroundResource(R.drawable.main_bg)

                        val gradientDrawable = GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            intArrayOf(swatch.rgb, 0x00000000)
                        )
                        gradient.background = gradientDrawable

                        val gradientDrawableBg = GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            intArrayOf(swatch.rgb, swatch.rgb)
                        )

                        mContainer.background =
                            gradientDrawableBg

                        songName.setTextColor(swatch.titleTextColor)
                        artistName.setTextColor(swatch.bodyTextColor)

                    } else {
                        val gradient: ImageView = findViewById(R.id.imageViewGradient)
                        val mContainer: RelativeLayout = findViewById(R.id.mContainer)
                        gradient.setBackgroundResource(R.drawable.gradient_bg)
                        mContainer.setBackgroundResource(R.drawable.main_bg)

                        val gradientDrawable = GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            intArrayOf(0xff000000.toInt(), 0x00000000)
                        )
                        gradient.background = gradientDrawable

                        val gradientDrawableBg = GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            intArrayOf(0xff000000.toInt(), 0xff000000.toInt())
                        )

                        mContainer.background = gradientDrawableBg
                        songName.setTextColor(Color.WHITE)
                        artistName.setTextColor(Color.DKGRAY)
                    }
                }
                /* Fin del efecto gradiente */
            } else {
                Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.null_cover)
                    .into(coverArt)
                val gradient: ImageView = findViewById(R.id.imageViewGradient)
                val mContainer: RelativeLayout = findViewById(R.id.mContainer)
                gradient.setBackgroundResource(R.drawable.gradient_bg)
                mContainer.setBackgroundResource(R.drawable.main_bg)

                // gradiente si no hay bitmap en metadatos
                val defaultGradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    intArrayOf(0xff000000.toInt(), 0x00000000)
                )
                gradient.background = defaultGradientDrawable

                // fondo predeterminado si no hay bitmap en metadatos
                val defaultGradientDrawableBg = GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    intArrayOf(0xff000000.toInt(), 0xff000000.toInt())
                )
                mContainer.background = defaultGradientDrawableBg

                // colores de texto por defecto
                songName.setTextColor(Color.WHITE)
                artistName.setTextColor(Color.DKGRAY)

            }
        } catch (e: Exception) {
            // manejo de errores si el URI es inválido o si hay problemas al convertir la duración
            Toast.makeText(this, "Error al cargar metadatos", Toast.LENGTH_SHORT).show()
        } finally {
            retriever.release() //  libera el MediaMetadataRetriever para evitar fugas de memoria
        }
    }

    /* Animación fade in & fade out al cambiar de canción */
    fun imageAnimation(context: Context, imageView: ImageView, bitmap: Bitmap) {
        val animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
        val animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)

        animOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                Glide.with(context).load(bitmap).into(imageView)
                animIn.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {

                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
                imageView.startAnimation(animIn)
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        imageView.startAnimation(animOut)
    }
    /* *********************** Fin de animación *********************************/

    override fun onCompletion(mp: MediaPlayer?){
        nextBtnClicked()
        if (mediaPlayer != null){
            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener(this)
        }

    }
}





