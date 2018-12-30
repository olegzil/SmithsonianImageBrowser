package com.bluestone.imageexplorer.fragments

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bluestone.imageexplorer.R
import com.bluestone.imageexplorer.cachemanager.CacheManager
import com.bluestone.imageexplorer.datamodel.BaseFragmentInitializer
import com.bluestone.imageexplorer.datamodel.DisplayedPageState
import com.bluestone.imageexplorer.datamodelloader.DisplayedPageStateLoader
import com.bluestone.imageexplorer.interfaces.FragmentCreationInterface
import com.bluestone.imageexplorer.recyclerviewadapters.GenericImageAdapter
import com.bluestone.imageexplorer.touchhandlers.RecyclerItemClickListener
import com.bluestone.imageexplorer.touchhandlers.RecyclerViewScrollHandler
import com.bluestone.imageexplorer.utilities.ImageManipulator
import com.bluestone.imageexplorer.utilities.printLog
import com.bluestone.imageexplorer.utilities.printLogWithStack
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers


open class BaseFragment : Fragment(), FragmentCreationInterface {
    private lateinit var mRecyclerView: RecyclerView
    private val disposables = CompositeDisposable()
    private var nextPage = 1
    protected lateinit var parentState: BaseFragmentInitializer
    private var shortAnimationDuration: Int = 0
    private lateinit var mainView:View
    private lateinit var recylcerViewHandler:RecyclerViewScrollHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        * If context is null, the show is over. Otherwise, we test savedInstanceState. If that value is not null
        * we use the previously saved state. If that value is null, then the run clause is executed and we try
        * to use the global state that was saved by the caller. This bit of complexity allows the class inheriting
        * from BaseFragment to be used in the xml file, i.e. <fragment android:name="com.bluestone.imageexplorer.fragments.SmithsonianImagesFragment"/>
        * Without the global state BaseFragment code will be constructed without initial data.
        * */
        context?.let { _ ->
            savedInstanceState?.let { bundle ->
                parentState =
                        bundle.getSerializable(BASE_FRAGMENT_INITIAL_DATA_KEY) as BaseFragmentInitializer
                super.onCreate(savedInstanceState)
            } ?: run {
                baseSate?.let { parentState = it }
                super.onCreate(savedInstanceState)
            }
        }
    }

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

    override fun callbackSubject() = parentState.fragmentSubject
    override fun fragment() = this
    override fun getFragmentId() = parentState.fragmentID
    private fun restoreScrollChangeObserver(){
        disposables.add(recylcerViewHandler.handleScrollingChange(RecyclerViewScrollHandler.scrollNotifier))
    }
    private fun restoreSavedAdapterSavedState(){
        populateAdapterFromSavedState(nextPage)?.run {
            val disposable = observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableSingleObserver<DisplayedPageState>() {
                    override fun onSuccess(itemDetails: DisplayedPageState) {
                        nextPage = GenericImageAdapter.maxAdapterSize / GenericImageAdapter.maxPageSize
                        parentState.genericAdapter.update(itemDetails.items)
                        mRecyclerView.layoutManager?.scrollToPosition(itemDetails.firstVisible)
                        parentState.genericAdapter.update()
                        printLog("initial count = ${parentState.genericAdapter.itemCount}")
                    }

                    override fun onError(e: Throwable) {
                        printLogWithStack(e.localizedMessage)
                        // Do Nothing. Should never reach this logic
                    }
                })
            disposable.run {
                disposables.add(this)
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }
    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        restoreScrollChangeObserver()
        restoreSavedAdapterSavedState()
        printLog("${parentState.fragmentTag}.onResume")
    }

    override fun onPause() {
        super.onPause()
//        saveAdapterState()
        disposables.clear()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    override fun onDetach() {
        super.onDetach()
        disposables.clear()
        printLog("From $parentState.fragmentTag.onDetach clearing disposables")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        saveAdapterState()
        printLog("onSaveInstanceState")
    }

    protected open fun fetchServerData(nextPage: Int): Single<DisplayedPageState>? {
        return Single.never()
    }


    private fun saveAdapterState() {
        parentState.genericAdapter.let { adapterData ->
            if (adapterData.itemCount == 0)
                return
            val currentState = DisplayedPageStateLoader(CacheManager)
            val first = (mRecyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            val nextPage: Int = currentState.get()?.nextPage ?: 0
            currentState.put(DisplayedPageState(nextPage, first, adapterData.getAllItems()))

            printLog("saveAdapterState saved ${adapterData.itemCount}")
        }
    }

    private fun populateAdapterFromSavedState(nextPage:Int): Single<DisplayedPageState>? =  fetchNextPage(nextPage)


    private fun savedStateReader(): Single<DisplayedPageState>? =
        Single.create<DisplayedPageState> { emitter ->
            val items = DisplayedPageStateLoader(CacheManager)
            items.get()?.let { itemListDescriptor ->
                emitter.onSuccess(itemListDescriptor)
            }
        }

    private fun fetchNextPage(nextPage: Int): Single<DisplayedPageState>? {
        return fetchServerData(nextPage)
    }

    protected open fun displayDetailPhoto(url: String) {
        mainView.findViewById<ImageManipulator>(R.id.expanded_image)
            ?.let {targetImageView ->
            targetImageView.visibility = View.VISIBLE
            shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
            Picasso.get()
                .load(url)
                .noPlaceholder()
                .fit()
                .centerInside()
                .into(targetImageView, object : Callback {
                    override fun onSuccess() {
                        val tempBMP = targetImageView.drawable as BitmapDrawable
                        targetImageView.setBitmap(tempBMP.bitmap, targetImageView.imageMatrix)
                    }
                    override fun onError(e: Exception?) {
                        e?.let {
                            printLog(it.localizedMessage)
                        }
                    }
                })
        }
    }

    private fun initializeFragment(view: View) {
        view.let { recyclerView ->
            mRecyclerView = recyclerView.findViewById(R.id.recycler_view)
            val mLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            mRecyclerView.layoutManager = mLayoutManager
            mRecyclerView.itemAnimator = DefaultItemAnimator()
            mRecyclerView.adapter = parentState.genericAdapter
            val divider = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.custom_devider)!!)
            mRecyclerView.addItemDecoration(divider)
            context?.let { context ->
                recylcerViewHandler = RecyclerViewScrollHandler(
                    parentState.genericAdapter,
                    nextPage
                )
                mRecyclerView.addOnScrollListener(recylcerViewHandler)

                mRecyclerView.addOnItemTouchListener(
                    RecyclerItemClickListener(
                        context,
                        mRecyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View, position: Int) {
                                displayDetailPhoto(parentState.genericAdapter.getItemDetailByPosition(position).thumbnailUrl)
                            }

                            override fun onItemLongClick(view: View?, position: Int) {
                                Toast.makeText(
                                    context,
                                    "got long click position $position item count ${parentState.genericAdapter.itemCount}",
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
        private var baseSate: BaseFragmentInitializer? = null
        fun setBaseFragmentState(state: BaseFragmentInitializer) {
            baseSate = state
        }
    }
}