package com.bluestone.imageexplorer.utilities

import android.util.Log
import com.bluestone.imageexplorer.itemdetail.ItemDetail
import org.json.JSONArray
import org.json.JSONException

const val TAG = "DoorDashTest"
fun printLog(msg: String) {
//    if (Log.isLoggable(TAG, Log.DEBUG))
        Log.d(TAG, "=-=-=-=-=-=-= $msg")
}

fun populateRestaurantData(jsonString: String): List<ItemDetail>? {
    var retVal: List<ItemDetail>? = null
    try {
        val objectArray = JSONArray(jsonString)
        val workArea = mutableListOf<ItemDetail>()
        for (index in 0 until objectArray.length()) {
            var status = objectArray.getJSONObject(index).getString("status_type")
            if (status.compareTo("open", ignoreCase = true) == 0)
                status = objectArray.getJSONObject(index).getString("status")

            workArea.add(
                ItemDetail(
                    objectArray.getJSONObject(index).getString("id"),
                    objectArray.getJSONObject(index).getString("name"),
                    objectArray.getJSONObject(index).getString("description"),
                    objectArray.getJSONObject(index).getString("cover_img_url"),
                    status,
                    objectArray.getJSONObject(index).getInt("delivery_fee")
                )
            )
        }
        retVal = workArea
    } catch (e: JSONException) {
        return null
    } finally {
        return retVal
    }
}
