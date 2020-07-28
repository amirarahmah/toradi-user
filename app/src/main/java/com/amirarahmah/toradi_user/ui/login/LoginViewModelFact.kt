package com.amirarahmah.toradi_user.ui.login

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amirarahmah.toradi_user.data.source.local.UserRepository

class LoginViewModelFact(
    private val userRepository: UserRepository,
    private val prefs : SharedPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LoginViewModel(userRepository, prefs) as T
    }

}