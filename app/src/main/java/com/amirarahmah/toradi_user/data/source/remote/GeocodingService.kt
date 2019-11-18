package com.amirarahmah.toradi_user.data.source.remote

import com.amirarahmah.toradi_user.data.model.Geocoding
import com.amirarahmah.toradi_user.data.model.PlaceAutocomplete
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {

    @GET("json")
    fun getLatLng(
        @Query("address") input: String,
        @Query("key") key: String
    ): Observable<Geocoding>

    companion object Factory {

        fun create(): GeocodingService {

            val mRetrofit = Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/geocode/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(ApiModule.getClient())
                .build()

            return mRetrofit.create(GeocodingService::class.java)
        }
    }
}