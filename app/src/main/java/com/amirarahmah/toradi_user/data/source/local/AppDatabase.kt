package com.amirarahmah.toradi_user.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.amirarahmah.toradi_user.data.model.User

@Database(entities = arrayOf(User::class),
    version = 5)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao() : UserDao

    companion object {
        private const val DATABASE_NAME = "toradidb"
        private var mInstance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (mInstance == null) {
                mInstance = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return mInstance!!
        }

    }
}