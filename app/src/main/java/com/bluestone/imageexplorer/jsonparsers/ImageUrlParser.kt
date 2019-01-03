package com.bluestone.imageexplorer.jsonparsers

import org.json.JSONObject

class ImageUrlParser(val listOfStrings: List<String>) {
    private val listOfURLs = mutableListOf<String>()
    fun get() = listOfStrings
    init {
        listOfStrings.forEach { jsonString ->
            JSONObject(jsonString).run {
                optJSONObject("data")?.run {
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
    }
}