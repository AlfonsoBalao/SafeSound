package com.example.safesound

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongsAdapter(
    var songs: MutableList<Song>,
    private val onSongClicked: (Song) -> Unit)
    : RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = view.findViewById(R.id.title)
        private val artistTextView: TextView = view.findViewById(R.id.artist)

        fun bind(song: Song, onSongClicked: (Song) -> Unit) {
            Log.d("SongsAdapter", "Binding song: ${song.title}")
            titleTextView.text = song.title
            artistTextView.text = song.artist
            itemView.setOnClickListener { onSongClicked(song) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position], onSongClicked)
    }

    override fun getItemCount() = songs.size

    fun updateSongs(newSongs: List<Song>) {
        (songs as MutableList<Song>).clear()
        (songs as MutableList<Song>).addAll(newSongs)
        notifyDataSetChanged()
    }
}


