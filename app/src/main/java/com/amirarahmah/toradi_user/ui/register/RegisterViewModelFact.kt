package com.amirarahmah.toradi_user.ui.register

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amirarahmah.toradi_user.ui.login.LoginViewModel

class RegisterViewModelFact(
    private val prefs: SharedPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RegisterViewModel(prefs) as T
    }

}