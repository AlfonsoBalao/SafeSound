package com.example.safesound

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaylistAdapter(
    private var songsList: List<Song>,
    private var onSongSelected: (Song, Boolean) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.SongViewHolder>() {

    val selectedSongsId = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songsList[position]
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selectedSongsId.contains(song.id)
        holder.songTitle.text = song.title
        holder.songArtist.text = song.artist

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedSongsId.add(song.id)
            } else {
                selectedSongsId.remove(song.id)
            }
            onSongSelected(song, isChecked)
        }
    }

    override fun getItemCount(): Int = songsList.size

    fun setSelectedSongs(ids: List<Long>) {
        selectedSongsId.clear()
        selectedSongsId.addAll(ids)
        notifyDataSetChanged()
    }

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songTitle: TextView = view.findViewById(R.id.songTitle)
        val songArtist: TextView = view.findViewById(R.id.songArtist)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
    }
}
