package com.bluestone.imageexplorer.utilities

import android.support.v7.widget.RecyclerView
import com.bluestone.imageexplorer.interfaces.AdapterScrollerInterface

open class AdapterScroller<T> (private val itemList:ArrayList<T>, val recycler: RecyclerView.Adapter<RecyclerView.ViewHolder>) :
    AdapterScrollerInterface<T> {
    override fun removeFirstNItems(newItems: List<T>) {
        for (  i in 0 until itemList.size)
            itemList.removeAt(0)
        itemList.addAll(itemList.size, newItems)
        recycler.notifyItemRangeRemoved(0, newItems.size)
    }

    override fun removeLastNItems(newItems:List<T>) {
        val start = itemList.size - newItems.size
        if (start < 0)
            return
        for (i in start until itemList.size)
            itemList.removeAt(start)
        itemList.addAll(start, newItems)
        recycler.notifyItemRangeInserted(start, newItems.size)
    }

    override fun update(newItems: List<T>) {
        itemList.clear()
        itemList.addAll(newItems)
        recycler.notifyDataSetChanged()
    }
    override fun update(){
        recycler.notifyDataSetChanged()
    }
    override fun getItemDetailByPosition(index: Int): T {
        return itemList[index]
    }

    override fun getAllItems(): List<T> {
        return itemList.toMutableList()
    }
}