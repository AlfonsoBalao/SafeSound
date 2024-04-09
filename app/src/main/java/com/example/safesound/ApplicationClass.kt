package com.example.safesound

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context


class ApplicationClass : Application() {
    companion object { // -> manera de definir constantes estáticas en kotlin asociadas a clase
        const val CHANNEL_1 = "channel1"
        const val CHANNEL_2 = "channel2"
        const val ACTION_PREV = "actionPrev"
        const val ACTION_NEXT = "actionNext"
        const val ACTION_PLAY = "actionPlay"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }


    fun createNotificationChannel(): Unit{ //-> creamos dos canales de notificación

        val channel1: NotificationChannel = NotificationChannel(CHANNEL_1, "Channel(1)", NotificationManager.IMPORTANCE_HIGH)
        channel1.description = "Channel 1 Description"
        val channel2: NotificationChannel = NotificationChannel(CHANNEL_2, "Channel(2)", NotificationManager.IMPORTANCE_HIGH)
        channel2.description = "Channel 2 Description"

        val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel1)
        notificationManager.createNotificationChannel(channel2)

    }
}