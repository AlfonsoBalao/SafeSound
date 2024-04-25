package com.example.safesound
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "playlist")
data class PlayListEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val songs: String,
    val songCount: Int

)