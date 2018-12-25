package com.bluestone.imageexplorer.fragments

import android.os.Bundle
import com.bluestone.imageexplorer.activities.fragmentCallback
import com.bluestone.imageexplorer.datamodel.BaseFragmentInitializer
import com.bluestone.imageexplorer.datamodel.DisplayedPageState
import com.bluestone.imageexplorer.interfaces.FragmentCreationInterface
import com.bluestone.imageexplorer.recyclerviewadapters.GenericImageAdapter
import com.bluestone.imageexplorer.server.NetworkServiceInitializer
import com.bluestone.imageexplorer.server.RetrofitNetworkService
import com.bluestone.imageexplorer.utilities.populateRestaurantData
import io.reactivex.Single

class RestaurantListFragment : BaseFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            super.onCreate(it)
        } ?: run {
            setBaseFragmentState(getBaseFragmentInitializer())
            super.onCreate(savedInstanceState)
        }
    }

    override fun fetchServerData(): Single<DisplayedPageState>? {
        return parentState.serverCall.getApi()?.run {
            val result: Single<String>? =
                fetchRestaurantList(parentState.location.first, parentState.location.second, GenericImageAdapter.maxAdapterSize)
            result?.run {
                flatMap { items ->
                        populateRestaurantData(items)?.let { itemList ->
                            Single.just(DisplayedPageState(0, 0, itemList))
                        } ?: let {
                            Single.error<DisplayedPageState>(Throwable("Empty Payload"))
                        }
                }
            }
        }
    }

    override fun getFragmentId() = fragmentID

    companion object {
        const val fragmentID = "5a5e870f-01db-4e44-977e-0212501177a5"
        @JvmStatic
        fun getBaseFragmentInitializer(payLoad: String? = null) =
            BaseFragmentInitializer(
                GenericImageAdapter(),
                fragmentCallback,
                "RestaurantList",
                RetrofitNetworkService(NetworkServiceInitializer("https://api.doordash.com/v2/restaurant/")),
                fragmentID,
                Pair<Float,Float>(37.422740f, -122.139956f),
                payLoad
            )

        @JvmStatic
        fun newInstance(initialData: String?=null): FragmentCreationInterface {
            Bundle().apply {
                putSerializable(BaseFragment.BASE_FRAGMENT_INITIAL_DATA_KEY, getBaseFragmentInitializer(initialData))
            }
            return RestaurantListFragment()
        }
    }
}