package com.amirarahmah.toradi_user.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amirarahmah.toradi_user.data.model.Prediction
import com.amirarahmah.toradi_user.data.model.Resource
import com.amirarahmah.toradi_user.data.source.remote.PlaceService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail_order.*

class LocationViewModel : ViewModel() {

    private val placeService by lazy {
        PlaceService.create()
    }

    var placeAutocomplete = MutableLiveData<Resource<List<Prediction>>>()
    private val compositeDisposable = CompositeDisposable()

    fun getPlaceSuggestion(input: String, latitude: Double, longitude: Double){
        val location = "$latitude,$longitude"
        val apiKey = "AIzaSyCCCMdJd6BGpTEuge2crPT3oV8v5coY8PU"

        val disposable = placeService.getPlaceSuggestion(input,location, 5000, apiKey)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                placeAutocomplete.value = Resource.success(it.predictions)
            }, {
                placeAutocomplete.value = Resource.error(
                    "Tidak ada koneksi internet atau gagal menghubungi server. Silakan coba lagi",
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