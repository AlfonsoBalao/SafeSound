package com.example.safesound

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index


@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    indices = [
        Index(value = ["songId"]),
        Index(value = ["playlistId"])
    ],
    foreignKeys = [
        ForeignKey(entity = PlayListEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE)
    ]

)
data class PlaylistSong(
    val playlistId: Long,
    val songId: Long
)
