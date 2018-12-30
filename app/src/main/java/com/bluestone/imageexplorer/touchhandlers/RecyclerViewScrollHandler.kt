package com.bluestone.imageexplorer.touchhandlers


import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.AbsListView
import com.bluestone.imageexplorer.dataloaders.SmithsonianImagesDataLoader
import com.bluestone.imageexplorer.datamodel.ScrollDirection
import com.bluestone.imageexplorer.datamodel.ScrollingParametersData
import com.bluestone.imageexplorer.itemdetail.ItemDetail
import com.bluestone.imageexplorer.recyclerviewadapters.GenericImageAdapter
import com.bluestone.imageexplorer.utilities.printLog
import com.bluestone.imageexplorer.utilities.printOrCrash
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class RecyclerViewScrollHandler(
    private val adapter: GenericImageAdapter,
    private var nextPage: Int
) : RecyclerView.OnScrollListener() {
    private var scrollAccumulator = 0
    private var prevPage = nextPage
    private val dataLoader = SmithsonianImagesDataLoader("gWGUcVRk85uDmdlt2w9VZvTaR47gmLc1iYKjiiXy")
    private var scrollDirection = ScrollDirection.IDLE

    //TODO:OZ add a selector to discriminate between vertical and horizontal scrolling
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        scrollDirection = when {
            dx < 0 -> ScrollDirection.SCROLL_RIGHT
            dx > 0 -> ScrollDirection.SCROLL_LEFT
            dy < 0 -> ScrollDirection.SCROLL_DOWN
            dy > 0 -> ScrollDirection.SCROLL_UP

            else -> ScrollDirection.IDLE
        }
    }

    private fun advancePageCounter(page: Int, first: Int, last: Int, direction: ScrollDirection): Int {
        var nextPage = page
        when (direction) {
            ScrollDirection.SCROLL_LEFT ->
                if (last >= adapter.itemCount-scrollPrefetchTrigger) {
                    ++nextPage
                    printLog("scrollAccumulator = $scrollAccumulator SCROLLING DOWN first = $first nextPage = $nextPage")
                }
            ScrollDirection.SCROLL_RIGHT ->
                if (first >= scrollPrefetchTrigger) {
                    --nextPage
                    printLog("scrollAccumulator = $scrollAccumulator SCROLLING UP first = $first nextPage = $nextPage")
                }
            else ->
                printLog("No scroll action taken")
        }
        return nextPage
    }

    fun handleScrollingChange(notifier: Observable<ScrollingParametersData>) : Disposable{
        return notifier
            .filter{scrollingParamData ->
                scrollingParamData.direction != ScrollDirection.IDLE
            }
            .flatMapSingle {scrollParameters ->
                    dataLoader.get(adapter.itemCount, scrollParameters.page)?.let {
                        it.flatMap { itemDetailList ->
                            Single.just(Pair(scrollParameters, itemDetailList))
                        }
                    }
                }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableObserver<Pair<ScrollingParametersData, List<ItemDetail>>>() {
                override fun onComplete() {

                }

                override fun onNext(items: Pair<ScrollingParametersData, List<ItemDetail>>) {
                    when (items.first.direction) {
                        ScrollDirection.SCROLL_RIGHT ->adapter.removeLastNItems(items.second)
                        ScrollDirection.SCROLL_LEFT ->  adapter.removeFirstNItems(items.second)
                        else -> printLog("no need to update paging")
                    }
                }

                override fun onError(e: Throwable) {
                    printOrCrash(e.toString())
                }
            })

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
        val scrollNotificationData = ScrollingParametersData(first, last, nextPage, scrollDirection)
        scrollNotifier.onNext(scrollNotificationData)
    }

    companion object {
        val scrollNotifier = PublishSubject.create<ScrollingParametersData>()
        const val scrollPrefetchTrigger = 3
    }
}

