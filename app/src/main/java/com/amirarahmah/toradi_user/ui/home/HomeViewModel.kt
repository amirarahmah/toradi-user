package com.amirarahmah.toradi_user.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amirarahmah.toradi_user.data.model.GeocodeResult
import com.amirarahmah.toradi_user.data.model.Geometry
import com.amirarahmah.toradi_user.data.model.Prediction
import com.amirarahmah.toradi_user.data.model.Resource
import com.amirarahmah.toradi_user.data.source.remote.GeocodingService
import com.amirarahmah.toradi_user.data.source.remote.PlaceService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class HomeViewModel : ViewModel() {

    private val geocodeService by lazy {
        GeocodingService.create()
    }

    var geocodeResult = MutableLiveData<Resource<Geometry>>()
    private val compositeDisposable = CompositeDisposable()

    fun getLatLng(address: String){
        val apiKey = "AIzaSyCCCMdJd6BGpTEuge2crPT3oV8v5coY8PU"

        val disposable = geocodeService.getLatLng(address, apiKey)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                geocodeResult.value = Resource.success(it.results[0].geometry)
            }, {
                geocodeResult.value = Resource.error(
                    "Tidak ada koneksi internet atau gagal menghubungi server. Silakan coba lagi",
                    null
                )
            })

        compositeDisposable.add(disposable)
    }
}