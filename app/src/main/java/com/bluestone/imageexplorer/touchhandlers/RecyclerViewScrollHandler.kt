package com.bluestone.imageexplorer.touchhandlers


import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.AbsListView
import android.widget.Toast
import com.bluestone.imageexplorer.datamodel.DisplayedPageState
import com.bluestone.imageexplorer.recyclerviewadapters.GenericImageAdapter
import com.bluestone.imageexplorer.server.RetrofitNetworkService
import com.bluestone.imageexplorer.utilities.populateRestaurantData
import com.bluestone.imageexplorer.utilities.printLog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class RecyclerViewScrollHandler(
    private val context: Context,
    private val adapter: GenericImageAdapter,
    private val serverCall: RetrofitNetworkService,
    private val location:Pair<Float,Float>,
    private var nextPage: Int
) : RecyclerView.OnScrollListener() {
    private var disposable = Disposables.disposed()
    private var scrollAccumulator = 0
    private var prevPage = nextPage

    private enum class ScrollDirection { SCROLL_DOWN, SCROLL_UP, IDLE }

    private var scrollDirection = ScrollDirection.IDLE
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        scrollDirection = when {
            dy < 0 -> ScrollDirection.SCROLL_DOWN
            dy > 0 -> ScrollDirection.SCROLL_UP
            else -> ScrollDirection.IDLE
        }
    }

    private fun advancePageCounter(page: Int, first: Int, last: Int, direction: ScrollDirection): Int {
        var nextPage = page
        when (direction) {
            ScrollDirection.SCROLL_DOWN ->
                if (first == 0 && nextPage > 1) {
                    --nextPage
                    printLog("scrollAccumulator = $scrollAccumulator SCROLLING DOWN first = $first nextPage = $nextPage")
                }
            ScrollDirection.SCROLL_UP ->
                if (last >= GenericImageAdapter.maxAdapterSize - 1) {
                    ++nextPage
                    printLog("scrollAccumulator = $scrollAccumulator SCROLLING UP first = $first nextPage = $nextPage")
                }
            else ->
                printLog("No scroll action taken")
        }
        return nextPage
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
            return
        val last = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        val first = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        nextPage = advancePageCounter(nextPage, first, last, scrollDirection)

        if (prevPage == nextPage)
            return
        prevPage = nextPage
        disposable.dispose()
        printLog("Count before scroll ${adapter.itemCount}")
        serverCall.getApi()?.run {
            disposable = this.fetchRLNextPage(location.first, location.second, GenericImageAdapter.maxPageSize, nextPage)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableSingleObserver<String>() {
                    override fun onSuccess(items: String) {
                        populateRestaurantData(items)?.let { itemList ->
                            when (scrollDirection) {
                                ScrollDirection.SCROLL_UP -> if (last == GenericImageAdapter.maxAdapterSize - 1) adapter.removeFirstNItems(
                                    itemList
                                )
                                ScrollDirection.SCROLL_DOWN -> if (first == 0) adapter.removeLastNItems(itemList)
                                else -> printLog("no need to update paging")
                            }
                        } ?: let {
                            Single.error<DisplayedPageState>(Throwable("Empty Payload"))
                        }
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}