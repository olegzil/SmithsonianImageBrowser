package com.bluestone.imageexplorer.server

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface NetworkApiInterface {
    @Headers(
        "X-Api-Key: gWGUcVRk85uDmdlt2w9VZvTaR47gmLc1iYKjiiXy",
        "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
        "User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.80 Safari/537.36",
        "Accept-Encoding: gzip, deflate, br",
        "Content-Type: application/json, application/ld+json, application/xml, application/zip, image/jpeg, image/gif"
    )

    @GET("?")
    fun fetchImageList(@Query("lat") latitude:Float, @Query("lng") longitude:Float, @Query("limit") limit: Int): Single<String>

    @GET("?")
    fun fetchRLNextPage(@Query("lat") latitude:Float,
                        @Query("lng") longitude:Float,
                        @Query("limit") limit: Int,
                        @Query("page") page:Int): Single<String>
    @GET(" ")
    fun fetchData(@Query("api_key") endPoint:String ): Single<String>

    @GET("{path}")
    fun fetchDataDebug(@Path("path") path:String, @Query("api_key") endPoint:String ): Single<String>

    @GET(" ")
    fun fetchNextPage(@Query("api_key") key:String, @Query("page[limit]") pageLimit:Int, @Query("page[offset]") nextPage:Int): Single<String>

    @GET("")
    fun search(@Query("search") searchText:String ): Single<String>
}