package com.bluestone.imageexplorer.dataloaders

import com.bluestone.imageexplorer.interfaces.DataLoaderInterface
import com.bluestone.imageexplorer.itemdetail.ItemDetail
import com.bluestone.imageexplorer.server.NetworkServiceInitializer
import com.bluestone.imageexplorer.server.RetrofitNetworkService
import com.bluestone.imageexplorer.utilities.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class SmithsonianImagesDataLoader(
    private val key: String
) : DataLoaderInterface {
    private var server: RetrofitNetworkService
    private val header = mapOf(
        "X-Api-Key"                         to "gWGUcVRk85uDmdlt2w9VZvTaR47gmLc1iYKjiiXy",
        "Accept"                            to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
        "User-Agent"                        to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.80 Safari/537.36",
        "Accept-Encoding"                   to "gzip, deflate, br",
        "Content-Type"                      to "application/json, application/ld+json, application/xml, application/zip, image/jpeg, image/gif"
    )

    init {
        server = RetrofitNetworkService(NetworkServiceInitializer("https://api.si.edu/saam/v1/artworks/", header))
    }

    override fun get(items_per_page: Int, page_number: Int): Single<List<ItemDetail>>? {
        return fetchItemDetails(server, key, items_per_page, page_number)?.singleOrError()?.run {
            subscribeOn(Schedulers.io())
            doOnSubscribe {
                printLog("from fetchItemDetails")
            }
            flatMap {combinedData ->
                val result = mutableListOf<ItemDetail>()
                combinedData.forEach { entry ->
                    with(entry.value) {
                        result.add(
                            ItemDetail(
                                payload.imageID,
                                payload.title,
                                payload.credit,
                                payload.medium,
                                entry.value.url
                            )
                        )
                    }
                }
                Single.just(result.toImmutableList())
            }
        }?:let {
            Single.error<List<ItemDetail>>(Throwable("DataLoader returned null"))
        }
    }

    override fun put(data: ItemDetail) {
    }

    fun getUrlList(items_per_page: Int, page_number: Int): Observable<List<String>>? {
        return fetchImageDirectoryEntries(server, key, items_per_page, page_number)?.let { directoryList ->
            fetchImageUrlList(server, directoryList.toObservable(), key)
        }
    }

    companion object {
        const val dataKey = "23daf91f-19d8-499e-9e28-1dfdf0b37a05"
    }
}