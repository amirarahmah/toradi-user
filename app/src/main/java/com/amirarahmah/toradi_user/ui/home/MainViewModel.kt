package com.amirarahmah.toradi_user.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amirarahmah.toradi_user.data.model.User
import com.amirarahmah.toradi_user.data.source.local.UserRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {

    var user = MutableLiveData<User>()
    private val compositeDisposable = CompositeDisposable()

    fun getUserData() {
        val disposable = userRepository.getUser()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ user ->
                this.user.postValue(user)
            })
            { throwable ->

            }
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}