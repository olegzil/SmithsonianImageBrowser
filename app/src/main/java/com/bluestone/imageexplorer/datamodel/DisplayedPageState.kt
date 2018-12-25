package com.bluestone.imageexplorer.datamodel

import com.bluestone.imageexplorer.itemdetail.ItemDetail
import kotlinx.serialization.Serializable

@Serializable
data class DisplayedPageState(val nextPage:Int,
                              val firstVisible:Int,
                              val items:List<ItemDetail>){}
