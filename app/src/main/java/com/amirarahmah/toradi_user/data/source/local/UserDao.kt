package com.amirarahmah.toradi_user.data.source.local

import androidx.room.*
import com.amirarahmah.toradi_user.data.model.User
import io.reactivex.Flowable

@Dao
interface UserDao {

    @Query("SELECT * FROM user")
    fun getUser(): Flowable<User>

    @Query("SELECT id FROM user")
    fun getIdUser(): Flowable<Int>

    @Query("DELETE FROM user")
    fun deleteUser()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Update
    fun updateUser(user: User)

}