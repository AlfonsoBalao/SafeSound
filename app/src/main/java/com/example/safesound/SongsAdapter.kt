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

    private var currentlyPlayingPosition: Int = -1

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = view.findViewById(R.id.title)
        private val artistTextView: TextView = view.findViewById(R.id.artist)
        private val playingIndicator: View = view.findViewById(R.id.playing_indicator)

        fun bind(song: Song, isPlaying: Boolean, onSongClicked: (Song) -> Unit) {

            titleTextView.text = song.title
            artistTextView.text = song.artist
            playingIndicator.visibility = if (isPlaying) View.VISIBLE else View.GONE
            itemView.setOnClickListener { onSongClicked(song) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val isPlaying = position == currentlyPlayingPosition
        holder.bind(songs[position], isPlaying, onSongClicked)

    }

    override fun getItemCount() = songs.size

    fun updateSongs(newSongs: List<Song>) {
        (songs as MutableList<Song>).clear()
        (songs as MutableList<Song>).addAll(newSongs)
        notifyDataSetChanged()
    }

    fun setCurrentPlayingPosition(position: Int) {
        val previousPosition = currentlyPlayingPosition
        if (position != previousPosition) {
            currentlyPlayingPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)
        }

}
    }


