package com.bluestone.imageexplorer.interfaces

import com.bluestone.imageexplorer.itemdetail.ItemDetail
import io.reactivex.Single

interface DataLoaderInterface {
    fun get(items_per_page:Int, page_number:Int) : Single<List<ItemDetail>>?
    fun put(data:ItemDetail)//TODO:Implement write to cache
}