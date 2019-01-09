package com.bluestone.imageexplorer.notification

import android.view.View

data class ViewNotificationPayload(val parentView: View, val targetView:View, val position:Int, val url:String, val busy:Boolean)