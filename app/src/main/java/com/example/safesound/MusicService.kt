package com.example.safesound

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlin.random.Random
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
//import kotlin.coroutines

class MusicService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private val binder = MyBinder()
    private var currentSongPath: String? = null
    private var mediaPlayer: MediaPlayer? = null
    var songsList = arrayListOf<Song>()
    private var position = -1
    var isShuffling = false
    var isRepeating = false
    private var onMediaPlayerPreparedListener: (() -> Unit)? = null


    override fun onBind(intent: Intent?): IBinder {
        initializeMediaPlayer()
        return binder
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicService = this@MusicService
        fun setOnPreparedListener(listener: () -> Unit) {
            onMediaPlayerPreparedListener = listener
        }
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener(this@MusicService)
            setOnPreparedListener(this@MusicService)
        }
    }

    fun playSong(songPosition: Int) {
        val song = songsList.getOrNull(songPosition)
        if (song != null) {
            Log.d("MusicService", "Canción sonando: ${song.path}")
            try {
                currentSongPath = song.path
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(this, Uri.parse(song.path))
                mediaPlayer?.prepareAsync()
                sendSongInfoToUI(song.path)
            } catch (e: Exception) {
                Log.e("MusicService", "Error estableciendo la fuente de datos", e)
            }
        }
    }




    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()
        onMediaPlayerPreparedListener?.invoke()
        currentSongPath?.let {
            Log.d("MusicService", "onPrepared: mandando información de canción para $it")
            sendSongInfoToUI(it)
        }

        val intentReady = Intent("com.example.safesound.MEDIAPLAYER_READY")
        sendBroadcast(intentReady)
    }


    private fun sendSongInfoToUI(songPath: String) {
        val coverArtPath = getCoverArtPath(songPath, applicationContext)
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            val duration =
                mediaPlayer!!.duration
            val intent = Intent("com.example.safesound.PLAYBACK_STATE_CHANGED")
            intent.putExtra("duration", duration)
            intent.putExtra("coverArtPath", coverArtPath)
            sendBroadcast(intent)
        }
    }

    fun pauseSong() {
        mediaPlayer?.pause()
    }

    fun resumeSong() {
        mediaPlayer?.start()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (isRepeating) {
            playSong(position)
        } else if (isShuffling) {
            playSong(Random.nextInt(songsList.size))
        } else {
            nextSong()
        }
    }

    fun nextSong() {
        if (songsList.isEmpty()) {
            Log.e("MusicService", "La lista de canciones está vacía")
            return
        }
        position = when {
            isRepeating -> position
            isShuffling -> randomizer(songsList.size - 1)
            else -> (position + 1) % songsList.size
        }

        playSong(position)
        songChangeCallback?.onSongChanged(position)
    }





    fun previousSong() {
        if (songsList.isEmpty()) {
            Log.e("MusicService", "La lista de canciones está vacía")
            return
        }
        position = if (position - 1 < 0) songsList.size - 1 else position - 1
        playSong(position)
        songChangeCallback?.onSongChanged(position)

    }


    fun setSongs(songs: ArrayList<Song>) {
        songsList = songs
    }

    private fun randomizer(i: Int): Int = Random.nextInt(i + 1)

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null

    }

    fun seekTo(progress: Int) {
        if (mediaPlayer != null && progress >= 0) {
            mediaPlayer?.seekTo(progress)
        }
    }

    fun setOnPreparedListener(function: () -> Unit) {

    }

    private fun getCoverArtPath(songPath: String, context: Context): String {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(songPath)
            val artBytes = retriever.embeddedPicture
            if (artBytes != null) {
                val imageFile = File(context.cacheDir, "cover_art_${System.currentTimeMillis()}.jpg")
                FileOutputStream(imageFile).use { fos ->
                    fos.write(artBytes)
                }

                return imageFile.absolutePath
            }
        } catch (e: Exception) {
            //Log.e("MusicService", "Error recuperando carátula", e)

        } finally {
            retriever.release()
        }
        return ""
    }

    interface SongChangeCallback {
        fun onSongChanged(position: Int)
    }

    private var songChangeCallback: SongChangeCallback? = null

    fun setSongChangeCallback(callback: SongChangeCallback) {
        this.songChangeCallback = callback
    }


    fun setShuffleState(isShuffle: Boolean) {
        isShuffling = isShuffle
        Log.d("MusicService", "Modo shuffle establecido en $isShuffling")
    }


    fun setRepeatState(isRepeat: Boolean) {
        isRepeating = isRepeat
        Log.d("MusicService", "Modo repeat establecido en $isRepeating")
    }

}



