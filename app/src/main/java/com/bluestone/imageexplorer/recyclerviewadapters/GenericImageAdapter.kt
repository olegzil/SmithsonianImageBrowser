package com.bluestone.imageexplorer.recyclerviewadapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bluestone.imageexplorer.R
import com.bluestone.imageexplorer.datamodel.ScrollingParametersData
import com.bluestone.imageexplorer.interfaces.AdapterScrollerInterface
import com.bluestone.imageexplorer.itemdetail.ItemDetail
import com.bluestone.imageexplorer.utilities.AdapterScroller
import com.bluestone.imageexplorer.utilities.printLog
import com.squareup.picasso.Picasso

class GenericImageAdapter : RecyclerView.Adapter<GenericImageAdapter.ImageHolder>(),
    AdapterScrollerInterface<ItemDetail> {
    private val itemList = ArrayList<ItemDetail>()
    private var scrollHelper: AdapterScrollerInterface<ItemDetail>
    private val imageViewRef= mutableListOf<ImageView>()
    init {
        scrollHelper = AdapterScroller(itemList, this as RecyclerView.Adapter<RecyclerView.ViewHolder>)
    }

    inner class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(itemDetail: ItemDetail, pos:Int) {
            printLog("$pos")
            val targetView = itemView.findViewById<ImageView>(R.id.thumbnail)
            val countDisplay = itemView.findViewById<TextView>(R.id.detailTitle)
            countDisplay.text = pos.toString()
            imageViewRef.add(targetView)
            Picasso.get()
                .load(itemDetail.previewURL)
                .placeholder(R.drawable.ic_launcher_background)
                .resize(600, 200)
                .centerInside()
                .into(targetView)
        }
    }
    fun pixaBayNextPage(currentPage:Int) = currentPage + 1
    fun pixaBayPrevPage(currentPage: Int) = if(currentPage > 1) currentPage - 1 else 1

    fun smithsonianNextPage(currentPage: Int) = itemList.size + currentPage

    fun smithsonianPrevPage(currentPage: Int)= if (currentPage > itemList.size) currentPage - itemList.size else 1

    fun cancelImageFetching(){
        imageViewRef.forEach {
            Picasso.get().cancelRequest(it)
        }
        imageViewRef.clear()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericImageAdapter.ImageHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_detail, parent, false)

        return GenericImageAdapter().ImageHolder(itemView)
    }

    override fun onBindViewHolder(holder: GenericImageAdapter.ImageHolder, position: Int) {
        holder.bind(itemList[position], position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun removeFirstNItems(newItems: List<ItemDetail>, scrollParameters:ScrollingParametersData) {
        scrollHelper.removeFirstNItems(newItems, scrollParameters)
    }

    override fun removeLastNItems(newItems: List<ItemDetail>, scrollParameters: ScrollingParametersData) {
        scrollHelper.removeLastNItems(newItems, scrollParameters)
    }

    override fun update(newItems: List<ItemDetail>) {
        scrollHelper.update(newItems)
    }

    override fun update() {
        notifyDataSetChanged()
    }

    override fun getItemDetailByPosition(index: Int): ItemDetail {
        return itemList[index]
    }

    override fun getAllItems(): List<ItemDetail> {
        return itemList.toMutableList()
    }

    companion object {
        val maxAdapterSize = 10
    }
}