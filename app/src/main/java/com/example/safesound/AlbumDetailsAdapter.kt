package com.example.safesound

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class AlbumDetailsAdapter(
    private val mContext: Context, private val albumFiles: ArrayList<MusicFiles>
) : RecyclerView.Adapter<AlbumDetailsAdapter.MyHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false)
        return MyHolder(view)
    }


    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.album_name.text = albumFiles[position].title
        val image: ByteArray? = MusicUtils.getAlbumArt(albumFiles[position].path)
        if (image != null) {
            Glide.with(mContext).asBitmap()  // -> sintaxis propia de la librería Glide
                .load(image).into(holder.album_image)
        } else {

            Glide.with(mContext).load(R.drawable.null_cover).into(holder.album_image)

        }
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, PlayerActivity::class.java).apply {
                putExtra("position", holder.adapterPosition) // -> manda la posición correcta
                putExtra("sender", "albumDetailsAdapter") // -> identificador para saber desde dónde se inició
                putParcelableArrayListExtra("albumFiles", albumFiles) // -> manda la lista de canciones
            }
            mContext.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return albumFiles.size
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var album_image: ImageView = itemView.findViewById(R.id.music_img)
        var album_name: TextView = itemView.findViewById(R.id.music_file_name)
    }
}



