package com.bluestone.imageexplorer.datamodel

import com.bluestone.imageexplorer.interfaces.DataLoaderInterface
import com.bluestone.imageexplorer.recyclerviewadapters.GenericImageAdapter
import io.reactivex.subjects.PublishSubject
import java.io.Serializable

data class BaseFragmentInitializer(
    val genericAdapter: GenericImageAdapter,
    val fragmentSubject: PublishSubject<FragmentCreationDescriptor>,
    val fragmentTag: String,
    val dataLoader: DataLoaderInterface,
    val fragmentID: String,
    val initialData: String? = null
) : Serializable
