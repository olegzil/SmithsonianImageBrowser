package com.bluestone.imageexplorer.datamodel

import kotlinx.serialization.Serializable

@Serializable
class FragmentScrollData(val nextPage:Int, val firstItemOnScreen:Int, val lastItemOnScreen:Int)