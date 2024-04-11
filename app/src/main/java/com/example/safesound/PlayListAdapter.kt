package com.example.safesound

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaylistAdapter(
    private var songsList: List<MusicFiles>,
    private var onSongSelected: (MusicFiles, Boolean) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songTitle: TextView = view.findViewById(R.id.songTitle)
        val songArtist: TextView = view.findViewById(R.id.songArtist)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        // Inflar el layout para el ítem
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.playlist_item, parent, false)
        return SongViewHolder(view)

    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songsList[position]
        holder.songTitle.text = song.title
        holder.songArtist.text = song.artist

        // Desvincular el listener primero
        holder.checkBox.setOnCheckedChangeListener(null)
        // Cambiar el estado del checkBox
        holder.checkBox.isChecked = false
        // Volver a vincular el listener
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onSongSelected(song, isChecked)
        }
    }


    override fun getItemCount(): Int {
        return songsList.size
    }

}