package com.example.safesound

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DisplayListAdapter(
    private val playlists: MutableList<PlayListEntity>,
    private val onEdit: (PlayListEntity) -> Unit,
    private val onDelete: (PlayListEntity) -> Unit
) : RecyclerView.Adapter<DisplayListAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.displaylist_item, parent, false)
        return PlaylistViewHolder(view, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    class PlaylistViewHolder(
        itemView: View,
        private val onEdit: (PlayListEntity) -> Unit,
        private val onDelete: (PlayListEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.playlistName)
        private val countTextView: TextView = itemView.findViewById(R.id.songCount)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(playlist: PlayListEntity) {
            nameTextView.text = playlist.name
            countTextView.text = "Canciones: ${playlist.songCount}"

            editButton.setOnClickListener {
                val intent = Intent(itemView.context, CreatePlayListActivity::class.java).apply {
                    putExtra("PLAYLIST_ID", playlist.id)
                    putExtra("PLAYLIST_NAME", playlist.name)
                    putExtra("SONGS_IDS_JSON", playlist.songs)
                }
                itemView.context.startActivity(intent)
            }
            deleteButton.setOnClickListener { onDelete(playlist) }
        }
    }
}
