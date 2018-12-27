package com.bluestone.imageexplorer.recyclerviewadapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bluestone.imageexplorer.R
import com.bluestone.imageexplorer.interfaces.AdapterScrollerInterface
import com.bluestone.imageexplorer.itemdetail.ItemDetail
import com.bluestone.imageexplorer.utilities.AdapterScroller
import com.squareup.picasso.Picasso

class GenericImageAdapter : RecyclerView.Adapter<GenericImageAdapter.ImageHolder>(),
    AdapterScrollerInterface<ItemDetail> {
    private val itemList = ArrayList<ItemDetail>()
    private var scrollHelper: AdapterScrollerInterface<ItemDetail>

    init {
        scrollHelper = AdapterScroller(itemList, this as RecyclerView.Adapter<RecyclerView.ViewHolder>)
    }

    inner class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(itemDetail: ItemDetail) {
            itemView.findViewById<TextView>(R.id.image_name).text = itemDetail.title
            val item = itemView.findViewById<ImageView>(R.id.thumbnail)

            Picasso.get()
                .load(itemDetail.thumbnailUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .resize(600, 200)
                .centerInside()
                .into(itemView.findViewById<ImageView>(R.id.thumbnail))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_detail, parent, false)

        return ImageHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun removeFirstNItems(newItems: List<ItemDetail>) {
        scrollHelper.removeFirstNItems(newItems)
    }

    override fun removeLastNItems(newItems: List<ItemDetail>) {
        scrollHelper.removeLastNItems(newItems)
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
        val maxAdapterSize = 1000000
        val maxPageSize = 5
    }
}