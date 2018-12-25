package com.bluestone.imageexplorer.cachemanager

import android.content.Context
import com.bluestone.imageexplorer.utilities.printLog
import com.snappydb.DB
import com.snappydb.SnappyDB
import com.snappydb.SnappydbException
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse

object CacheManager {
    fun initialize(context: Context, dbname: String) : CacheManager {
        if (initialized)
            return this
        initialized = true
        database = SnappyDB.Builder(context)
            .directory(context.getExternalFilesDir(dbname).absolutePath)
            .name("viewState")
            .build()
        //TODO:OZ replace with a version check and the appropriate migration code.
        database.put(rootKey, dbVersion)
        return  this
    }
    private var initialized=false
    private lateinit var database: DB
    private val rootKey = "__CacheManager__"
    private val dbVersion = "1.0.0"

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun putString(key:String, value:String) {
        if (!database.isOpen)
            return
        database.put(key, value)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun getString(key:String) : String? {
        if (!database.isOpen)
            return null
        if (!database.exists(key))
            return null
        return database.get(key)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun getItem(key:String) : Any? {
        if (!database.isOpen)
            return null
        if (database.exists(key)){
            return JSON.unquoted.parse(database.get(key))
        }
        return null
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun <T>putItem(key:String, item:T) {
        if (!database.isOpen)
            return
        database.put(key, item)
    }

    fun deleteKey(key: String) = try {
        database.del(key)
    } catch (e: SnappydbException) {
        printLog(e.localizedMessage)
    }

    fun deleteDB() {
        if (!database.isOpen)
            return
        database.destroy()
    }

    fun exists(key: String): Boolean{
        if (!database.isOpen)
            return false
        return database.exists(key)
    }
}