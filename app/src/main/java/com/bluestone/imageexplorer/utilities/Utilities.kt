package com.bluestone.imageexplorer.utilities

import android.util.Log
import com.bluestone.imageexplorer.BuildConfig
import com.bluestone.imageexplorer.datamodel.ImageDirectoryEntry
import com.bluestone.imageexplorer.jsonparsers.ImageDirectoryParser
import com.bluestone.imageexplorer.jsonparsers.ImageUrlParser
import com.bluestone.imageexplorer.server.SmithsonianRetrofitNetworkService
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject

const val TAG = "DoorDashTest"

data class CombinedImageData(var url: String, val payload: ImageDirectoryEntry)
class ImmutableList<T>(private val inner: List<T>) : List<T> by inner

fun <T> List<T>.toImmutableList(): List<T> {
    return if (this is ImmutableList<T>) {
        this
    } else {
        ImmutableList(this)
    }
}

fun printOrCrash(msg: String, e: Throwable? = null) {
    e?.run {
        when (BuildConfig.BUILD_TYPE) {
            "debug" -> throw(this)
            "release" -> printLog(msg)
            else -> {
                printLog("unrecognizable ${BuildConfig.BUILD_TYPE}")
            }
        }
    } ?: run {
        printLog(msg)
    }
}

fun printLog(msg: String) {
    val decorator = "=-=-=-=-=-=-="
    if (BuildConfig.DEBUG)
        Log.d(TAG, "$decorator $msg $decorator")
}

fun printLogWithStack(msg: String) {
    val decorator = "=-=-=-=-=-=-="
//    if (Log.isLoggable(TAG, Log.DEBUG))
    Log.d(TAG, "$decorator begin stack trace $decorator")
    Log.d(TAG, "$decorator $msg $decorator")
    Log.d(TAG, " ${buildStackTraceString(Thread.currentThread().stackTrace)}")
    Log.d(TAG, "$decorator end stack trace $decorator")
}

fun buildStackTraceString(elements: Array<StackTraceElement>?): String {
    val sb = StringBuilder()
    if (elements != null && elements.isNotEmpty()) {
        for (element in elements) {
            sb.append(element.toString())
            sb.append("\n")
        }
    }
    return sb.toString()
}

fun fetchImageDirectoryEntries(
    server: SmithsonianRetrofitNetworkService,
    key: String,
    items_per_page: Int,
    page_number: Int
): Single<List<ImageDirectoryEntry>>? {
    return server.getApi()?.run {
        fetchNextPage(key, items_per_page, page_number)
            .subscribeOn(Schedulers.io())
            .flatMap { jsonString ->
                try {
                    val imageDescriptor = ImageDirectoryParser(jsonString).get()
                    if (imageDescriptor.size > 0)
                        Single.just(imageDescriptor)
                    else
                        Single.error(Throwable("Invalid JSON"))
                } catch (e: JSONException) {
                    Single.error<List<ImageDirectoryEntry>>(Throwable(e.cause.toString()))
                }
            }
    }
}

fun fetchImageUrlList(
    server: SmithsonianRetrofitNetworkService,
    source: Observable<List<ImageDirectoryEntry>>,
    key: String
): Observable<List<String>>? {
    return source.flatMap {
        val items = arrayListOf<ObservableSource<String>>()
        it.forEach { imageDirectoryEntry ->
            server.getApi()?.run {
                items.add(fetchData("${imageDirectoryEntry.imageID}/default_image?api_key=$key").toObservable())
            }
        }
        Observable.just(items)
    }.flatMap { arrayOfObservables ->
        Observable.zip(arrayOfObservables) { mutableListOf<String>() }
            .flatMap { listOfStrings ->
                val listOfURLs = ImageUrlParser(listOfStrings).get()
                if (!listOfURLs.isEmpty()) {
                    Observable.just(listOfURLs)
                } else {
                    Observable.error<List<String>>(Throwable("invalid json"))
                }
            }
    }
}

fun fetchItemDetails(
    server: SmithsonianRetrofitNetworkService,
    key: String,
    items_per_page: Int,
    page_number: Int
): Observable<Map<String, CombinedImageData>>? {
    val urlToImageDataMap = mutableMapOf<String, CombinedImageData>()
    return fetchImageDirectoryEntries(server, key, items_per_page, page_number)?.let { ideSingle ->
        ideSingle.subscribeOn(Schedulers.io())
        ideSingle.toObservable()?.let { ideObservable ->
            ideObservable.flatMap {
                val items = arrayListOf<ObservableSource<String>>()
                it.forEach { imageDirectoryEntry ->
                    server.getApi()?.run {
                        urlToImageDataMap[imageDirectoryEntry.imageID] = CombinedImageData("", imageDirectoryEntry)
                        items.add(
                            fetchDataWithPath(
                                "${imageDirectoryEntry.pathToImage}/default_image",
                                key
                            ).toObservable()
                        )
                    }
                }
                Observable.just(items)
            }.flatMap { arrayOfObservables ->
                Observable.zip(arrayOfObservables) { data ->
                    val transfer = mutableListOf<String>()
                    data.forEach {
                        transfer.add(it as String)
                    }
                    transfer
                }
                    .flatMap { listOfStrings ->
                        listOfStrings.forEach { jsonString ->
                            JSONObject(jsonString).run {
                                optJSONObject("data")?.let { data ->
                                    data.optJSONObject("attributes")?.run {
                                        optJSONObject("uri")?.run {
                                            optString("url")?.let { url ->
                                                val itemUUID = data.optString("id")
                                                val value = urlToImageDataMap[itemUUID]
                                                value?.run {
                                                    val temp = value.copy()
                                                    temp.url = url
                                                    urlToImageDataMap.put(itemUUID, temp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!urlToImageDataMap.isEmpty()) {
                            Observable.just(urlToImageDataMap)
                        } else
                            Observable.error<Map<String, CombinedImageData>>(Throwable("invalid json"))
                    }
            }
        }
    }
}