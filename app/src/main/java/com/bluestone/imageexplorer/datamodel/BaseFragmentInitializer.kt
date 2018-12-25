package com.bluestone.imageexplorer.datamodel

import com.bluestone.imageexplorer.recyclerviewadapters.GenericImageAdapter
import com.bluestone.imageexplorer.server.RetrofitNetworkService
import io.reactivex.subjects.PublishSubject
import java.io.Serializable

data class BaseFragmentInitializer(
    val genericAdapter: GenericImageAdapter,
    val fragmentSubject: PublishSubject<FragmentCreationDescriptor>,
    val fragmentTag: String,
    val serverCall: RetrofitNetworkService,
    val fragmentID: String,
    val location:Pair<Float,Float>,
    val initialData: String? = null
) : Serializable
