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
    private val albumFiles: ArrayList<MusicFiles>
) : RecyclerView.Adapter<AlbumAdapter.MyHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.albumName.text = albumFiles[position].album
        val image: ByteArray? = MusicUtils.getAlbumArt(albumFiles[position].path)
        if (image != null) {
            Glide.with(mContext).asBitmap()  // -> sintaxis propia de la librería Glide
                .load(image)
                .into(holder.albumImage)
        } else {

            Glide.with(mContext)
                .load(R.drawable.null_cover)
                .into(holder.albumImage)

        }
        holder.itemView.setOnClickListener { v ->
            val intent = Intent(mContext, AlbumDetails::class.java).apply {
                putExtra("albumName", albumFiles[position].album)

                putParcelableArrayListExtra("albumSongs", albumFiles)
            }
            mContext.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return albumFiles.size
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumImage: ImageView = itemView.findViewById(R.id.album_image)
        var albumName: TextView = itemView.findViewById(R.id.album_name)
    }
}

/*
private fun getAlbumArt(uri: String): ByteArray? { // -> devolverá un array de bites (imagen)
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(uri)
    val art = retriever.embeddedPicture
    retriever.release() // -> liberamos recursos del objeto MediaDataRetriever
    return art // -> devuelve los datos de la imagen / Si no existe, devuelve null.
}


*/
