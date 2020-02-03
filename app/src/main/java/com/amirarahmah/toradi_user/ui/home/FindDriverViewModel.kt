package com.amirarahmah.toradi_user.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amirarahmah.toradi_user.data.model.BaseResponse
import com.amirarahmah.toradi_user.data.model.Order
import com.amirarahmah.toradi_user.data.model.OrderResponse
import com.amirarahmah.toradi_user.data.model.Resource
import com.amirarahmah.toradi_user.data.source.remote.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FindDriverViewModel : ViewModel() {

    private val apiService by lazy {
        ApiService.create()
    }

    var order = MutableLiveData<Resource<OrderResponse>>()
    var orderCanceled = MutableLiveData<Resource<String>>()

    var isLoading = MutableLiveData<Boolean>()

    private val compositeDisposable = CompositeDisposable()


    fun sendOrderOjek(
        token: String?,
        pickupLat: Double?,
        pickupLng: Double?,
        pickupAddress: String?,
        destinationLat: Double?,
        destinationLng: Double?,
        destinationAddress: String?,
        price: Int,
        distance: Double,
        note: String?
    ) {
        val disposable = apiService.sendOrderOjek("Bearer $token", distance, price, destinationLat!!,
            destinationLng!!, destinationAddress!!, note!!, pickupLat!!, pickupLng!!, pickupAddress!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                order.value = Resource.success(it)
            },{
                order.value = Resource.error(
                    "Gagal menghubungi server! Pastikan Anda terhubung ke Jaringan Internet!",
                    null)
            })
    }


    fun cancelOrder(token: String, orderId: Int) {
        isLoading.value = true
        val disposable = apiService.cancelOrder("Bearer $token", orderId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                isLoading.value = false
                orderCanceled.value = Resource.success(it.message)
            }, {
                isLoading.value = false
                orderCanceled.value = Resource.error("Gagal membatalkan order", null)
            })
        compositeDisposable.add(disposable)
    }

}