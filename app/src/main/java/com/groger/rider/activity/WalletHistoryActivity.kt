package com.groger.rider.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_wallet_history.*
import kotlinx.android.synthetic.main.lyt_header.*
import org.json.JSONException
import org.json.JSONObject
import com.groger.rider.R
import com.groger.rider.adapter.WalletHistoryAdapter
import com.groger.rider.helper.ApiConfig
import com.groger.rider.helper.AppController
import com.groger.rider.helper.Constant
import com.groger.rider.helper.Session
import com.groger.rider.model.WalletHistory

class WalletHistoryActivity : AppCompatActivity() {
    private lateinit var session: Session
    private lateinit var walletHistories: ArrayList<WalletHistory?>
    private lateinit var walletHistoryAdapter: WalletHistoryAdapter
    private lateinit var activity: Activity
    private var total = 0
    private var isLoadMore = false
    private var offset = 0
    private var dataType = Constant.FUND_TRANSFERS
    private var filterIndex = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_history)
        activity = this@WalletHistoryActivity
        session = Session(activity)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        tvBalance.text = Session(activity).getData(Constant.CURRENCY) + session.getData(Constant.BALANCE)?.toDouble()
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.wallet_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        walletHistory()
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            offset = 0
            walletHistory()
        }

        btnSendWithdrawalRequest.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this@WalletHistoryActivity)
            val inflater = this@WalletHistoryActivity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_confirm_send_request, null)
            alertDialog.setView(dialogView)
            alertDialog.setCancelable(true)
            val dialog = alertDialog.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val tvDialogSend = dialogView.findViewById<TextView>(R.id.tvDialogSend)
            val tvDialogCancel = dialogView.findViewById<TextView>(R.id.tvDialogCancel)
            val edtAmount = dialogView.findViewById<EditText>(R.id.edtAmount)
            val edtMsg = dialogView.findViewById<EditText>(R.id.edtMsg)
            tvDialogSend.setOnClickListener {
                if (edtAmount.text.toString().isNotEmpty() || edtAmount.text.toString() == "0") {
                    if (edtAmount.text.toString().toDouble() <= session.getData(Constant.BALANCE)?.toDouble()!!) {
                        sendWithdrawalRequest(edtAmount.text.toString().trim() ,edtMsg.text.toString().trim())
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            R.string.alert_balance_limit,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    edtAmount.error = getString(R.string.alert_enter_amount)
                }
            }
            tvDialogCancel.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }

    private fun sendWithdrawalRequest(amount: String, message: String) {
        val params: MutableMap<String, String?> = HashMap()
        params[Constant.ID] = session.getData(Constant.ID)
        params[Constant.SEND_WITHDRAWAL_REQUEST] = Constant.GetVal
        params[Constant.TYPE] = Constant.DELIVERY_BOY
        params[Constant.AMOUNT] = amount
        params[Constant.MESSAGE] = message
        ApiConfig.requestToVolley({ result, response ->
            if (result) {
                try {
                    val jsonObject1 = JSONObject(response.toString())
                    if (!jsonObject1.getBoolean(Constant.ERROR)) {
                        val updateBalance = jsonObject1.getString(Constant.UPDATED_BALANCE)
                        tvBalance.text = updateBalance
                        tvWallet?.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_wallet_white,
                            0,
                            0,
                            0
                        )
                        tvWallet?.text = getString(
                            R.string.wallet_balance,
                            Session(activity).getData(Constant.CURRENCY),
                            updateBalance
                        )
                        session.setData(
                            Constant.BALANCE,
                            jsonObject1.getString(Constant.UPDATED_BALANCE)
                        )
                    }
                    Toast.makeText(activity,jsonObject1.getString(Constant.MESSAGE),Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }, activity, Constant.MAIN_URL, params)
    }

    private fun walletHistory() {
        AppController.showShimmer(shimmerFrameLayout,recyclerView)
            walletHistories = ArrayList()
            val linearLayoutManager = LinearLayoutManager(activity)
            recyclerView.layoutManager = linearLayoutManager
            val params: MutableMap<String, String?> = HashMap()
            params[Constant.TYPE_ID] = session.getData(Constant.ID)
            params[Constant.GET_WITHDRAWAL_REQUEST] = Constant.GetVal
            params[Constant.OFFSET] = offset.toString()
            params[Constant.TYPE] = Constant.DELIVERY_BOY
            params[Constant.LIMIT] = Constant.PRODUCT_LOAD_LIMIT
            params[Constant.DATA_TYPE] = dataType
            ApiConfig.requestToVolley({ result, response ->
                if (result) {
                    try {
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
                                    val notification = g.fromJson(
                                        jsonObject2.toString(),
                                        WalletHistory::class.java
                                    )
                                    walletHistories.add(notification)
                                } else {
                                    break
                                }
                            }
                            if (offset == 0) {
                                walletHistoryAdapter = WalletHistoryAdapter(activity, walletHistories)
                                walletHistoryAdapter.setHasStableIds(true)
                                recyclerView.adapter = walletHistoryAdapter
                                AppController.hideShimmer(shimmerFrameLayout,recyclerView)
                                scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                                    if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                                        val linearLayoutManager1 =
                                            recyclerView.layoutManager as LinearLayoutManager?
                                        if (walletHistories.size < total) {
                                            if (!isLoadMore) {
                                                if (linearLayoutManager1 != null && linearLayoutManager1.findLastCompletelyVisibleItemPosition() == walletHistories.size - 1) {
                                                    walletHistories.add(null)
                                                    walletHistoryAdapter.notifyItemInserted(
                                                        walletHistories.size - 1
                                                    )
                                                    offset += Constant.LOAD_ITEM_LIMIT
                                                    val params1: MutableMap<String, String?> =
                                                        HashMap()
                                                    params1[Constant.TYPE_ID] = session.getData(
                                                        Constant.ID
                                                    )
                                                    params1[Constant.GET_WITHDRAWAL_REQUEST] =
                                                        Constant.GetVal
                                                    params1[Constant.OFFSET] = offset.toString()
                                                    params1[Constant.TYPE] = Constant.DELIVERY_BOY
                                                    params1[Constant.LIMIT] =
                                                        Constant.PRODUCT_LOAD_LIMIT
                                                    params1[Constant.DATA_TYPE] = dataType
                                                    ApiConfig.requestToVolley({ result, response ->
                                                        if (result) {
                                                            try {
                                                                val jsonObject3 =
                                                                    JSONObject(response.toString())
                                                                if (!jsonObject3.getBoolean(
                                                                        Constant.ERROR
                                                                    )
                                                                ) {
                                                                    session.setData(
                                                                        Constant.TOTAL,
                                                                        jsonObject1.getString(
                                                                            Constant.TOTAL
                                                                        )
                                                                    )
                                                                    walletHistories.removeAt(
                                                                        walletHistories.size - 1
                                                                    )
                                                                    walletHistoryAdapter.notifyItemRemoved(
                                                                        walletHistories.size
                                                                    )
                                                                    val jsonObject4 =
                                                                        JSONObject(response.toString())
                                                                    val jsonArray2 =
                                                                        jsonObject4.getJSONArray(
                                                                            Constant.DATA
                                                                        )
                                                                    for (i in 0 until jsonArray2.length()) {
                                                                        val jsonObject5 =
                                                                            jsonArray2.getJSONObject(
                                                                                i
                                                                            )
                                                                        if (jsonObject5 != null) {
                                                                            val notification =
                                                                                g.fromJson(
                                                                                    jsonObject5.toString(),
                                                                                    WalletHistory::class.java
                                                                                )
                                                                            walletHistories.add(
                                                                                notification
                                                                            )
                                                                        } else {
                                                                            break
                                                                        }
                                                                    }
                                                                    walletHistoryAdapter.notifyDataSetChanged()
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
                        e.printStackTrace()
                        AppController.hideShimmer(shimmerFrameLayout,recyclerView)
                    }
                }
            }, activity, Constant.MAIN_URL, params)
        }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.toolbar_filter) {
            val builder = AlertDialog.Builder(
                activity
            )
            builder.setTitle(activity.resources.getString(R.string.filter_by))
            builder.setSingleChoiceItems(Constant.filtervalues, filterIndex) { dialog, item ->
                filterIndex = item
                when (item) {
                    0 -> dataType = Constant.FUND_TRANSFERS
                    1 -> dataType = Constant.WITHDRAWAL_REQUEST
                }
                walletHistory()
                dialog.dismiss()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}