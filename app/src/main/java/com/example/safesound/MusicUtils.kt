package com.example.safesound

import android.media.MediaMetadataRetriever
import android.util.Log

object MusicUtils {

    fun getAlbumArt(uri: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(uri)
            val art = retriever.embeddedPicture
            retriever.release()
            art
        } catch (e: Exception) {
            Log.e("MusicUtils", "Error al obtener imagen incrustada", e)
            null
        }
    }

}
