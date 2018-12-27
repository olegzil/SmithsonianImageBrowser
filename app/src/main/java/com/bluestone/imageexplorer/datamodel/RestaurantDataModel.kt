package com.bluestone.imageexplorer.datamodel

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialId

class RestaurantData(
    @SerializedName("restaurantId") val restaurantId : String,
    @SerializedName("restaurantName") val restaurantName : String,
    @SerializedName("foodType") val foodType : String,
    @SerializedName("thumbnailUrl") val restaurantThumbnailUrl : String,
    @SerializedName("restaurantStatus") val restaurantStatus : String,
    @SerializedName("deliveryFee") val deliveryFee : Int
)

class RestaurantDataModel(
    @SerialId(1) val items:List<RestaurantData>
)
