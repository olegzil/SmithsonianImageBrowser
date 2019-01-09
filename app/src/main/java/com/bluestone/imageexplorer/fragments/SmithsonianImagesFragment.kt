package com.bluestone.imageexplorer.fragments

import com.bluestone.imageexplorer.dataloaders.SmithsonianImagesDataLoader
import com.bluestone.imageexplorer.datamodel.DisplayedPageState
import com.bluestone.imageexplorer.datamodel.SmithsonianKey
import com.bluestone.imageexplorer.interfaces.FragmentCreationInterface
import com.bluestone.imageexplorer.itemdetail.ItemDetail
import com.bluestone.imageexplorer.notification.ViewNotificationPayload
import com.bluestone.imageexplorer.recyclerviewadapters.GenericImageAdapter
import io.reactivex.Single

class SmithsonianImagesFragment : BaseFragment() {
    override fun displayImageMetadata(imageDescriptor: ViewNotificationPayload) {
    }

    private val dataLoader = SmithsonianImagesDataLoader(SmithsonianKey)
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
        const val fragmentID = "5a5e870f-01db-4e44-977e-0212501177a5"
        @JvmStatic
        fun newInstance(): FragmentCreationInterface {
            return SmithsonianImagesFragment()
        }
    }
}