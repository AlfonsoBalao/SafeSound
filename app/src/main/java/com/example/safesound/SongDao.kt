package com.example.safesound

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface SongDao {
    @Insert
    suspend fun insertSong(song: Song): Long

    @Insert
    suspend fun insertPlaylistSong(playlistSong: PlaylistSong)


    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<Song>

    @Query("SELECT s.* FROM songs s JOIN playlist_songs ps ON s.id = ps.songId WHERE ps.playlistId = :playlistId")
    suspend fun getSongsByPlaylistId(playlistId: Long): List<Song>


    @Query("SELECT EXISTS(SELECT 1 FROM songs WHERE title = :title AND artist = :artist AND album = :album)")
    suspend fun isSongExists(title: String, artist: String, album: String): Boolean

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun deleteSongsFromPlaylist(playlistId: Int)


}
