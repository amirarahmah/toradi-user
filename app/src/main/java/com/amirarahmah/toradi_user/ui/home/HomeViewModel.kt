package com.amirarahmah.toradi_user.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amirarahmah.toradi_user.data.model.Price
import com.amirarahmah.toradi_user.data.model.Resource
import com.amirarahmah.toradi_user.data.source.remote.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class HomeViewModel : ViewModel() {

    private val apiService by lazy {
        ApiService.create()
    }

    var price = MutableLiveData<Resource<Price>>()

    private val compositeDisposable = CompositeDisposable()

    fun getTransportPrice(distance: Double){
        val disposable = apiService.getTransportPrice(distance)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                price.value = Resource.success(it.data)
            },{
                price.value = Resource.error(
                    "Gagal menghubungi server! Pastikan Anda terhubung ke Jaringan Internet!",
                    null)
            })
    }

}