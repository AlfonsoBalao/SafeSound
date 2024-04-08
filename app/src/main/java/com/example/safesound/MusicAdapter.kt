package com.example.safesound

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import java.io.File

class MusicAdapter(private val mContext: Context, private val mFiles: ArrayList<MusicFiles>) :
    RecyclerView.Adapter<MusicAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false)
        return MyViewHolder(view)
    }

    //Carga las carátulas encontradas en los metadatos o asigna una por defecto si no existe
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.fileName.text = mFiles[position].title
        val image: ByteArray? = MusicUtils.getAlbumArt(mFiles[position].path)
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
        holder.menuMore.setOnClickListener { view ->
            val popupMenu = PopupMenu(mContext, view)
            popupMenu.inflate(R.menu.popup) // Simplificación del inflado del menú
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.deleteFile -> {
                        Toast.makeText(mContext, "Clicado Borrar", Toast.LENGTH_SHORT).show()
                        deleteFile(position, view)
                        true // Maneja el evento del clic
                    }
                    else -> false
                }
            }
        }
    }

    private fun deleteFile(position: Int, view: View) {
        val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mFiles[position].id.toLong())
        val deletedRows = mContext.contentResolver.delete(contentUri, null, null)
        if (deletedRows > 0) {
            val file: File = File(mFiles[position].path)
            val deleted: Boolean = file.delete();
            if (deleted){
                mContext.contentResolver.delete(contentUri, null, null)
                mFiles.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, mFiles.size)
                Snackbar.make(view, "Archivo eliminado", Snackbar.LENGTH_LONG).show()
            }
        } else {
            Snackbar.make(view, "Error al eliminar el archivo", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int {
        return mFiles.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fileName: TextView = itemView.findViewById(R.id.music_file_name)
        var albumArt: ImageView = itemView.findViewById(R.id.music_img)
        var menuMore: ImageView = itemView.findViewById(R.id.menuMore)
    }


    fun updateList(musicFilesArrayList: ArrayList<MusicFiles>) {
        mFiles.clear()
        mFiles.addAll(musicFilesArrayList)
        notifyDataSetChanged()
        Log.d("MusicAdapter", "Ha llegado al método UpdateList del MusicAdapter")
    }

}



