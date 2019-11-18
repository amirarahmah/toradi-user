package com.amirarahmah.toradi_user.data.source.remote

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

interface ApiService {



    companion object Factory {

        fun create(): PlaceService {

            val mRetrofit = Retrofit.Builder()
                .baseUrl("")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(ApiModule.getClient())
                .build()

            return mRetrofit.create(PlaceService::class.java)
        }
    }
}