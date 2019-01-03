package com.bluestone.imageexplorer.interfaces

import com.bluestone.imageexplorer.datamodel.ScrollingParametersData

interface AdapterScrollerInterface<T> {
    fun removeFirstNItems(newItems: List<T>, scrollParameters:ScrollingParametersData)
    fun removeLastNItems(newItems: List<T>, scrollParameters: ScrollingParametersData)
    fun update(newItems: List<T>)
    fun update()
    fun getItemDetailByPosition(index: Int): T
    fun getAllItems(): List<T>
}