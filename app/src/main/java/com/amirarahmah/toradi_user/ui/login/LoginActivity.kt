package com.amirarahmah.toradi_user.ui.login

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.ui.home.MainActivity
import com.amirarahmah.toradi_user.util.Injection
import com.amirarahmah.toradi_user.util.PreferenceHelper
import com.amirarahmah.toradi_user.util.showSnackbarInfo
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var loading: ProgressDialog

    private var email: String = ""
    private var password: String = ""

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs = PreferenceHelper.defaultPrefs(this@LoginActivity)
        val viewModelFactory = LoginViewModelFact(Injection.provideUserRepository(this), prefs)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel::class.java)

        loading = ProgressDialog(this@LoginActivity)

        btn_login.setOnClickListener {
            validateLogin()
        }

        viewModel.isLoading.observe(this, Observer {
            if(it == true){
                showLoading()
            }else{
                loading.dismiss()
            }
        })

    }


    private fun validateLogin() {
        email = et_email.text.toString().trim()
        password = et_password.text.toString().trim()

        if (email.isBlank() || password.isBlank()) {
            this.showSnackbarInfo("Mohon masukkan email dan password")
        } else {
            doLogin()
        }
    }


    private fun doLogin(){
        viewModel.doLogin(email, password)

        viewModel.errorMessage.observe(this, Observer {
            if (it != null) {
                this.showSnackbarInfo(it)
            }
        })

        viewModel.loggedIn.observe(this, Observer {
            if(it == true){
                navigateToMainActivity()
            }
        })
    }


    private fun navigateToMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(mainIntent)
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
