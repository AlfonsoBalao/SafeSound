package com.example.safesound

import android.Manifest
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {

    /*Propiedad para los permisos, se define aquí para que se registre en los eventos, siempre antes
    de la función OnCreate()*/

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val grantedPermissions = permissions.filterValues { it }
        val deniedPermissions = permissions.filterValues { !it }

        if (grantedPermissions.isNotEmpty()) {
            // Al menos un permiso concedido
            val mensaje = "Permisos concedidos: ${grantedPermissions.keys.joinToString()}"
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            musicFiles = getAllAudio(this)
            initPaginador()
        }

        if (deniedPermissions.isNotEmpty()) {
            // Al menos un permiso denegado
            val mensaje = "Permisos rechazados: ${deniedPermissions.keys.joinToString()}"
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()

        }
    }
    /* *********************************************************************** */


    //Almacenaremos una lista de objetos MusicFiles
    private lateinit var musicFiles : ArrayList<MusicFiles>

    //Función para obtener la lista musicFiles desde otro punto de la app
    fun getMusicFiles(): ArrayList<MusicFiles> {
        if (::musicFiles.isInitialized) {
            return musicFiles
        } else {
            return arrayListOf()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Solicitar permisos de la aplicación

        val permisos = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        requestMultiplePermissionsLauncher.launch(permisos)
        //Fin de la solicitud de permisos de la aplicación


    }


    //Función que inicia la paginación y recoge los fragments de canciones y álbumes
    private fun initPaginador() {
        val paginador: ViewPager = findViewById(R.id.paginador)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)

        //Para proporcionar fragmentos dinámicamente a un ViewPager
        val adaptador = ViewPagerAdapter(supportFragmentManager)
        adaptador.addFragments(SongsFragment(), "Canciones")
        adaptador.addFragments(AlbumFragment(), "Álbumes")
        paginador.adapter = adaptador
        tabLayout.setupWithViewPager(paginador)
    }


    class ViewPagerAdapter(fragmentManager: FragmentManager) : PagerAdapter() {
        private val fragmentManager: FragmentManager = fragmentManager

        private val fragments = mutableListOf<Fragment>()
        private val titulos = mutableListOf<String>()

        fun createFragment(position: Int): Fragment {
            return fragments[position]
        }

        //Cuenta los fragments que hay en la lista
        override fun getCount(): Int {
            return fragments.size
        }

        //Determina si un objeto es una vista asociada a un fragment en el paginador
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == (`object` as Fragment).view
        }

        //Introduce los fragments
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = fragments[position]
            fragmentManager.beginTransaction().add(container.id, fragment).commit()
            return fragment
        }

        override fun getPageTitle(position: Int): CharSequence? {
            // Devolver el título de la página en la posición dada
            return titulos[position]
        }


        //Para agregar los títulos
        fun addFragments(fragment: Fragment, titulo: String) {
            fragments.add(fragment)
            titulos.add(titulo)
        }


    }


    /*  Función encargada de buscar los archivos de audio en el dispositivo
     y devolver una lista de objetos MusicFiles que representan tales archivos */

    fun getAllAudio(context: Context): ArrayList<MusicFiles> {
        val tempAudioList = ArrayList<MusicFiles>()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA, // para la ruta
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media._ID

        )

        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val album: String = it.getString(0)
                val title: String = it.getString(1)
                val duration: String = it.getString(2)
                val path: String = it.getString(3)
                val artist: String = it.getString(4)
                val id: String = it.getString(5)

                var musicFiles: ArrayList<MusicFiles> = arrayListOf()


                // tomar Log.e para verificar
                Log.e("Path: $path", "Album : $album")
                tempAudioList.add(MusicFiles(path, title, artist, album, duration, id))
            }
        }
        return tempAudioList
    }
    /* **************************************************************************** */


    fun openAlbumDetails(musicFiles: ArrayList<MusicFiles>) {
        val intent = Intent(this, AlbumDetails::class.java).apply {
            putExtra("albumDetails", musicFiles)
        }
        startActivity(intent)
    }

}
