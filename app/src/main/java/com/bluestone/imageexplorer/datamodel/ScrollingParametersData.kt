package com.bluestone.imageexplorer.datamodel

enum class ScrollDirection { SCROLL_DOWN, SCROLL_UP, SCROLL_LEFT, SCROLL_RIGHT, IDLE }
data class ScrollingParametersData(
    val first: Int,
    val last: Int,
    val page: Int,
    val direction: ScrollDirection
)

