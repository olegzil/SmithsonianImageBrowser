package com.bluestone.imageexplorer.interfaces

interface AdapterScrollerInterface<T> {
    fun removeFirstNItems(newItems: List<T>)
    fun removeLastNItems(newItems: List<T>)
    fun update(newItems: List<T>)
    fun update()
    fun getItemDetailByPosition(index: Int): T
    fun getAllItems(): List<T>
}