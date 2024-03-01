package wrteam.ecart.rider.helper

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.view.View
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.facebook.shimmer.ShimmerFrameLayout
import wrteam.ecart.rider.R


class AppController : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        mRequestQueue = Volley.newRequestQueue(applicationContext)
        sharedPref = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)

    }

    fun <T> addToRequestQueue(req: Request<T>) {
        req.tag = TAG
        requestQueue.add(req)
    }

    fun getRequestQueue(): RequestQueue {
        return mRequestQueue
    }

    companion object {
        private const val TAG: String = "AppController"
        private lateinit var mRequestQueue: RequestQueue
        private lateinit var sharedPref: SharedPreferences

        @get:Synchronized
        var instance: AppController? = null
            private set

        private val requestQueue: RequestQueue
            get() {
                return mRequestQueue
            }


        fun isConnected(activity: Activity): Boolean {
            var check = false
            val connectionManager =
                activity.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectionManager.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {
                check = true
            }
            return check
        }

        fun toTitleCase(str: String?): String? {
            if (str == null) {
                return null
            }
            var space = true
            val builder = StringBuilder(str)
            val len = builder.length
            for (i in 0 until len) {
                val c = builder[i]
                if (space) {
                    if (!Character.isWhitespace(c)) {
                        // Convert to title case and switch out of whitespace mode.
                        builder.setCharAt(i, Character.toTitleCase(c))
                        space = false
                    }
                } else if (Character.isWhitespace(c)) {
                    space = true
                } else {
                    builder.setCharAt(i, Character.toLowerCase(c))
                }
            }
            return builder.toString()
        }

        fun showShimmer(shimmerFrameLayout: ShimmerFrameLayout, layout: View) {
            layout.visibility = View.GONE
            shimmerFrameLayout.visibility = View.VISIBLE
            shimmerFrameLayout.startShimmer()
        }

        fun hideShimmer(shimmerFrameLayout: ShimmerFrameLayout, layout: View) {
            shimmerFrameLayout.visibility = View.GONE
            shimmerFrameLayout.stopShimmer()
            layout.visibility = View.VISIBLE
        }

    }
}