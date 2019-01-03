package com.bluestone.imageexplorer.jsonparsers

import com.bluestone.imageexplorer.datamodel.ImageDirectoryEntry
import org.json.JSONObject

class ImageDirectoryParser(private val jsonString: String) {
    private val imageDescriptor = mutableListOf<ImageDirectoryEntry>()
    fun get() = imageDescriptor
    init {
        val rootObject = JSONObject(jsonString)
        val dataArray = rootObject.getJSONArray("data")
        for (index in 0 until dataArray.length()) {
            val idKey = dataArray.getJSONObject(index).optString("id")
            val attributes = dataArray.getJSONObject(index).optJSONObject("attributes")
            attributes?.let { attributesItems ->
                val title = attributesItems.optString("title")
                val creditLine = attributesItems.optString("credit_line")
                val displayMedium = attributesItems.optString("display_mediums")
                val relationships = dataArray.getJSONObject(index).optJSONObject("relationships")
                relationships?.optJSONObject("default_image")
                    ?.optJSONObject("data")
                    ?.optString("id")
                    ?.let { pathToImage ->
                        if (idKey.isNotEmpty())
                            imageDescriptor.add(
                                ImageDirectoryEntry(
                                    idKey,
                                    pathToImage,
                                    title,
                                    creditLine,
                                    displayMedium
                                )
                            )
                    }
            }
        }
    }
}