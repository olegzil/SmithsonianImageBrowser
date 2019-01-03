package com.bluestone.imageexplorer.utilities

import android.support.v7.widget.RecyclerView
import com.bluestone.imageexplorer.datamodel.ScrollingParametersData
import com.bluestone.imageexplorer.interfaces.AdapterScrollerInterface

open class AdapterScroller<T>(
    private val itemList: ArrayList<T>,
    private val recycler: RecyclerView.Adapter<RecyclerView.ViewHolder>
) :
    AdapterScrollerInterface<T> {
    //Scrolling left
    override fun removeFirstNItems(newItems: List<T>, scrollParameters: ScrollingParametersData) {
        val insertionPoint = itemList.size    //new data insertion point
        itemList.addAll(insertionPoint, newItems)    //append new data at the end of the buffer
        recycler.notifyItemRangeInserted(insertionPoint, newItems.size)   //notify insertion occurred
        for (i in scrollParameters.first downTo 0) //remove the front of the buffer up until the first visible item
            itemList.removeAt(0)
        recycler.notifyItemRangeRemoved(0, scrollParameters.first+1)  //notify that the remove occurred
        printLog("after removeFirstNItems first = ${scrollParameters.first} last=${scrollParameters.last} page=${scrollParameters.page} items=${itemList.size}")
    }

    //Scrolling right
    override fun removeLastNItems(newItems: List<T>, scrollParameters: ScrollingParametersData) {
        val removalStart = scrollParameters.last+1
        val originalSize = itemList.size
        var removedCount = 0
        for (i in originalSize-1 downTo removalStart) {
            itemList.removeAt(itemList.size - 1)
            removedCount++
        }
        recycler.notifyItemRangeRemoved(removalStart, removedCount)  //notify that the remove occurred

        itemList.addAll(0, newItems)
        recycler.notifyItemRangeInserted(0, newItems.size)
        printLog("after removeLastNItems first = ${scrollParameters.first} last=${scrollParameters.last} page=${scrollParameters.page} items=${itemList.size}")
    }

    override fun update(newItems: List<T>) {
        itemList.clear()
        itemList.addAll(newItems)
        recycler.notifyDataSetChanged()
    }

    override fun update() {
        recycler.notifyDataSetChanged()
    }

    override fun getItemDetailByPosition(index: Int): T {
        return itemList[index]
    }

    override fun getAllItems(): List<T> {
        return itemList.toMutableList()
    }
}