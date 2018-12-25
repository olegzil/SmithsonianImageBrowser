package com.bluestone.imageexplorer.itemdetail

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable

@Serializable
data class ItemDetail(
    @SerialId(1) val restaurantId: String,
    @SerialId(2) val restaurantName: String,
    @SerialId(3) val foodType: String,
    @SerialId(4) val restaurantThumbnailUrl: String,
    @SerialId(5) val restaurantStatus: String,
    @SerialId(6) val deliveryFee : Int
)
