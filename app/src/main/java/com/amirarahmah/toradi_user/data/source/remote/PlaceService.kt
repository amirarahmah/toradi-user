package com.amirarahmah.toradi_user.data.source.remote

import com.amirarahmah.toradi_user.data.model.PlaceAutocomplete
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceService {

    @GET("json")
    fun getPlaceAutocomplete(@Query("input") input: String,
                             @Query("location") location: String,
                             @Query("radius") radius: Int,
                             @Query("key") key: String)
            : Observable<PlaceAutocomplete>

    companion object Factory {

        fun create(): PlaceService {

            val mRetrofit = Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/place/autocomplete/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(ApiModule.getClient())
                .build()

            return mRetrofit.create(PlaceService::class.java)
        }
    }
}