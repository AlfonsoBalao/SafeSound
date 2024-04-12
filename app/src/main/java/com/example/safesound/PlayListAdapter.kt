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


    val selectedSongsId = mutableSetOf<String>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        // Inflar el layout para el ítem
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.playlist_item, parent, false)
        return SongViewHolder(view)

    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songsList[position]

        // desvinculo el listener para evitar llamadas redundantes
        holder.checkBox.setOnCheckedChangeListener(null)

        // seteo el estado del checkbox basado en si el ID de la canción está seleccionado de antes
        holder.checkBox.isChecked = selectedSongsId.contains(song.id)

        // titulo y artista
        holder.songTitle.text = song.title
        holder.songArtist.text = song.artist

        // listener de cambio
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedSongsId.add(song.id)
            } else {
                selectedSongsId.remove(song.id)
            }
            onSongSelected(song, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    fun setSelectedSongs(ids: List<String>) {
        selectedSongsId.clear()
        selectedSongsId.addAll(ids)
        notifyDataSetChanged() //
    }


    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songTitle: TextView = view.findViewById(R.id.songTitle)
        val songArtist: TextView = view.findViewById(R.id.songArtist)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
    }

}