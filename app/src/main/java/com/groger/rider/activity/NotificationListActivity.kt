package com.groger.rider.activity

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_notification_list.*
import org.json.JSONException
import org.json.JSONObject
import com.groger.rider.R
import com.groger.rider.adapter.NotificationAdapter
import com.groger.rider.helper.ApiConfig
import com.groger.rider.helper.AppController
import com.groger.rider.helper.Constant
import com.groger.rider.helper.Session
import com.groger.rider.model.Notification

class NotificationListActivity : AppCompatActivity() {
    lateinit var activity: Activity
    private lateinit var notifications: ArrayList<Notification?>
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var session: Session

    private var total = 0
    private var offset = 0
    private var isLoadMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_list)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.notifications)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        activity = this@NotificationListActivity

        recyclerView.layoutManager = LinearLayoutManager(activity)
        session = Session(activity)
        if (AppController.isConnected(activity)) {
            getNotificationData()
        } else {
            setSnackBar(
                activity,
                getString(R.string.no_internet_message),
                getString(R.string.ok)
            )
        }
        swipeLayout.setOnRefreshListener {
            if (AppController.isConnected(activity)) {
                offset = 0
                getNotificationData()
                swipeLayout.isRefreshing = false
                ApiConfig.disableSwipe(swipeLayout)
            } else {
                setSnackBar(
                    activity,
                    getString(R.string.no_internet_message),
                    getString(R.string.ok)
                )
            }
        }
    }

    private fun getNotificationData() {
        AppController.showShimmer(shimmerFrameLayout,swipeLayout)
        notifications = ArrayList()
        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = linearLayoutManager
        val params: MutableMap<String, String?> = HashMap()
        params[Constant.ID] = session.getData(Constant.ID)
        params[Constant.GET_NOTIFICATION] = Constant.GetVal
        params[Constant.OFFSET] = offset.toString()
        params[Constant.LIMIT] = Constant.PRODUCT_LOAD_LIMIT

//        System.out.println("====params " + params.toString());
        ApiConfig.requestToVolley({ result, response ->
            if (result) {
                try {
                    //System.out.println("====product  " + response);
                    val jsonObject = JSONObject(response.toString())
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        total = jsonObject.getString(Constant.TOTAL).toInt()
                        session.setData(Constant.TOTAL, total.toString())
                        val jsonObject1 = JSONObject(response.toString())
                        val jsonArray = jsonObject1.getJSONArray(Constant.DATA)
                        val g = Gson()
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject2 = jsonArray.getJSONObject(i)
                            if (jsonObject2 != null) {
                                val notification =
                                    g.fromJson(jsonObject2.toString(), Notification::class.java)
                                notifications.add(notification)
                            } else {
                                break
                            }
                        }
                        if (offset == 0) {
                            notificationAdapter = NotificationAdapter(activity, notifications)
                            notificationAdapter.setHasStableIds(true)
                            recyclerView.adapter = notificationAdapter
                            AppController.hideShimmer(shimmerFrameLayout,swipeLayout)
                            scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                                // if (diff == 0) {
                                if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                    val linearLayoutManager1 =
                                        recyclerView.layoutManager as LinearLayoutManager?
                                    if (notifications.size < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager1 != null && linearLayoutManager1.findLastCompletelyVisibleItemPosition() == notifications.size - 1) {
                                                //bottom of list!
                                                notifications.add(null)
                                                notificationAdapter.notifyItemInserted(
                                                    notifications.size - 1
                                                )
                                                offset += Constant.LOAD_ITEM_LIMIT
                                                val params1: MutableMap<String, String?> = HashMap()
                                                params1[Constant.ID] = session.getData(Constant.ID)
                                                params1[Constant.GET_NOTIFICATION] = Constant.GetVal
                                                params1[Constant.OFFSET] = offset.toString()
                                                params1[Constant.LIMIT] =
                                                    Constant.PRODUCT_LOAD_LIMIT
                                                ApiConfig.requestToVolley({ result, response ->
                                                    if (result) {
                                                        try {
                                                            // System.out.println("====product  " + response);
                                                            val jsonObject3 = JSONObject(response.toString())
                                                            if (!jsonObject3.getBoolean(Constant.ERROR)) {
                                                                session.setData(
                                                                    Constant.TOTAL,
                                                                    jsonObject3.getString(
                                                                        Constant.TOTAL
                                                                    )
                                                                )
                                                                notifications.removeAt(
                                                                    notifications.size - 1
                                                                )
                                                                notificationAdapter.notifyItemRemoved(
                                                                    notifications.size
                                                                )
                                                                val jsonObject4 =
                                                                    JSONObject(response.toString())
                                                                val jsonArray1 =
                                                                    jsonObject4.getJSONArray(
                                                                        Constant.DATA
                                                                    )
                                                                for (i in 0 until jsonArray1.length()) {
                                                                    val jsonObject5 =
                                                                        jsonArray1.getJSONObject(i)
                                                                    if (jsonObject5 != null) {
                                                                        val notification =
                                                                            g.fromJson(
                                                                                jsonObject5.toString(),
                                                                                Notification::class.java
                                                                            )
                                                                        notifications.add(
                                                                            notification
                                                                        )
                                                                    } else {
                                                                        break
                                                                    }
                                                                }
                                                                notificationAdapter.notifyDataSetChanged()
                                                                notificationAdapter.setLoaded()
                                                                isLoadMore = false
                                                            }
                                                        } catch (e: JSONException) {
                                                            e.printStackTrace()
                                                        }
                                                    }
                                                }, activity, Constant.MAIN_URL, params1)
                                                isLoadMore = true
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                } catch (e: JSONException) {
                    AppController.hideShimmer(shimmerFrameLayout,swipeLayout)
                    e.printStackTrace()
                }
            }
        }, activity, Constant.MAIN_URL, params)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun setSnackBar(activity: Activity, message: String?, action: String?) {
        val snackBar = Snackbar.make(
            activity.findViewById(android.R.id.content),
            message.toString(),
            Snackbar.LENGTH_INDEFINITE
        )
        snackBar.setAction(action) {
            getNotificationData()
            snackBar.dismiss()
        }
        snackBar.setActionTextColor(Color.RED)
        val snackBarView = snackBar.view
        val textView =
            snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = 5
        snackBar.show()
    }
}