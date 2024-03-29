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

class MusicAdapter(private val mContext: Context, private val mFiles: ArrayList<MusicFiles>) :
    RecyclerView.Adapter<MusicAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false)
        return MyViewHolder(view)
    }

    //Carga las carátulas encontradas en los metadatos o asigna una por defecto si no existe
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.fileName.text = mFiles[position].title
        val image: ByteArray? = getAlbumArt(mFiles[position].path)
        if (image != null){
            Glide.with(mContext).asBitmap()  // -> sintaxis propia de la librería Glide
                .load(image)
                .into(holder .albumArt)
        }
        else{

            Glide.with(mContext)
                .load(R.drawable.null_cover)
                .into(holder.albumArt)

        }
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, PlayerActivity::class.java).apply {
                putExtra("position", position)
                putParcelableArrayListExtra("musicFiles", mFiles)
            }
            mContext.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return mFiles.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fileName: TextView = itemView.findViewById(R.id.music_file_name)
        var albumArt: ImageView = itemView.findViewById(R.id.music_img)
    }

    /*Usamos la clase de Android MediaDataRetriever para recuperar datos
    de los archivos multimedia */
    private fun getAlbumArt(uri: String): ByteArray? { // -> devolverá un array de bites (imagen)
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        val art = retriever.embeddedPicture
        retriever.release() // -> liberamos recursos del objeto MediaDataRetriever
        return art // -> devuelve los datos de la imagen / Si no existe, devuelve null.
    }


}




