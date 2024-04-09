package com.example.safesound

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log

class MusicService : Service() {

    val mBinder: IBinder = MyBinder(this)
    lateinit var mediaPLayer: MediaPlayer
    val musicFiles: ArrayList<MusicFiles> = ArrayList()

    override fun onBind(intent: Intent?): IBinder {
        Log.d("MusicService", "método onBind llamado ")
        return mBinder
    }

    class MyBinder(private val service: MusicService) : Binder() {
        fun getService(): MusicService {
            return service
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }



}