package com.amirarahmah.toradi_user.util

import android.content.Context
import com.amirarahmah.toradi_user.data.source.local.AppDatabase
import com.amirarahmah.toradi_user.data.source.local.UserRepository

object Injection{

    fun provideUserRepository(context : Context) : UserRepository {
        val appDatabase  = AppDatabase.getInstance(context)
        val userDao = appDatabase.userDao()

        return UserRepository(userDao)
    }

}