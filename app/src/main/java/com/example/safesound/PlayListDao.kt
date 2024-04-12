package com.example.safesound

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface PlayListDao {
    @Insert
    suspend fun insertPlaylist(playlist: PlayListEntity)

    @Query("SELECT * FROM playlist")
    suspend fun getAllPlaylists(): List<PlayListEntity>

    @Query("DELETE FROM playlist WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Int)

    @Update
    suspend fun updatePlaylist(playlist: PlayListEntity)
}