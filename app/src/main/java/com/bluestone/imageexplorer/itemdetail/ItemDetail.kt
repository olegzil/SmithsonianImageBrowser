package com.bluestone.imageexplorer.itemdetail

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable

@Serializable
data class ItemDetail(
    @SerialId(1) val title: String,
    @SerialId(3) val fullResolutionURL: String,
    @SerialId(4) val previewURL: String,
    @SerialId(5) val extra:Any
)
