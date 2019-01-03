package com.bluestone.imageexplorer.datamodel

import kotlinx.serialization.Serializable

@Serializable
class FragmentSaveState(val nextPage:Int, val urlToExpendedImage:String)