package com.bluestone.imageexplorer.server

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkApiInterface {
    @GET("?")
    fun fetchRestaurantList(@Query("lat") latitude:Float, @Query("lng") longitude:Float,  @Query("limit") limit: Int): Single<String>

    @GET("?")
    fun fetchRLNextPage(@Query("lat") latitude:Float,
                        @Query("lng") longitude:Float,
                        @Query("limit") limit: Int,
                        @Query("page") page:Int): Single<String>
    @GET(" ")
    fun fetchData(@Query("dummy") endPoint:String ): Single<String>

    @GET("?")
    fun search(@Query("search") searchText:String ): Single<String>
}