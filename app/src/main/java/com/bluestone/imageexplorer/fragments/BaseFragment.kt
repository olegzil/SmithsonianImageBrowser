package com.bluestone.imageexplorer.fragments

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.bluestone.imageexplorer.R
import com.bluestone.imageexplorer.cachemanager.CacheManager
import com.bluestone.imageexplorer.dataloaders.SmithsonianImagesDataLoader
import com.bluestone.imageexplorer.datamodel.DisplayedPageState
import com.bluestone.imageexplorer.datamodel.FragmentScrollData
import com.bluestone.imageexplorer.datamodel.SmithsonianKey
import com.bluestone.imageexplorer.interfaces.FragmentCreationInterface
import com.bluestone.imageexplorer.itemdetail.ItemDetail
import com.bluestone.imageexplorer.notification.ViewNotificationPayload
import com.bluestone.imageexplorer.recyclerviewadapters.GenericImageAdapter
import com.bluestone.imageexplorer.touchhandlers.RecyclerItemClickListener
import com.bluestone.imageexplorer.touchhandlers.RecyclerViewScrollHandler
import com.bluestone.imageexplorer.utilities.ImageManipulator
import com.bluestone.imageexplorer.utilities.printLog
import com.bluestone.imageexplorer.utilities.printLogWithStack
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.json.JSON

abstract class BaseFragment : Fragment(), FragmentCreationInterface {
    private lateinit var mRecyclerView: RecyclerView
    private val disposables = CompositeDisposable()
    private var nextPage = 1
    private var shortAnimationDuration: Int = 0
    private lateinit var mainView: View
    private lateinit var recyclerViewScrollHandler: RecyclerViewScrollHandler
    private val adapter = GenericImageAdapter()
    private val dataloader = SmithsonianImagesDataLoader(SmithsonianKey)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // If there is no saved state, initiate a server request to populate the recycler view adapter
        mainView = inflater.inflate(R.layout.fragment_recycler, container, false)
        initializeFragment(mainView)
        super.onCreateView(inflater, container, savedInstanceState)
        return mainView
    }

    override fun fragment() = this
    override fun getFragmentId() = ""
    private fun restoreScrollChangeObserver() {
        disposables.add(recyclerViewScrollHandler.handleScrollingChange(RecyclerViewScrollHandler.scrollNotifier))
    }

    private fun restoreSavedAdapterSavedState() {
        populateAdapterFromSavedState(nextPage)?.run {
            val disposable = observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableSingleObserver<DisplayedPageState>() {
                    override fun onSuccess(itemDetails: DisplayedPageState) {
                        mainView.findViewById<TextView>(R.id.message_text).visibility = View.GONE
                        adapter.cancelImageFetching()
                        adapter.update(itemDetails.items)
                        mRecyclerView.layoutManager?.scrollToPosition(itemDetails.firstVisible)
                    }

                    override fun onError(e: Throwable) {
                        val myTextSize = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            18F,
                            context?.resources?.displayMetrics
                        )
                        mainView.findViewById<TextView>(R.id.message_text).text = e.localizedMessage
                        mainView.findViewById<TextView>(R.id.message_text)
                            .setTextSize(TypedValue.COMPLEX_UNIT_SP, myTextSize)
                        mainView.findViewById<TextView>(R.id.message_text).visibility = View.VISIBLE
                        printLogWithStack(e.localizedMessage)
                    }
                })
            disposable.run {
                disposables.add(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        restoreScrollState()
        restoreScrollChangeObserver()
        restoreSavedAdapterSavedState()
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
        saveScrollState()
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }

    private fun restoreScrollState() {
        val savedState = CacheManager.getString(BASE_FRAGMENT_INITIAL_DATA_KEY)
        savedState?.let { jsonString ->
            try {
                JSON.parse(FragmentScrollData.serializer(), jsonString).let { scrollData ->
                    nextPage = scrollData.nextPage
                }
            } catch (e: MissingFieldException) {
                nextPage = 2
            }
        }
    }

    private fun saveScrollState() {
        val state =
            FragmentScrollData(
                recyclerViewScrollHandler.getScrollState().nextPage,
                recyclerViewScrollHandler.getScrollState().firstItemOnScreen,
                recyclerViewScrollHandler.getScrollState().lastItemOnScreen
            )
        CacheManager.putString(
            BaseFragment.BASE_FRAGMENT_INITIAL_DATA_KEY,
            JSON.stringify(FragmentScrollData.serializer(), state)
        )
    }

    abstract fun fetchServerData(nextPage: Int): Single<DisplayedPageState>?

    private fun populateAdapterFromSavedState(nextPage: Int): Single<DisplayedPageState>? = fetchServerData(nextPage)
    abstract fun fetchNextPage(nextPage: Int): Single<List<ItemDetail>>?

    private fun displayDetail(url: String, pos: Int): Observable<ViewNotificationPayload> {
        return Observable.create { emitter ->
            val imageView = mainView.findViewById<ImageManipulator>(R.id.expanded_image)
            imageView?.let { targetImageView ->
                val target = object : Target {
                    fun toggleWaitMessage(flag: Boolean) {
                        if (flag) {
                            val textView = mainView.findViewById<TextView>(R.id.selected_index)
                            val loadingPanel = mainView.findViewById<RelativeLayout>(R.id.loadingPanel)
                            textView?.let {
                                targetImageView.visibility = View.GONE
                                loadingPanel.visibility = View.VISIBLE
                                it.visibility = View.VISIBLE
                                it.text = resources.getString(R.string.wait_loading)
                            }
                        } else {
                            val textView = mainView.findViewById<TextView>(R.id.selected_index)
                            val loadingPanel = mainView.findViewById<RelativeLayout>(R.id.loadingPanel)
                            textView?.let {
                                it.text = ""
                                it.visibility = View.GONE
                                loadingPanel.visibility = View.GONE
                            }
                            targetImageView.visibility = View.VISIBLE
                        }
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        emitter.onNext(ViewNotificationPayload(mainView, targetImageView, pos, url, false))
                        printLog(e.toString())
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        toggleWaitMessage(false)
                        bitmap?.let { newImageBitmap ->
                            targetImageView.setBitmap(newImageBitmap, targetImageView.imageMatrix)
                            printLog("bit map loaded")
                        }
                        emitter.onNext(ViewNotificationPayload(mainView, targetImageView, pos, url, false))
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        emitter.onNext(ViewNotificationPayload(mainView, targetImageView, pos, url, true))
                        toggleWaitMessage(true)
                    }

                }
                targetImageView.visibility = View.VISIBLE
                targetImageView.tag = target
                shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
                Picasso.get()
                    .load(url)
                    .noPlaceholder()
                    .into(target)
            }
        }
    }

    abstract fun displayImageMetadata(imageDescriptor: ViewNotificationPayload)

    private fun initializeFragment(view: View) {
        view.let { recyclerView ->
            mRecyclerView = recyclerView.findViewById(R.id.recycler_view)
            val mLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            mRecyclerView.layoutManager = mLayoutManager
            mRecyclerView.itemAnimator = DefaultItemAnimator()
            mRecyclerView.adapter = adapter
            context?.let { context ->
                recyclerViewScrollHandler = RecyclerViewScrollHandler(
                    adapter,
                    nextPage
                ) { np -> dataloader.get(GenericImageAdapter.maxAdapterSize, np) }
                mRecyclerView.addOnScrollListener(recyclerViewScrollHandler)

                mRecyclerView.addOnItemTouchListener(
                    RecyclerItemClickListener(
                        context,
                        mRecyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            var busy = false
                            override fun onItemClick(view: View, position: Int) {
                                if (busy)
                                    return
                                disposables.add(
                                    displayDetail(
                                        adapter.getItemDetailByPosition(position).fullResolutionURL,
                                        position
                                    ).subscribeWith(object : DisposableObserver<ViewNotificationPayload>() {
                                        override fun onNext(viewNotificationPayload: ViewNotificationPayload) {
                                            busy = viewNotificationPayload.busy
                                            viewNotificationPayload.targetView.setOnClickListener {
                                                displayImageMetadata(viewNotificationPayload)
                                            }
                                        }

                                        override fun onError(e: Throwable) {}
                                        override fun onComplete() {}
                                    })
                                )
                            }

                            override fun onItemLongClick(view: View?, position: Int) {
                                Toast.makeText(
                                    context,
                                    "got long click position $position item count ${adapter.itemCount}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                )
            }
        }
    }

    companion object {
        const val BASE_FRAGMENT_INITIAL_DATA_KEY = "19056c4f-5406-4505-9192-164e4b1cbd04"
    }
}