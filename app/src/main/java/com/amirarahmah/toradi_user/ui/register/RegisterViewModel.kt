package com.amirarahmah.toradi_user.ui.register

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amirarahmah.toradi_user.data.source.remote.ApiService
import com.amirarahmah.toradi_user.util.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class RegisterViewModel(val prefs: SharedPreferences): ViewModel(){

    private val apiService by lazy {
        ApiService.create()
    }

    var isRegistered = SingleLiveEvent<Boolean>()
    var errorMessage = MutableLiveData<String>()

    var isLoading = MutableLiveData<Boolean>()
    private val compositeDisposable = CompositeDisposable()


    fun doRegister(
        email: String, fullname: String, phone: String, password: String
    ) {
        isLoading.value = true
        val disposable = apiService.doRegister(
            email, fullname, phone, password
        ).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                isLoading.value = false
                isRegistered.value = true
            }, {
                isLoading.value = false

                val message = it.message

                if (message != null) {
                    if (message.contains("409")) {
                        errorMessage.value = "Email sudah terdaftarkan"
                    } else {
                        errorMessage.value =
                            "Gagal melakukan login, pastikan Anda terhubung ke jaringan internet"
                    }
                }

            })

        compositeDisposable.add(disposable)
    }


    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}