package com.bluestone.imageexplorer.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.bluestone.imageexplorer.R
import com.bluestone.imageexplorer.cachemanager.CacheManager
import com.bluestone.imageexplorer.datamodel.FragmentCreationDescriptor
import com.bluestone.imageexplorer.datamodel.MainActivityData
import com.bluestone.imageexplorer.fragments.SmithsonianImagesFragment
import com.bluestone.imageexplorer.utilities.printLog
import io.reactivex.disposables.Disposables
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

val fragmentCallback = PublishSubject.create<FragmentCreationDescriptor>()

class MainActivity : AppCompatActivity() {

    private var disposable = Disposables.disposed()
    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null){
            Bundle().run {
                putSerializable(MAIN_APP_ACTIVITY_KEY, MainActivityData(fragmentCallback))
                super.onCreate(this)
            }
        } else {
            super.onCreate(savedInstanceState)
        }
        setContentView(R.layout.activity_main)
        CacheManager.initialize(applicationContext, "masterDB")
        displayNextFragment(SmithsonianImagesFragment.newInstance() as Fragment, SmithsonianImagesFragment.fragmentID)
    }

    private fun displayNextFragment(fragment: Fragment, fragmentID: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, fragmentID)
            .addToBackStack(null)
            .commit()
    }

    override fun onStart() {
        super.onStart()
        disposable = fragmentCallback.subscribeWith(object : DisposableObserver<FragmentCreationDescriptor>() {
            override fun onComplete() {}
            override fun onNext(fragmentDescriptor: FragmentCreationDescriptor) {
                displayNextFragment(fragmentDescriptor.fragment, fragmentDescriptor.fragmentTag)
            }

            override fun onError(e: Throwable) {
                printLog("MainActivity: ${e.localizedMessage}")
            }
        })
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }
    companion object {
        const val MAIN_APP_ACTIVITY_KEY="'bf0fc40e-891d-431a-b59d-735f0b72420d"
        fun getNotifier() = fragmentCallback
    }
}
