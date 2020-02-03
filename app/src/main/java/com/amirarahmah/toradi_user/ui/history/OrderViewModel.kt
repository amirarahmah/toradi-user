package com.amirarahmah.toradi_user.ui.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amirarahmah.toradi_user.data.model.Order
import com.amirarahmah.toradi_user.data.model.Resource
import com.amirarahmah.toradi_user.data.source.remote.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class OrderViewModel : ViewModel(){

    private val apiService by lazy {
        ApiService.create()
    }

    val listOrder = MutableLiveData<Resource<List<Order>>>()
    var isLoading = MutableLiveData<Boolean>()

    private val compositeDisposable = CompositeDisposable()

    fun getOrderHistory(token: String){
        isLoading.value = true
        val disposable = apiService.getListOrder("Bearer $token", 0)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                isLoading.value = false
                listOrder.value = Resource.success(it.data)
            }, {
                isLoading.value = false
                listOrder.value = Resource.error(
                    "Gagal menghubungi server! Pastikan Anda terhubung ke Jaringan Internet!",
                    null
                )
            })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}