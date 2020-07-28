package com.amirarahmah.toradi_user.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amirarahmah.toradi_user.R
import com.amirarahmah.toradi_user.ui.register.RegisterActivity
import kotlinx.android.synthetic.main.activity_first.*

class FirstActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        btn_login.setOnClickListener {
            navigateToLoginActivity()
        }

        btn_register.setOnClickListener {
            navigateToRegisterActivity()
        }

    }

    private fun navigateToLoginActivity() {
        val i = Intent(this@FirstActivity, LoginActivity::class.java)
        startActivity(i)
    }

    private fun navigateToRegisterActivity() {
        val i = Intent(this@FirstActivity, RegisterActivity::class.java)
        startActivity(i)
    }

}
