package com.example.safesound;

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class AlbumAdapter(
        private val mContext: Context,
        private val albumFiles: ArrayList<MusicFiles>
) : RecyclerView.Adapter<AlbumAdapter.MyHolder>() {

@NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false)
        return MyHolder(view)
        }

        override fun onBindViewHolder(@NonNull holder: MyHolder, position: Int) {
            holder.album_name.text = albumFiles[position].album
            val image: ByteArray? = getAlbumArt(albumFiles[position].path)
            if (image != null){
                Glide.with(mContext).asBitmap()  // -> sintaxis propia de la librería Glide
                    .load(image)
                    .into(holder .album_image)
            }
            else{

                Glide.with(mContext)
                    .load(R.drawable.null_cover)
                    .into(holder.album_image)

            }
            holder.itemView.setOnClickListener { v ->
                val intent = Intent(mContext, AlbumDetails::class.java)
                intent.putExtra("albumName", albumFiles[position].album)
                mContext.startActivity(intent)
            }


        }

        override fun getItemCount(): Int {
        return albumFiles.size
        }

        inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var album_image: ImageView = itemView.findViewById(R.id.album_image)
        var album_name: TextView = itemView.findViewById(R.id.album_name)
        }
        }
    private fun getAlbumArt(uri: String): ByteArray? { // -> devolverá un array de bites (imagen)
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(uri)
    val art = retriever.embeddedPicture
    retriever.release() // -> liberamos recursos del objeto MediaDataRetriever
    return art // -> devuelve los datos de la imagen / Si no existe, devuelve null.
}

    /*

    public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyHolder>{
            private Context mContext;
            private ArrayList<MusicFiles> albumFiles;
            View view;

            public AlbumAdapter(Context mContext, ArrayList<MusicFiles> albumFiles){
                        this.context = mContext;
                        this.albumFiles = albumFiles;

            }
            @NonNull
            @Override
            public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
            view = Layout Inflater.from(mContext).inflate(R.layout.album_item, parent, false);
            return new MyHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull MyHolder holder, int position){
            }


            @Override
            public int getItemCount(){
                return albumFiles.size();
                }


            public class MyHolder extends RecyclerView.ViewHolder{

            ImageView album_image;
            TextView album_name;
            public MyHolder(@NonNull View itemView){

            super(itemView);
            album_image = itemView.findViewById(R.id.album_image);
            album_name = itemView.findViewById(R.id.album_name);

                     }
            }


    }

     */