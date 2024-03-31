package com.example.safesound

/* ***************************************************************
Este archivo contiene la definición de la clase Musicfiles, que se
utiliza para representar los archivos de música, y que almacena
la información de cada archivo de música que encuentre.
**************************************************************** */


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicFiles(
    val path: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val id: String
) : Parcelable


