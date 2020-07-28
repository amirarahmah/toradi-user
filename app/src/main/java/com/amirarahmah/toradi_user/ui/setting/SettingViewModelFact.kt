package com.amirarahmah.toradi_user.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amirarahmah.toradi_user.data.source.local.UserRepository

class SettingViewModelFact(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingViewModel(userRepository) as T
    }

}