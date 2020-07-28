package com.amirarahmah.toradi_user.data.source.remote

import com.amirarahmah.toradi_user.data.model.*
import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("user/login")
    fun doLogin(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("token_firebase") token_firebase: String
    ): Flowable<LoginResponse>

    @FormUrlEncoded
    @POST("user/register")
    fun doRegister(
        @Field("email") email: String,
        @Field("name") fullname: String,
        @Field("phone") phone: String,
        @Field("password") password: String
    ): Observable<RegisterResponse>

    @GET("user/nearby_driver")
    fun getNearbyDriver(): Observable<BaseResponse<List<Driver>>>

    @GET("user/price/{distance}")
    fun getTransportPrice(
        @Path("distance") distance: Double
    ): Observable<BaseResponse<Price>>

    @POST("user/order")
    @FormUrlEncoded
    fun sendOrderOjek(
        @Header("Authorization") token: String,
        @Field("distance") distance: Double,
        @Field("price") price: Int,
        @Field("destination_lat") destination_lat: Double,
        @Field("destination_lng") destination_lng: Double,
        @Field("destination_address") destination_address: String,
        @Field("note") note: String,
        @Field("pickup_lat") pickup_lat: Double,
        @Field("pickup_lng") pickup_lng: Double,
        @Field("pickup_address") pickup_address: String
    ): Observable<OrderResponse>

    @FormUrlEncoded
    @POST("user/order/cancel")
    fun cancelOrder(
        @Header("Authorization") token: String,
        @Field("order_id") order_id: Int
    ): Flowable<Response>

    @GET("user/order/{order_id}")
    fun getDetailOrder(
        @Header("Authorization") token: String,
        @Path("order_id") order_id: Int
    ): Observable<BaseResponse<Order>>

    @GET("user/order")
    fun getListOrder(
        @Header("Authorization") token: String,
        @Query("active") active: Int
    ): Observable<BaseResponse<List<Order>>>

    @POST("user/review")
    @FormUrlEncoded
    fun sendReview(
        @Header("Authorization") token: String,
        @Field("order_id") order_id: Int,
        @Field("rating") rating: Int,
        @Field("review") review: String
    ): Observable<Response>

    @GET("user/order/check_has_reviewed/{order_id}")
    fun checkHasReviewed(
        @Header("Authorization") token: String,
        @Path("order_id") order_id: Int
    ): Observable<BaseResponse<List<Review>>>

    companion object Factory {

        fun create(): ApiService {

            val mRetrofit = Retrofit.Builder()
                .baseUrl("https://ojektoradi.com/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(ApiModule.getClient())
                .build()

            return mRetrofit.create(ApiService::class.java)
        }
    }
}