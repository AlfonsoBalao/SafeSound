package com.example.safesound

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import androidx.appcompat.widget.SearchView

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    //Para que sólo lance una carátula en el fragment Álbumes
    private var albums: ArrayList<MusicFiles> = ArrayList()
    private var duplicate: HashSet<String> = HashSet()

    //Para la barra de búsqueda
    private lateinit var songsFragment: SongsFragment

    //Para el orden en el menú búsqueda
    private val preferences: String ="SortOrder"


    /*Propiedad para los permisos que se define aquí para que se registre en los eventos,
    siempre antes de la función OnCreate()*/

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
    private lateinit var musicFiles: ArrayList<MusicFiles>

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
        val adaptador = ViewPagerAdapter(supportFragmentManager)

        //Para la lista de la barra de búsqueda, instanciamos el fragmento
        songsFragment = SongsFragment()

        // Obtiene todos los archivos de música
        val musicFiles = getAllAudio(this)

        // Obtiene los álbumes únicos
        val uniqueAlbums = getUniqueAlbums(musicFiles)

        // Crea y pasa los álbumes únicos al AlbumFragment
        val albumFragment = AlbumFragment().apply {
            val bundle = Bundle().apply {
                putParcelableArrayList("uniqueAlbums", albums)
                putParcelableArrayList("allMusicFiles", getAllAudio(this@MainActivity))
            }
            arguments = bundle
        }
        adaptador.addFragments(songsFragment, "Canciones")
        adaptador.addFragments(albumFragment, "Álbumes")

        paginador.adapter = adaptador
        tabLayout.setupWithViewPager(paginador)
    }

    private fun getUniqueAlbums(musicFiles: ArrayList<MusicFiles>): ArrayList<MusicFiles> {
        // filtra para no repetir el mismo álbum después en el fragment
        return musicFiles.distinctBy { it.album }.toCollection(ArrayList())
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

    fun getAllAudio(context: Context, sortOrder: String? = null): ArrayList<MusicFiles> {
        val tempAudioList = ArrayList<MusicFiles>()

        val order = when (sortOrder) {
            "sortByName" -> MediaStore.MediaColumns.DISPLAY_NAME + " ASC"
            "sortByDate" -> MediaStore.MediaColumns.DATE_ADDED + " ASC"
            "sortBySize" -> MediaStore.MediaColumns.SIZE + " DESC"
            else -> "${MediaStore.MediaColumns.DISPLAY_NAME} ASC"  // Valor por defecto si sortOrder no coincide
        }
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA, // para la ruta
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media._ID

        )

       val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, order)
        cursor?.use {
            while (it.moveToNext()) {
                val album: String = it.getString(0)
                val title: String = it.getString(1)
                val duration: String = it.getString(2)
                val path: String = it.getString(3)
                val artist: String = it.getString(4)
                val id: String = it.getString(5)

                val musicFile = MusicFiles(path, title, artist, album, duration, id)

                if (!duplicate.contains(album)) {
                    albums.add(musicFile)
                    duplicate.add(album)
                }
                // añadimos todos los archivos a tempAudioList también
                tempAudioList.add(musicFile)
                //tempAudioList.add(MusicFiles(path, title, artist, album, duration, id))
            }
        }
        return tempAudioList
    }
    /* **************************************************************************** */


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search, menu)
        val searchItem = menu?.findItem(R.id.search_option)
        val searchView = searchItem?.actionView as? SearchView
        searchView?.setOnQueryTextListener(this)
        Log.d("MainActivity", "onCreateOptionsMenu: Listener configurado en onCreateOptionsMenu")

        searchView?.requestFocus()

        searchView?.onActionViewExpanded()
        return true
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        Log.d("MAINACTIVITY", "ONQUERYTEXTSUBMIT FUNCIONANDO")
        TODO("Not yet implemented")

    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val userInput = newText?.lowercase() ?: ""
        Log.d("SearchTest", "Texto de búsqueda: $newText")
        val myFiles = ArrayList<MusicFiles>()
        musicFiles.forEach { song ->
            if (song.title.lowercase().contains(userInput)) {
                myFiles.add(song)
            }
        }

        if (this::songsFragment.isInitialized) {
            songsFragment.onMusicListUpdated(myFiles)
        }
        Log.d("MainActivity", "onQueryTextChange llamado con el texto: $newText")

        return true
    }
    interface MusicUpdateListener {
        fun onMusicListUpdated(newList: ArrayList<MusicFiles>)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sortingValue = when (item.itemId) {
            R.id.by_name -> "sortByName"
            R.id.by_date -> "sortByDate"
            R.id.by_size -> "sortBySize"
            else -> return super.onOptionsItemSelected(item)
        }

        val editor: SharedPreferences.Editor = getSharedPreferences(preferences, MODE_PRIVATE).edit()
        editor.putString("sorting", sortingValue)
        editor.apply()
        updateSongListByOrder()

        return true
    }

    fun updateSongListByOrder() {
        val sharedPreferences = getSharedPreferences(preferences, Context.MODE_PRIVATE)
        val sortOrder = sharedPreferences.getString("sorting", "sortByName")
        val updatedMusicFiles = getAllAudio(this, sortOrder)
        songsFragment.onMusicListUpdated(updatedMusicFiles)
    }

}
