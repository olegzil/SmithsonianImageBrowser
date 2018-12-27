package com.bluestone.imageexplorer.utilities

import android.util.Log
import com.bluestone.imageexplorer.datamodel.ImageDirectoryEntry
import com.bluestone.imageexplorer.server.RetrofitNetworkService
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import org.json.JSONException
import org.json.JSONObject

const val TAG = "DoorDashTest"

data class CombinedImageData(var url: String, val payload: ImageDirectoryEntry)
class ImmutableList<T>(private val inner:List<T>) : List<T> by inner
fun <T> List<T>.toImmutableList(): List<T> {
    if (this is ImmutableList<T>) {
        return this
    } else {
        return ImmutableList(this)
    }
}

fun printLog(msg: String) {
    val decorator="=-=-=-=-=-=-="
//    if (Log.isLoggable(TAG, Log.DEBUG))
    Log.d(TAG, "$decorator $msg $decorator")
}

fun printLogWithStack(msg: String) {
    val decorator="=-=-=-=-=-=-="
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
    server: RetrofitNetworkService,
    key: String,
    items_per_page: Int,
    page_number: Int
): Single<List<ImageDirectoryEntry>>? {
    return server.getApi()?.run {
        fetchNextPage(key, items_per_page, page_number)
            .flatMap { jsonString ->
                try {
                    val rootObject = JSONObject(jsonString)
                    val dataArray = rootObject.getJSONArray("data")
                    val imageDescriptor = mutableListOf<ImageDirectoryEntry>()
                    for (index in 0 until dataArray.length()) {
                        val id = dataArray.getJSONObject(index).optString("id")
                        val attributes = dataArray.getJSONObject(index).optJSONObject("attributes")
                        attributes?.let { attributesItems ->
                            val title = attributesItems.optString("title")
                            val creditLine = attributesItems.optString("credit_line")
                            val displayMedium = attributesItems.optString("display_mediums")
                            imageDescriptor.add(ImageDirectoryEntry(id, title, creditLine, displayMedium))
                        }
                    }
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
    server: RetrofitNetworkService,
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
                val listOfURLs = mutableListOf<String>()
                listOfStrings.forEach { jsonString ->
                    JSONObject(jsonString).run {
                        val data = optJSONObject("data")?.run {
                            optJSONObject("attributes")?.run {
                                optJSONObject("uri")?.run {
                                    optString("url")?.let { item ->
                                        listOfURLs.add(optString(item))
                                    }
                                }
                            }
                        }
                    }
                }
                if (!listOfURLs.isEmpty()) {
                    Observable.just(listOfURLs)
                } else {
                    Observable.error<List<String>>(Throwable("invalid json"))
                }
            }
    }
}

fun fetchItemDetails(
    server: RetrofitNetworkService,
    key: String,
    items_per_page: Int,
    page_number: Int
): Observable<Map<String, CombinedImageData>>? {
    val urlToIDMap = mutableMapOf<String, CombinedImageData>()
    return fetchImageDirectoryEntries(server, key, items_per_page, page_number)?.toObservable()?.flatMap {
        val items = arrayListOf<ObservableSource<String>>()
        it.forEach { imageDirectoryEntry ->
            server.getApi()?.run {
                urlToIDMap[imageDirectoryEntry.imageID] = CombinedImageData("", imageDirectoryEntry)
                items.add(fetchDataDebug("${imageDirectoryEntry.imageID}/default_image", key).toObservable())
            }
        }
        Observable.just(items)
    }?.flatMap { arrayOfObservables ->
        Observable.zip(arrayOfObservables) { data->
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
                                        urlToIDMap[data.optString("id")]?.url = url
                                    }
                                }
                            }
                        }
                    }
                }
                if (!urlToIDMap.isEmpty())
                    Observable.just(urlToIDMap)
                else
                    Observable.error<Map<String, CombinedImageData>>(Throwable("invalid json"))
            }
    }
}

