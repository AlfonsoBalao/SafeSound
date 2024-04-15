package com.example.safesound

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlin.random.Random

class MusicService : Service(), MediaPlayer.OnCompletionListener {

    private val binder = MyBinder()
    private var mediaPlayer: MediaPlayer? = null
    var songsList = arrayListOf<Song>()
    private var position = -1
    var isShuffling = false
    var isRepeating = false

    override fun onBind(intent: Intent?): IBinder {
        initializeMediaPlayer()
        return binder
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener(this@MusicService)
        }
    }

    fun setPlaylist(songs: ArrayList<Song>, startPosition: Int) {
        songsList = songs
        position = startPosition
        playSong(startPosition)
    }

    fun playSong(songPosition: Int) {
        if (songPosition >= 0 && songPosition < songsList.size) {
            mediaPlayer?.apply {
                reset()
                setDataSource(this@MusicService, Uri.parse(songsList[songPosition].path))
                prepare()
                start()
                Log.d("MusicService", "Reproduciendo canción: ${songsList[songPosition].title}")
            }
        } else {
            Log.e("MusicService", "Índice de canción fuera de rango: $songPosition, tamaño de lista: ${songsList.size}")
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
        } else {
            nextSong()
        }
    }

    fun nextSong() {
        if (isShuffling) {
            position = randomizer(songsList.size - 1)
        } else {
            position = (position + 1) % songsList.size
        }
        playSong(position)
    }

    fun previousSong() {
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


}
