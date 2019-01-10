package com.bluestone.imageexplorer.fragments

import com.bluestone.imageexplorer.dataloaders.PixabayDataLoader
import com.bluestone.imageexplorer.datamodel.DisplayedPageState
import com.bluestone.imageexplorer.datamodel.PixabayKey
import com.bluestone.imageexplorer.itemdetail.ItemDetail
import com.bluestone.imageexplorer.notification.ViewNotificationPayload
import com.bluestone.imageexplorer.recyclerviewadapters.GenericImageAdapter
import io.reactivex.Single

class PixabayImagesFragment : BaseFragment() {
    override fun displayImageMetadata(imageDescriptor: ViewNotificationPayload) {

    }

    private val dataLoader = PixabayDataLoader(PixabayKey)
    override fun fetchNextPage(nextPage: Int): Single<List<ItemDetail>>? =
        dataLoader.get(GenericImageAdapter.maxAdapterSize, nextPage)

    override fun fetchServerData(nextPage: Int): Single<DisplayedPageState>? {
        return dataLoader.get(GenericImageAdapter.maxAdapterSize, nextPage)?.run {
            flatMap { items ->
                Single.just(DisplayedPageState(nextPage, 0, items))
            } ?: let {
                Single.error<DisplayedPageState>(Throwable("Empty Payload"))
            }
        }
    }


    override fun getFragmentId() = fragmentID
    companion object {
        const val fragmentID = "c7cc7bd1-2467-4981-9463-1d65845ba937"
        @JvmStatic
        fun newInstance(): PixabayImagesFragment {
            return PixabayImagesFragment()
        }
    }
}