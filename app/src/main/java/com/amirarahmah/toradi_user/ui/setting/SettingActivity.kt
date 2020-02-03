package com.amirarahmah.toradi_user.ui.setting

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.data.model.User
import com.amirarahmah.toradi_user.ui.login.FirstActivity
import com.amirarahmah.toradi_user.util.Injection
import com.amirarahmah.toradi_user.util.PreferenceHelper
import com.amirarahmah.toradi_user.util.PreferenceHelper.set
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportActionBar?.title = "Pengaturan"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val viewModelFactory = SettingViewModelFact(Injection.provideUserRepository(this))
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingViewModel::class.java)

        viewModel.getUserData()
        viewModel.user.observe(this, Observer {
            if (it != null) {
                setProfile(it!!)
            }
        })

        tv_edit.setOnClickListener {

        }

        btn_logout.setOnClickListener {
            val ad = AlertDialog.Builder(this)
            ad.create()
            ad.setTitle("Logout")
            ad.setMessage("Apakah Anda yakin ingin keluar?")
            ad.setPositiveButton("Ya") { dialog, which ->
                val prefs = PreferenceHelper.defaultPrefs(this)
                prefs["loggedIn"] = false

                val i = Intent(this, FirstActivity::class.java)
                startActivity(i)
                finish()
            }
            ad.setNegativeButton("Tidak") { dialog, which ->
                dialog.dismiss()
            }

            ad.show()
        }

    }


    private fun setProfile(user: User) {
        tv_name.text = user.name
        tv_email.text = user.email
        if (user.profile_photo != null) {
            Glide.with(this)
                .load(user.profile_photo)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(image_profile)
        } else {
            Glide.with(this)
                .load(R.drawable.no_profile_image)
                .into(image_profile)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return false
    }
}
