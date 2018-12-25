package com.bluestone.imageexplorer.datamodel

import io.reactivex.subjects.PublishSubject
import java.io.Serializable

data class MainActivityData(val notifier:PublishSubject<FragmentCreationDescriptor>) : Serializable