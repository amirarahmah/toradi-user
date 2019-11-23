package com.amirarahmah.toradi_user.data.source.local

import com.amirarahmah.toradi_user.data.model.User
import io.reactivex.Completable
import io.reactivex.Flowable

class UserRepository(val userDao : UserDao){

    fun getUser(): Flowable<User> {
        return userDao.getUser()
    }

    fun getIdUser(): Flowable<Int> {
        return userDao.getIdUser()
    }

    fun insertUser(user : User) : Completable {
        return Completable.fromAction { userDao.insertUser(user) }
    }

    fun deleteUser() : Completable {
        return Completable.fromAction { userDao.deleteUser() }
    }

    fun updateUser(user : User) : Completable {
        return Completable.fromAction { userDao.updateUser(user) }
    }

}