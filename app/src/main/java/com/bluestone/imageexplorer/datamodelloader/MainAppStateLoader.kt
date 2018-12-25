package com.bluestone.imageexplorer.datamodelloader

import com.bluestone.imageexplorer.cachemanager.CacheManager
import com.bluestone.imageexplorer.datamodel.MainAppSaveState
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.stringify

class MainAppStateLoader(val cacheManager: CacheManager) {
    private val mainStateKey = "19b7572b-839e-4118-bbc1-4a3e6198d032"
    fun get() : MainAppSaveState?{
        cacheManager.getItem(mainStateKey)?.run {
            return this as MainAppSaveState
        }
        return null
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun put(stateData:MainAppSaveState){
        val state = JSON.unquoted.stringify(stateData.fragmentID)
        cacheManager.putItem(mainStateKey, state)
    }
}