package com.bluestone.imageexplorer.datamodel

import java.io.Serializable

data class BaseFragmentInitializer(
    val nextPage:Int,
    val fragmentID: String,
    val initialData: String? = null
) : Serializable
