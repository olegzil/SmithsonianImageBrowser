package com.bluestone.imageexplorer.server

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SmithsonianServerAPIInterface {
    @GET(" ")
    fun fetchData(@Query("api_key") endPoint:String ): Single<String>

    @GET("{path}")
    fun fetchDataWithPath(@Path("path") path:String, @Query("api_key") endPoint:String ): Single<String>

    @GET(" ")
    fun fetchNextPage(@Query("api_key") key:String, @Query("page[limit]") pageLimit:Int, @Query("page[offset]") nextPage:Int): Single<String>
}