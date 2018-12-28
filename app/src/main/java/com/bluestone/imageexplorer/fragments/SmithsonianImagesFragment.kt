package com.bluestone.imageexplorer.fragments

import android.os.Bundle
import com.bluestone.imageexplorer.activities.fragmentCallback
import com.bluestone.imageexplorer.dataloaders.SmithsonianImagesDataLoader
import com.bluestone.imageexplorer.datamodel.BaseFragmentInitializer
import com.bluestone.imageexplorer.datamodel.DisplayedPageState
import com.bluestone.imageexplorer.interfaces.FragmentCreationInterface
import com.bluestone.imageexplorer.recyclerviewadapters.GenericImageAdapter
import io.reactivex.Single

class SmithsonianImagesFragment : BaseFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            super.onCreate(it)
        } ?: run {
            setBaseFragmentState(getBaseFragmentInitializer())
            super.onCreate(savedInstanceState)
        }
    }

    override fun fetchServerData(nextPage:Int): Single<DisplayedPageState>? {
        return parentState.dataLoader.get(GenericImageAdapter.maxAdapterSize, nextPage)?.run {
            flatMap { items ->
                Single.just(DisplayedPageState(0, 0, items))
            } ?: let {
                Single.error<DisplayedPageState>(Throwable("Empty Payload"))
            }
        }
    }


    override fun getFragmentId() = fragmentID

    companion object {
        const val fragmentID = "5a5e870f-01db-4e44-977e-0212501177a5"
        @JvmStatic
        fun getBaseFragmentInitializer(payLoad: String? = null): BaseFragmentInitializer {
            return BaseFragmentInitializer(
                GenericImageAdapter(),
                fragmentCallback,
                "SmithsonianImageList",
                SmithsonianImagesDataLoader("gWGUcVRk85uDmdlt2w9VZvTaR47gmLc1iYKjiiXy"),
                fragmentID,
                payLoad
            )
        }

        @JvmStatic
        fun newInstance(initialData: String? = null): FragmentCreationInterface {
            Bundle().apply {
                putSerializable(BaseFragment.BASE_FRAGMENT_INITIAL_DATA_KEY, getBaseFragmentInitializer(initialData))
            }
            return SmithsonianImagesFragment()
        }
    }
}