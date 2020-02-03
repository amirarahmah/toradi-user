package com.amirarahmah.toradi_user.ui.detailorder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amirarahmah.toradi_user.data.model.Order
import com.amirarahmah.toradi_user.data.model.Resource
import com.amirarahmah.toradi_user.data.model.Review
import com.amirarahmah.toradi_user.data.source.remote.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class DetailOrderViewModel : ViewModel(){

    private val apiService by lazy {
        ApiService.create()
    }

    val statusUpdated = MutableLiveData<Resource<String>>()
    val review = MutableLiveData<Resource<Review>>()
    val order = MutableLiveData<Resource<Order>>()
    var isLoading = MutableLiveData<Boolean>()

    private var dataReview: Review? = null

    private val compositeDisposable = CompositeDisposable()

    fun getDetailOrder(token: String, orderId : Int){
        isLoading.value = true
        val disposable = apiService.checkHasReviewed("Bearer $token", orderId)
            .flatMap {
                dataReview = it.data?.get(0)
                apiService.getDetailOrder("Bearer $token", orderId)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                isLoading.value = false

                review.value = Resource.success(dataReview)
                val order = it.data

                when (order.status) {
                    1 -> {
                        order.status_text = "Sedang Mencari Pengemudi"
                    }
                    2 ->
                        order.status_text = "Pengemudi sedang menuju Anda"
                    3 ->
                        order.status_text = "Anda telah dijemput oleh Pengemudi"
                    4 ->
                        order.status_text = "Anda telah sampai pada tujuan"
                    5 ->
                        order.status_text = "Pesanan dibatalkan"
                    6 ->
                        order.status_text = "Pesanan dibatalkan oleh Driver"
                }

                this.order.value = Resource.success(it.data)
            }, {
                isLoading.value = false
                order.value = Resource.error(
                    "Gagal menghubungi server! Pastikan Anda terhubung ke Jaringan Internet!",
                    null
                )
            })
        compositeDisposable.add(disposable)
    }


    fun cancelOrder(token: String, orderId: Int) {
        isLoading.value = true
        val disposable = apiService.cancelOrder("Bearer $token", orderId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                isLoading.value = false
                statusUpdated.value = Resource.success(it.message)
            }, {
                isLoading.value = false
                statusUpdated.value = Resource.error("Terjadi Kesalahan Jaringan", null)
            })
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}