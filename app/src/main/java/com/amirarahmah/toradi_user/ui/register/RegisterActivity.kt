package com.amirarahmah.toradi_user.ui.register

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.ui.login.LoginActivity
import com.amirarahmah.toradi_user.util.PreferenceHelper
import com.amirarahmah.toradi_user.util.showSnackbarInfo
import kotlinx.android.synthetic.main.activity_register.*
import java.io.File

class RegisterActivity : AppCompatActivity() {

    private lateinit var loading: ProgressDialog

    private var email = ""
    private var fullname = ""
    private var phone = ""
    private var password = ""

    private lateinit var viewModel : RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs = PreferenceHelper.defaultPrefs(this@RegisterActivity)
        val viewModelFactory = RegisterViewModelFact( prefs)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(RegisterViewModel::class.java)

        loading = ProgressDialog(this)

        btn_register.setOnClickListener {
            validateRegister()
        }

        viewModel.isLoading.observe(this, Observer {
            if(it == true){
                showLoading()
            }else{
                loading.dismiss()
            }
        })

    }


    private fun validateRegister() {
        email = et_email.text.toString().trim()
        fullname = et_name.text.toString().trim()
        phone = et_phone.text.toString().trim()
        var confirmed = et_confirm_password.text.toString().trim()
        password = et_password.text.toString().trim()

        if(email.isBlank() || fullname.isBlank() || phone.isBlank() ||
            confirmed.isBlank() || password.isBlank()){
            this.showSnackbarInfo("Mohon masukkan semua data")
        }else{
            if(confirmed != password){
                this.showSnackbarInfo("Password dan ulangi password tidak sesuai")
            }else{
                doRegister()
            }
        }

    }


    private fun doRegister() {
        viewModel.doRegister(email, fullname, phone, password)

        viewModel.isRegistered.observe(this, Observer {
            if(it == true){
                Toast.makeText(this,
                    "Pendaftaran berhasiil, silahkan login menggunakan akun yang telah didaftarakan",
                    Toast.LENGTH_SHORT).show()
                navigateToLoginActivity()
            }
        })

        viewModel.errorMessage.observe(this, Observer {
            if(it != null){
                this.showSnackbarInfo(it)
            }
        })
    }


    private fun navigateToLoginActivity() {
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
        finish()
    }


    private fun showLoading(){
        loading.setMessage("Mohon menunggu..")
        loading.setCancelable(false)
        loading.show()
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
