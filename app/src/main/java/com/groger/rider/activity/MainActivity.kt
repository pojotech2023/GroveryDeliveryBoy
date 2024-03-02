@file:Suppress("OverrideDeprecatedMigration")

package com.groger.rider.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_drawer.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import com.groger.rider.R
import com.groger.rider.adapter.OrderListAdapter
import com.groger.rider.helper.ApiConfig
import com.groger.rider.helper.AppController
import com.groger.rider.helper.Constant
import com.groger.rider.helper.Session
import com.groger.rider.model.OrderList

class MainActivity : DrawerActivity() {
    private lateinit var session1: Session
    private var doubleBackToExitPressedOnce = false
    private lateinit var activity1: Activity
    private lateinit var orderListAdapter: OrderListAdapter
    private var total = 0
    private var isLoadMore = false
    private var offset = 0
    lateinit var menu: Menu

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_main, content_frame)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        activity1 = this@MainActivity
        session1 = Session(activity1)

        if (session1.isUserLoggedIn) {
            AppController.showShimmer(shimmerFrameLayout, swipeRefresh)
            getData()
            getDeliveryBoyData(activity1)
        }

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            if (AppController.isConnected(activity1)) {
                AppController.showShimmer(shimmerFrameLayout, swipeRefresh)
                offset = 0
                getData()
                getDeliveryBoyData(activity1)
                ApiConfig.disableSwipe(swipeRefresh)
            } else {
                setSnackBar(
                    activity1,
                    getString(R.string.no_internet_message),
                    getString(R.string.retry)
                )
            }
        }
        drawerToggle = object : ActionBarDrawerToggle(
            this,
            drawer_layout, toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        ) {}

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            session1.setData(Constant.FCM_ID, token)
            updateFCMId()
        }
    }

    private fun updateFCMId() {
        val params: MutableMap<String, String?> = HashMap()
        params[Constant.ID] = session1.getData(Constant.ID)
        params[Constant.UPDATE_DELIVERY_BOY_FCM_ID] = Constant.GetVal
        params[Constant.FCM_ID] = session1.getData(Constant.FCM_ID)
        ApiConfig.requestToVolley(
            { _: Boolean, _: String? -> },
            activity1,
            Constant.MAIN_URL,
            params
        )
    }

    private fun getData() {
        orderListArrayList = ArrayList()
        val linearLayoutManager = LinearLayoutManager(activity1)
        recycleOrderList.layoutManager = linearLayoutManager
        val params: MutableMap<String, String?> = HashMap()
        params[Constant.ID] = session1.getData(Constant.ID)
        params[Constant.GET_ORDERS_BY_DELIVERY_BOY_ID] = Constant.GetVal
        params[Constant.OFFSET] = "" + offset
        params[Constant.LIMIT] = Constant.PRODUCT_LOAD_LIMIT
        ApiConfig.requestToVolley({ result, response ->
            if (result) {
                try {
                    //    System.out.println("====product  " + response);
                    val jsonObject = JSONObject(response.toString())
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        total = jsonObject.getString(Constant.TOTAL).toInt()
                        session1.setData(Constant.TOTAL, total.toString())
                        val jsonObject1 = JSONObject(response.toString())
                        val jsonArray = jsonObject1.getJSONArray(Constant.DATA)
                        val g = Gson()
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject2 = jsonArray.getJSONObject(i)
                            if (jsonObject2 != null) {
                                val orderList =
                                    g.fromJson(jsonObject2.toString(), OrderList::class.java)
                                orderListArrayList.add(orderList)
                            } else {
                                break
                            }
                        }
                        if (offset == 0) {
                            orderListAdapter = OrderListAdapter(activity1, orderListArrayList)
                            orderListAdapter.setHasStableIds(true)
                            recycleOrderList.adapter = orderListAdapter
                            AppController.hideShimmer(shimmerFrameLayout, swipeRefresh)
                            scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                                // if (diff == 0) {
                                if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                    val linearLayoutManager1 =
                                        recycleOrderList.layoutManager as LinearLayoutManager?
                                    if (orderListArrayList.size < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager1 != null && linearLayoutManager1.findLastCompletelyVisibleItemPosition() == orderListArrayList.size - 1) {
                                                //bottom of list!
                                                orderListArrayList.add(null)
                                                orderListAdapter.notifyItemInserted(
                                                    orderListArrayList.size - 1
                                                )
                                                offset += Constant.LOAD_ITEM_LIMIT
                                                val params1: MutableMap<String, String?> = HashMap()
                                                params1[Constant.ID] = session1.getData(Constant.ID)
                                                params1[Constant.GET_ORDERS_BY_DELIVERY_BOY_ID] =
                                                    Constant.GetVal
                                                params1[Constant.LIMIT] =
                                                    "" + Constant.LOAD_ITEM_LIMIT
                                                params1[Constant.OFFSET] = "" + offset
                                                ApiConfig.requestToVolley({ result, response ->
                                                    if (result) {
                                                        try {
                                                            // System.out.println("====product  " + response);
                                                            val jsonObject3 =
                                                                JSONObject(response.toString())
                                                            if (!jsonObject3.getBoolean(Constant.ERROR)) {
                                                                session1.setData(
                                                                    Constant.TOTAL,
                                                                    jsonObject3.getString(
                                                                        Constant.TOTAL
                                                                    )
                                                                )
                                                                orderListArrayList.removeAt(
                                                                    orderListArrayList.size - 1
                                                                )
                                                                orderListAdapter.notifyItemRemoved(
                                                                    orderListArrayList.size
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
                                                                        val orderList = g.fromJson(
                                                                            jsonObject5.toString(),
                                                                            OrderList::class.java
                                                                        )
                                                                        orderListArrayList.add(
                                                                            orderList
                                                                        )
                                                                    } else {
                                                                        break
                                                                    }
                                                                }
                                                                orderListAdapter.notifyDataSetChanged()
                                                                isLoadMore = false
                                                            }
                                                        } catch (e: JSONException) {
                                                            e.printStackTrace()
                                                        }
                                                    }
                                                }, activity1, Constant.MAIN_URL, params1)
                                                isLoadMore = true
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                } catch (e: JSONException) {
                    AppController.hideShimmer(shimmerFrameLayout, swipeRefresh)
                    e.printStackTrace()
                }
            }
        }, activity1, Constant.MAIN_URL, params)
    }

    private fun getTotalOrderCount(activity1: Activity) {
        if (AppController.isConnected(activity1)) {
            val params: MutableMap<String, String?> = HashMap()
            params[Constant.ID] = session1.getData(Constant.ID)
            params[Constant.GET_ORDERS_BY_DELIVERY_BOY_ID] = Constant.GetVal
            ApiConfig.requestToVolley({ result, response ->
                if (result) {
                    try {
                        val jsonObject = JSONObject(response.toString())
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            session1.setData(Constant.TOTAL, jsonObject.getString(Constant.TOTAL))
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }, activity1, Constant.MAIN_URL, params)
        } else {
            setSnackBar(
                activity1,
                getString(R.string.no_internet_message),
                getString(R.string.retry)
            )
        }
    }

    private fun getDeliveryBoyData(activity1: Activity) {
        if (AppController.isConnected(activity1)) {
            val params: MutableMap<String, String?> = HashMap()
            params[Constant.ID] = session1.getData(Constant.ID)
            params[Constant.GET_DELIVERY_BOY_BY_ID] = Constant.GetVal
            ApiConfig.requestToVolley({ result, response -> //  System.out.println("============" + response);
                if (result) {
                    try {
                        val jsonObject = JSONObject(response.toString())
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            startMainActivity(
                                activity1,
                                jsonObject.getJSONArray(Constant.DATA).getJSONObject(0)
                            )
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }, activity1, Constant.MAIN_URL, params)
        } else {
            setSnackBar(
                activity1,
                getString(R.string.no_internet_message),
                getString(R.string.retry)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    fun startMainActivity(activity1: Activity, jsonObject: JSONObject) {
        if (AppController.isConnected(activity1)) {
            try {
                Session(activity1).createUserLoginSession(
                    jsonObject.getString(Constant.FCM_ID),
                    jsonObject.getString(Constant.ID),
                    jsonObject.getString(Constant.NAME),
                    jsonObject.getString(Constant.MOBILE),
                    jsonObject.getString(Constant.PASSWORD),
                    jsonObject.getString(Constant.ADDRESS),
                    jsonObject.getString(Constant.BONUS),
                    jsonObject.getString(Constant.BALANCE),
                    jsonObject.getString(Constant.STATUS),
                    jsonObject.getString(Constant.IS_AVAILABLE),
                    jsonObject.getString(Constant.CREATED_AT)
                )
                session1.setData(Constant.ID, jsonObject.getString(Constant.ID))
                getTotalOrderCount(activity1)
                tvOrdersCount.text = session1.getData(Constant.TOTAL)
                tvBalanceCount.text =
                    Session(activity1).getData(Constant.CURRENCY) + session1.getData(
                        Constant.BALANCE
                    )
                tvBonusCount.text = session1.getData(Constant.BONUS) + " %"
                tvOrdersCount.visibility = View.VISIBLE
                tvBalanceCount.visibility = View.VISIBLE
                tvBonusCount.visibility = View.VISIBLE
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            setSnackBar(
                activity1,
                getString(R.string.no_internet_message),
                getString(R.string.retry)
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(nav_view)) drawer_layout.closeDrawers() else doubleBack()
    }

    private fun doubleBack() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(
            this, activity1.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()

        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    private fun setSnackBar(activity1: Activity, message: String?, action: String?) {
        val snackBar = Snackbar.make(
            activity1.findViewById(android.R.id.content),
            message.toString(),
            Snackbar.LENGTH_INDEFINITE
        )
        snackBar.setAction(action) { snackBar.dismiss() }
        snackBar.setActionTextColor(Color.RED)
        val snackBarView = snackBar.view
        val textView = snackBarView.findViewById<TextView>(R.id.snackbar_text)
        textView.maxLines = 5
        snackBar.show()
    }

    private fun enableDisableDeliveryBoy(status: String) {
        val params: MutableMap<String, String?> = HashMap()
        params[Constant.DELIVERY_BOY_ID] = session1.getData(Constant.ID)
        params[Constant.UPDATE_DELIVERY_BOY_IS_AVAILABLE] = Constant.GetVal
        params[Constant.IS_AVAILABLE] = status
        ApiConfig.requestToVolley({ result, response -> //  System.out.println("============" + response);
            if (result) {
                try {
                    val jsonObject = JSONObject(response.toString())
                    val message =
                        getString(R.string.delivery_boy_status) + ((if (status == "1") getString(R.string.enabled) else getString(
                            R.string.disabled
                        )) + getString(R.string.successfully))
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        if (status == "1") {
                            menu.findItem(R.id.toolbar_action).setIcon(R.drawable.ic_action_off)
                        } else {
                            menu.findItem(R.id.toolbar_action).setIcon(R.drawable.ic_action_on)
                        }
                        Toast.makeText(activity1, message, Toast.LENGTH_SHORT).show()
                        session1.setData(Constant.IS_AVAILABLE, status)
                        invalidateOptionsMenu()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }, activity1, Constant.MAIN_URL, params)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.toolbar_action) {
            enableDisableDeliveryBoy(if (session1.getData(Constant.IS_AVAILABLE) == "1") "0" else "1")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menu.findItem(R.id.toolbar_filter).isVisible = false
        menu.findItem(R.id.toolbar_action).isVisible = true
        if (session1.getData(Constant.IS_AVAILABLE) == "1") {
            menu.findItem(R.id.toolbar_action).setIcon(R.drawable.ic_action_off)
        } else {
            menu.findItem(R.id.toolbar_action).setIcon(R.drawable.ic_action_on)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        if (Constant.CLICK) {
            orderListAdapter.notifyDataSetChanged()
            Constant.CLICK = false
        }
    }

    companion object {
        lateinit var orderListArrayList: ArrayList<OrderList?>
    }
}