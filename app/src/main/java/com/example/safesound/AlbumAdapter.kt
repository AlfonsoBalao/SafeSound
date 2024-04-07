package com.example.safesound;

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class AlbumAdapter(
    private val mContext: Context,
    private val uniqueAlbums: ArrayList<MusicFiles>,
    private val allMusicFiles: ArrayList<MusicFiles>
) : RecyclerView.Adapter<AlbumAdapter.MyHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val album = uniqueAlbums[position]
        holder.albumName.text = album.album // Usamos `album` directamente
        val image: ByteArray? = MusicUtils.getAlbumArt(album.path) // `album.path` para obtener la imagen del álbum actual
        if (image != null) {
            Glide.with(mContext).asBitmap()
                .load(image)
                .into(holder.albumImage)
        } else {
            Glide.with(mContext)
                .load(R.drawable.null_cover)
                .into(holder.albumImage)
        }
        holder.itemView.setOnClickListener { v ->
            val albumSongs = allMusicFiles.filter { it.album == album.album } // Filtra todas las canciones del álbum seleccionado
            val intent = Intent(mContext, AlbumDetails::class.java).apply {
                putExtra("albumName", album.album)
                putParcelableArrayListExtra("albumSongs", ArrayList(albumSongs))
            }
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return uniqueAlbums.size // Usamos `uniqueAlbums.size` para el recuento de ítems
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumImage: ImageView = itemView.findViewById(R.id.album_image)
        var albumName: TextView = itemView.findViewById(R.id.album_name)
    }
}
