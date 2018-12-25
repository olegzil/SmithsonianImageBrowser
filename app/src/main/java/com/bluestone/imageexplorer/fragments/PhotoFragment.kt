package com.bluestone.imageexplorer.fragments

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bluestone.imageexplorer.R
import com.bluestone.imageexplorer.cachemanager.CacheManager
import com.bluestone.imageexplorer.utilities.ImageScroller
import com.bluestone.imageexplorer.utilities.printLog
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val PHOTO_URL = "param1"

/**
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [PhotoFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PhotoFragment : Fragment() {
    private var photoUrl: String? = null
    private val photofragmentkey = "photofragmentkey"
    private lateinit var mainView: View
    private var shortAnimationDuration: Int = 0
    private lateinit var targetImage: ImageScroller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            photoUrl = bundle.getString(PHOTO_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragmentphoto, container, false)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        val targetView = mainView.findViewById<ImageView>(R.id.fragment_image)
        Picasso.get()
            .load(photoUrl)
            .noPlaceholder()
            .fit()
            .centerInside()
            .into(targetView, object : Callback {
                override fun onSuccess() {
                    val tempBMP = targetView.drawable as BitmapDrawable
                    targetImage = mainView.findViewById(R.id.expanded_image)
                    targetView.visibility = View.GONE
                    targetImage.visibility = View.VISIBLE
                    targetImage.setBitmap(tempBMP.bitmap, targetView.imageMatrix)
                }

                override fun onError(e: Exception?) {
                    e?.let {
                        printLog(it.localizedMessage)
                    }
                }
            }
            )
        return mainView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        photoUrl?.let { url ->
            CacheManager.deleteKey(photofragmentkey)
            CacheManager.putString(
                photofragmentkey,
                url
            )
            printLog("saveAdapterState saved $url")
        }
    }

    companion object {
        val fragmentID = "c1b32631-4b46-4c7b-9e32-edcaf47b3e49"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param photo_url Parameter 1.
         * @return A new instance of fragment PhotoFragment.
         */
        @JvmStatic
        fun newInstance(photo_url: String) =
            PhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(PHOTO_URL, photo_url)
                }
            }
    }
}
