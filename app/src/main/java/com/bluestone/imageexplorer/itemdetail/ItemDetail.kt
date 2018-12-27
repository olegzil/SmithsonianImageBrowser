package com.bluestone.imageexplorer.itemdetail

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable

@Serializable
data class ItemDetail(
    @SerialId(0) val imageId:String,
    @SerialId(1) val title: String,
    @SerialId(2) val credit: String,
    @SerialId(3) val medium: String,
    @SerialId(4) val thumbnailUrl: String
)
