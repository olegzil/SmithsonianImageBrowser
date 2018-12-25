package com.bluestone.imageexplorer.utilities

import android.support.v7.widget.RecyclerView
import com.bluestone.imageexplorer.interfaces.AdapterScrollerInterface

open class AdapterScroller<T> (private val itemList:ArrayList<T>, val recycler: RecyclerView.Adapter<RecyclerView.ViewHolder>) :
    AdapterScrollerInterface<T> {
    override fun removeFirstNItems(newItems: List<T>) {
        for (  i in 0 until newItems.size)
            itemList.removeAt(i)
        itemList.addAll(itemList.size, newItems)
        recycler.notifyItemRangeRemoved(0, newItems.size)
    }

    override fun removeLastNItems(newItems:List<T>) {
        val start = itemList.size - newItems.size
        if (start < 0)
            return
        for (i in start until newItems.size)
            itemList.removeAt(i)
        itemList.addAll(0, newItems)
        recycler.notifyItemRangeInserted(0, newItems.size)
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