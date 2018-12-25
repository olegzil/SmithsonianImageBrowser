package com.bluestone.imageexplorer.datamodelloader

import com.bluestone.imageexplorer.cachemanager.CacheManager
import com.bluestone.imageexplorer.datamodel.DisplayedPageState
import kotlinx.serialization.*
import kotlinx.serialization.json.JSON

class DisplayedPageStateLoader(val cacheManager: CacheManager) {
    private val pageStateKey = "9256ac13-6850-43eb-b011-2169087e5d28"
    @UseExperimental(ImplicitReflectionSerializer::class)
    fun put(data: DisplayedPageState) {
        val item = JSON.unquoted.stringify(data)
        cacheManager.putString(pageStateKey, item)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun get(): DisplayedPageState? {
        cacheManager.getString(pageStateKey)?.run {
            return JSON.unquoted.parse(this)
        }
        return null
    }
}