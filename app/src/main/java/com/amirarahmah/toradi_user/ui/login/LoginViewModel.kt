package com.amirarahmah.toradi_user.ui.login

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amirarahmah.toradi_user.data.model.User
import com.amirarahmah.toradi_user.data.source.local.UserRepository
import com.amirarahmah.toradi_user.data.source.remote.ApiService
import com.amirarahmah.toradi_user.util.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import com.amirarahmah.toradi_user.util.PreferenceHelper.set
import com.google.firebase.iid.FirebaseInstanceId

class LoginViewModel(
    val userRepository: UserRepository,
    val prefs: SharedPreferences) : ViewModel() {

    private val apiService by lazy {
        ApiService.create()
    }

    var loggedIn = SingleLiveEvent<Boolean>()
    var errorMessage = MutableLiveData<String>()

    var isLoading = MutableLiveData<Boolean>()

    private val compositeDisposable = CompositeDisposable()

    fun doLogin(email: String, password: String) {
        isLoading.value = true

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                val token = task.result?.token
                Log.d("LoginActivity", "Firebase Token: $token")

                val disposable = apiService.doLogin(email, password, ""+token)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        prefs["token"] = it.token
                        saveUser(it.data)
                    }, {
                        isLoading.value = false

                        val message = it.message

                        if (message != null) {
                            if (message.contains("401")) {
                                errorMessage.value =
                                    "Email atau Pasword salah"
                            } else {
                                errorMessage.value =
                                    "Gagal melakukan login, pastikan Anda terhubung ke jaringan internet"
                            }
                        }

                    })
                compositeDisposable.add(disposable)
            }
    }


    fun saveUser(user: User) {
        val disposable =
            userRepository.deleteUser()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        insertUserToDb(user)
                    },
                    {
                        isLoading.value = false
                    })
        compositeDisposable.add(disposable)
    }


    fun insertUserToDb(user: User) {
        val disposable =
            userRepository.insertUser(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        prefs["loggedIn"] = true

                        isLoading.value = false
                        loggedIn.value = true
                    },
                    {
                        isLoading.value = false
                    })

    }


    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}