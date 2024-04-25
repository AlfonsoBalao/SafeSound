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
    val duration: Int, //-> duración en segundos
    val year: String, //-> año de lanzamiento
    val albumCoverUrl: String, //-> URL de la portada del álbum
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT)
    val lyrics: String, // -> letra de la canción
)

