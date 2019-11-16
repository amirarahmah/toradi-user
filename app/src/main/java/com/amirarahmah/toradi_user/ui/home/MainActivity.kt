package com.amirarahmah.toradi_user.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.amirarahmah.toradi_user.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import androidx.drawerlayout.widget.DrawerLayout
import android.util.DisplayMetrics


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        setfullwidth()

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        updateFragment(HomeFragment())
        setNavHeader()

        nav_view.setNavigationItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.nav_beranda -> {
                    updateFragment(HomeFragment())
                }
            }
            drawer_layout.closeDrawer(GravityCompat.START)
            false
        }

    }

    private fun setfullwidth() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val params = nav_view.layoutParams as DrawerLayout.LayoutParams
        val offset = .20f * resources.displayMetrics.widthPixels
        params.width = displayMetrics.widthPixels - offset.toInt()
        nav_view.layoutParams = params
    }

    private fun setNavHeader() {
        val v = nav_view.getHeaderView(0)
        v.tv_name.text = "Amira Fauzia"
        v.tv_email.text = "amirarahmah@gmail.com"
    }


    private fun updateFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
