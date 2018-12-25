package com.bluestone.imageexplorer.interfaces

import android.support.v4.app.Fragment
import com.bluestone.imageexplorer.datamodel.FragmentCreationDescriptor
import io.reactivex.subjects.PublishSubject

interface FragmentCreationInterface {
    fun callbackSubject(): PublishSubject<FragmentCreationDescriptor>
    fun fragment(): Fragment
    fun getFragmentId(): String
}