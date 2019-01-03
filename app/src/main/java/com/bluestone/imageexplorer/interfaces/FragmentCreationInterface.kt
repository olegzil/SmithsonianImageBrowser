package com.bluestone.imageexplorer.interfaces

import android.support.v4.app.Fragment

interface FragmentCreationInterface {
    fun fragment(): Fragment
    fun getFragmentId(): String
}