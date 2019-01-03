package com.bluestone.imageexplorer.server

import com.bluestone.imageexplorer.datamodel.PixabayPhotoDataModel
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayServerAPIInterface {
    @GET("?")
    fun fetchAll(@Query("key") key: String, @Query("per_page") per_page: Int): Single<PixabayPhotoDataModel>

    @GET("?")
    fun fetchNext(@Query("key") key: String, @Query("page") page: Int, @Query("per_page") per_page: Int): Single<String>
}