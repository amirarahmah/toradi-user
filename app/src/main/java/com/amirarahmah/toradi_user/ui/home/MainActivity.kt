package com.amirarahmah.toradi_user.ui.home

import android.app.AlertDialog
import android.content.Intent
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amirarahmah.toradi_user.data.model.User
import com.amirarahmah.toradi_user.ui.activeorder.ActiveOrderActivity
import com.amirarahmah.toradi_user.ui.history.HistoryActivity
import com.amirarahmah.toradi_user.ui.login.FirstActivity
import com.amirarahmah.toradi_user.ui.setting.SettingActivity
import com.amirarahmah.toradi_user.util.Injection
import com.amirarahmah.toradi_user.util.PreferenceHelper
import com.amirarahmah.toradi_user.util.PreferenceHelper.set
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


class MainActivity : AppCompatActivity(), HomeFragment.DonePickLocation {

    private lateinit var viewModel: MainViewModel
    private var donePickLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModelFactory = MainViewModelFact(Injection.provideUserRepository(this))
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        setfullwidth()

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        updateFragment(HomeFragment())

        //set navigation drawer header
        viewModel.getUserData()
        viewModel.user.observe(this, Observer {
            if (it != null) {
                setNavHeader(it)
            }
        })

        setNavigationListener()

        btn_nav.setOnClickListener {
            if (donePickLocation) {
                btn_nav.setImageResource(R.drawable.ic_menu)
                updateFragment(HomeFragment())
                donePickLocation = false
            } else {
                if (!drawer_layout.isDrawerOpen(GravityCompat.START)) {
                    drawer_layout.openDrawer(GravityCompat.START)
                }
            }
        }
    }


    private fun setNavigationListener() {
        nav_view.setNavigationItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.nav_beranda -> {
                    updateFragment(HomeFragment())
                }
                R.id.nav_active -> {
                    val intent = Intent(this, ActiveOrderActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_riwayat -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_setting -> {
                    val intent = Intent(this, SettingActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout ->{
                    val ad = AlertDialog.Builder(this@MainActivity)
                    ad.create()
                    ad.setTitle("Logout")
                    ad.setMessage("Apakah Anda yakin ingin keluar?")
                    ad.setPositiveButton("Ya") { dialog, which ->
                        val prefs = PreferenceHelper.defaultPrefs(this@MainActivity)
                        prefs["loggedIn"] = false

                        val i = Intent(this@MainActivity, FirstActivity::class.java)
                        startActivity(i)
                        finish()
                    }
                    ad.setNegativeButton("Tidak") { dialog, which ->
                        dialog.dismiss()
                    }

                    ad.show()
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


    private fun setNavHeader(user: User) {
        val v = nav_view.getHeaderView(0)
        v.tv_name.text = user.name
        v.tv_email.text = user.email
        if (user.profile_photo != null) {
            Glide.with(this)
                .load(user.profile_photo)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(v.image_profile)
        } else {
            Glide.with(this)
                .load(R.drawable.no_profile_image)
                .into(v.image_profile)
        }
    }


    private fun updateFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit()
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (donePickLocation) {
            btn_nav.setImageResource(R.drawable.ic_menu)
            updateFragment(HomeFragment())
            donePickLocation = false
        } else {
            super.onBackPressed()
        }
    }

    override fun updateData() {
        btn_nav.setImageResource(R.drawable.ic_arrow_back)
        donePickLocation = true
    }
}
