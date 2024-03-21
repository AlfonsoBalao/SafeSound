package com.example.safesound

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initPaginador();
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

        fun getItemCount(): Int {
            return fragments.size
        }

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

}


/*
*  ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager())
* }

* */