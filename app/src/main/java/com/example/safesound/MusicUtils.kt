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


 fun getUniqueAlbums(musicFiles: ArrayList<MusicFiles>): ArrayList<MusicFiles> {
        // filtra para no repetir el mismo álbum después en el fragment
        return musicFiles.distinctBy { it.album }.toCollection(ArrayList())
    }





}
