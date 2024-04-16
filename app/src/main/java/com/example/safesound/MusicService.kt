package com.example.safesound

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlin.random.Random

class MusicService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private val binder = MyBinder()
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
        val song = songsList.getOrNull(songPosition) ?: return
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(this, Uri.parse(song.path))
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            Log.e("MusicService", "Error estableciendo la fuente de datos", e)
        }
    }



    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()
        onMediaPlayerPreparedListener?.invoke()
        sendSongInfoToUI()
        // Notificar que MediaPlayer está listo
        val intentReady = Intent("com.example.safesound.MEDIAPLAYER_READY")
        sendBroadcast(intentReady)
    }


    private fun sendSongInfoToUI() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            val duration =
                mediaPlayer!!.duration
            val intent = Intent("com.example.safesound.PLAYBACK_STATE_CHANGED")
            intent.putExtra("duration", duration)
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
        position = if (isShuffling) {
            randomizer(songsList.size - 1)
        } else {
            (position + 1) % songsList.size
        }
        playSong(position)
    }

    fun previousSong() {
        if (songsList.isEmpty()) {
            Log.e("MusicService", "La lista de canciones está vacía")
            return
        }
        position = if (position - 1 < 0) songsList.size - 1 else position - 1
        playSong(position)
    }


    fun setSongs(songs: ArrayList<Song>) {
        songsList = songs
    }

    private fun randomizer(i: Int): Int = Random.nextInt(i + 1)

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    fun seekTo(progress: Int) {
        if (mediaPlayer != null && progress >= 0) {
            mediaPlayer?.seekTo(progress)
        }
    }

    fun setOnPreparedListener(function: () -> Unit) {

    }


}
