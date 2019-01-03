package com.bluestone.imageexplorer.datamodel

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialId

data class PixabayPhotoData (
        @SerializedName("previewURL") val previewURL : String,
        @SerializedName("previewWidth") val previewWidth : Int,
        @SerializedName("previewHeight") val previewHeight : Int,

        @SerializedName("largeImageURL") val largeImageURL : String,
        @SerializedName("imageWidth") val imageWidth : Int,
        @SerializedName("imageHeight") val imageHeight : Int,

        @SerializedName("user") val user : String,
        @SerializedName("tags") val tags : String
    )
    data class PixabayPhotoDataModel(
        @SerialId(1) val totalHits:Int,
        @SerialId(2) val hits:List<PixabayPhotoData>
    )
