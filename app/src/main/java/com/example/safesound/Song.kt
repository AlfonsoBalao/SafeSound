package com.example.safesound

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val path: String,
    val duration: Int, // Duración en segundos
    val year: String, // Año de lanzamiento
    val albumCoverUrl: String, // URL de la portada del álbum
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT) // Asegura que se maneje como texto largo
    val lyrics: String, // Letra de la canción
)

