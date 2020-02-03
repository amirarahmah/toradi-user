package com.amirarahmah.toradi_user.ui.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.util.Injection

class EditProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        supportActionBar?.title = "Ubah Profil"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val viewModelFactory = SettingViewModelFact(Injection.provideUserRepository(this))
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingViewModel::class.java)

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
