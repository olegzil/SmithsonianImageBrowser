package com.bluestone.imageexplorer.jsonparsers

import com.bluestone.imageexplorer.datamodel.PixabayPhotoData
import com.bluestone.imageexplorer.utilities.printLog
import org.json.JSONException
import org.json.JSONObject
/*
*
         @SerializedName("previewURL") val previewURL : String,
        @SerializedName("previewWidth") val previewWidth : Int,
        @SerializedName("previewHeight") val previewHeight : Int,

        @SerializedName("largeImageURL") val userImageURL : String,
        @SerializedName("imageWidth") val imageWidth : Int,
        @SerializedName("imageHeight") val imageHeight : Int,

        @SerializedName("user") val user : String,
        @SerializedName("tags") val tags : String
* */
class PixabayPayloadParser(private val jsonString:String) {
    private val pixaBayImageDescriptor:MutableList<PixabayPhotoData>? = mutableListOf()
    fun get():List<PixabayPhotoData>?{
        var retVal:List<PixabayPhotoData>? = null
        try {
            val root = JSONObject(jsonString)
            val objectArray = root.optJSONArray("hits")
            for (index in 0 until objectArray.length()){
                with (objectArray.getJSONObject(index)){
                    pixaBayImageDescriptor?.add(PixabayPhotoData(
                                                optString("previewURL"),
                                                optInt("previewWidth", -1),
                                                optInt("previewHeight", -1),
                                                optString("largeImageURL"),
                                                optInt("imageWidth", -1),
                                                optInt("imageHeight", -1),
                                                optString("user"),
                                                optString("tags")))
                }
            }
            retVal = pixaBayImageDescriptor
        }catch (e: JSONException){
            printLog(e.toString())
        }
        return retVal
    }
}