package com.bluestone.imageexplorer.dataloaders

import com.bluestone.imageexplorer.datamodel.PixabayKey
import com.bluestone.imageexplorer.interfaces.DataLoaderInterface
import com.bluestone.imageexplorer.itemdetail.ItemDetail
import com.bluestone.imageexplorer.jsonparsers.PixabayPayloadParser
import com.bluestone.imageexplorer.server.NetworkServiceInitializer
import com.bluestone.imageexplorer.server.PixabayRetrofitNetworkServices
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class PixabayDataLoader(
    private val key: String
) : DataLoaderInterface {
    override fun put(data: ItemDetail) {}

    private var server: PixabayRetrofitNetworkServices
    private val header = mapOf(
        "X-Api-Key" to "$PixabayKey",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
        "User-Agent" to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.80 Safari/537.36",
        "Accept-Encoding" to "gzip, deflate, br",
        "Content-Type" to "application/json, application/ld+json, application/xml, application/zip, image/jpeg, image/gif"
    )

    init {
        server = PixabayRetrofitNetworkServices(
            NetworkServiceInitializer(
                "https://pixabay.com/api/",
                null
            )
        )
    }

    override fun get(items_per_page: Int, page_number: Int): Single<List<ItemDetail>>? {
        return server.getApi()?.run {
            val result: Single<String>? =
                fetchNext(key, page_number, items_per_page)
                    .subscribeOn(Schedulers.io())
            result?.run {
                flatMap { data ->
                    val itemList = mutableListOf<ItemDetail>()

                    val parser = PixabayPayloadParser(data)
                    parser.get()?.let {item->
                        item.forEach {payloadData->
                            itemList.add(ItemDetail(payloadData.tags, payloadData.largeImageURL, payloadData.previewURL, payloadData))
                        }
                    }
                    Single.just(itemList)
                }
            }
        }
    }

    companion object {
        const val dataKey = "d25cf89d-14e0-45e1-b252-fb8a040f1a75"
    }
}