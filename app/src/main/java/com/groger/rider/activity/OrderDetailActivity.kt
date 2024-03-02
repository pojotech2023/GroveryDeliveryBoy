package com.groger.rider.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_order_detail.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import com.groger.rider.R
import com.groger.rider.adapter.ItemListAdapter
import com.groger.rider.helper.ApiConfig
import com.groger.rider.helper.AppController
import com.groger.rider.helper.Constant
import com.groger.rider.helper.Session
import com.groger.rider.model.Items
import com.groger.rider.model.OrderList
import java.util.*

class OrderDetailActivity : AppCompatActivity() {
    private lateinit var itemArrayList: ArrayList<Items>
    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var activity: Activity
    private lateinit var orderID: String
    private lateinit var latitude: String
    private lateinit var longitude: String
    private lateinit var session: Session
    private lateinit var otp: String
    private lateinit var updatedStatus: Array<String?>

    private var checkedItem = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        activity = this@OrderDetailActivity
        session = Session(activity)

        recyclerView.layoutManager = LinearLayoutManager(activity)

        orderID = intent.getStringExtra(Constant.ORDER_ID).toString()
        setSupportActionBar(toolbar)

        supportActionBar?.title = getString(R.string.product_detail) + orderID
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (AppController.isConnected(activity)) {
            AppController.showShimmer(shimmerFrameLayout, swipeRefresh)
            getOrderData(activity)
        } else {
            setSnackBar(
                activity,
                getString(R.string.no_internet_message),
                getString(R.string.retry),
                Color.RED
            )
        }
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        swipeRefresh.setOnRefreshListener {
            AppController.showShimmer(shimmerFrameLayout, swipeRefresh)
            if (AppController.isConnected(activity)) {
                getOrderData(activity)
                swipeRefresh.isRefreshing = false
                ApiConfig.disableSwipe(swipeRefresh)
            } else {
                setSnackBar(
                    activity,
                    getString(R.string.no_internet_message),
                    getString(R.string.retry),
                    Color.RED
                )
            }
            swipeRefresh.isRefreshing = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getOrderData(activity: Activity) =
        if (AppController.isConnected(activity)) {
            val params: MutableMap<String, String?> = HashMap()
            params[Constant.ID] = session.getData(Constant.ID)
            params[Constant.ORDER_ID] = orderID
            params[Constant.GET_ORDERS_BY_DELIVERY_BOY_ID] = Constant.GetVal
            ApiConfig.requestToVolley({ result, response ->
                if (result) {
                    try {
                        val jsonObject = JSONObject(response.toString())
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            val jsonArray = jsonObject.getJSONArray(Constant.DATA)
                            val jsonObject1 = jsonArray.getJSONObject(0)
                            tvDate.text =
                                getString(
                                    R.string.order_on,
                                    jsonObject1.getString(Constant.DATE_ADDED)
                                )
                            otp = jsonObject1.getString(Constant.OTP)
                            tvName.text =
                                getString(R.string._name, jsonObject1.getString(Constant.NAME))
                            tvPhone.text = jsonObject1.getString(Constant.MOBILE)
                            tvAddress.text =
                                getString(R.string.at, jsonObject1.getString(Constant.ADDRESS))
                            btnDeliveryStatus.text =
                                AppController.toTitleCase(jsonObject1.getString(Constant.ACTIVE_STATUS))
                            tvDeliveryTime.text = getString(
                                R.string.delivery_by,
                                jsonObject1.getString(Constant.DELIVERY_TIME)
                            )
                            tvDeliveryCharge.text =
                                (Session(activity).getData(Constant.CURRENCY) + jsonObject1.getString(
                                    Constant.DELIVERY_CHARGE
                                ))
                            if (jsonObject1.getString(Constant.LATITUDE) == "0" && jsonObject1.getString(
                                    Constant.LONGITUDE
                                ) == "0"
                            ) {
                                btnGetDirection.visibility = View.GONE
                            } else {
                                latitude = jsonObject1.getString(Constant.LATITUDE)
                                longitude = jsonObject1.getString(Constant.LONGITUDE)
                            }
                            tvItemTotal.text =
                                (Session(activity).getData(Constant.CURRENCY) + jsonObject1.getString(
                                    Constant.TOTAL
                                ))
                            tvPCAmount.text =
                                (Session(activity).getData(Constant.CURRENCY) + jsonObject1.getString(
                                    Constant.PROMO_DISCOUNT
                                ))
                            tvDiscountAmount.text =
                                (Session(activity).getData(Constant.CURRENCY) + jsonObject1.getString(
                                    Constant.DISCOUNT
                                ))
                            tvWallet.text =
                                (Session(activity).getData(Constant.CURRENCY) + jsonObject1.getString(
                                    Constant.STR_WALLET_BALANCE
                                ))
                            tvFinalTotal.text =
                                (Session(activity).getData(Constant.CURRENCY) + jsonObject1.getString(
                                    Constant.FINAL_TOTAL
                                ))
                            tvPaymentMethod.text = (getString(
                                R.string.via,
                                jsonObject1.getString(Constant.PAYMENT_METHOD)
                                    .uppercase(Locale.getDefault())
                            ))
                            tvTaxAmt.text = jsonObject1.getString(Constant.TAX)
                            //
                            itemArrayList = ArrayList()
                            val jsonArrayItems = JSONArray(jsonObject1.getString(Constant.ITEMS))
                            val g = Gson()
                            for (i in 0 until jsonArrayItems.length()) {
                                val itemsObject = jsonArrayItems.getJSONObject(i)
                                val items = g.fromJson(itemsObject.toString(), Items::class.java)
                                itemArrayList.add(items)
                            }
                            itemListAdapter = ItemListAdapter(activity, itemArrayList)
                            recyclerView.adapter = itemListAdapter
                            lyt_order_detail.visibility = View.VISIBLE

                        } else {
                            setSnackBar(
                                activity,
                                jsonObject.getString(Constant.MESSAGE),
                                getString(R.string.ok),
                                Color.RED
                            )
                        }
                    } catch (e: JSONException) {
                        AppController.hideShimmer(shimmerFrameLayout, swipeRefresh)
                        e.printStackTrace()
                    }
                }
            }, activity, Constant.MAIN_URL, params)
            AppController.hideShimmer(shimmerFrameLayout, swipeRefresh)
        } else {
            AppController.hideShimmer(shimmerFrameLayout, swipeRefresh)
            setSnackBar(
                activity,
                getString(R.string.no_internet_message),
                getString(R.string.retry),
                Color.RED
            )
        }

    private fun confirmOtp() {
        val alertDialog = AlertDialog.Builder(this@OrderDetailActivity)
        val inflater =
            this@OrderDetailActivity.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_otp_confirm_request, null)
        alertDialog.setView(dialogView)
        alertDialog.setCancelable(true)
        val dialog = alertDialog.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogConfirm = dialogView.findViewById<TextView>(R.id.tvDialogConfirm)
        val tvDialogCancel = dialogView.findViewById<TextView>(R.id.tvDialogCancel)
        val edtOTP = dialogView.findViewById<EditText>(R.id.edtOTP)
        tvDialogConfirm.setOnClickListener {
            if (edtOTP.text.toString()
                    .isNotEmpty() || edtOTP.text.toString() == "0" || edtOTP.text.toString().length >= 6
            ) {
                if (checkedItem <= 3) {
                    if (checkedItem == 3) {
                        if (edtOTP.text.toString() == otp) {
                            changeOrderStatus(activity, updatedStatus[0]?.lowercase(Locale.ROOT))
                            dialog.dismiss()
                        } else {
                            Toast.makeText(
                                activity,
                                getString(R.string.otp_not_matched),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.can_not_update_order),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                edtOTP.error = getString(R.string.alert_otp)
            }
        }
        tvDialogCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun onButtonClick(view: View) {
        if (AppController.isConnected(activity)) {
            when (view.id) {
                R.id.btnCallCustomer -> {
                    try {
                        val callIntent = Intent(Intent.ACTION_CALL)
                        if (ContextCompat.checkSelfPermission(
                                this@OrderDetailActivity,
                                Manifest.permission.CALL_PHONE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this@OrderDetailActivity, arrayOf(
                                    Manifest.permission.CALL_PHONE
                                ), 1
                            )
                        } else {
                            callIntent.data =
                                Uri.parse("tel:" + tvPhone.text.toString().trim { it <= ' ' })
                            startActivity(callIntent)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                R.id.btnGetDirection -> {
                    val builder1 = android.app.AlertDialog.Builder(activity)
                    builder1.setMessage(R.string.map_open_message)
                    builder1.setCancelable(true)
                    builder1.setPositiveButton(
                        getString(R.string.yes)
                    ) { _, _ -> //                                com.google.android.apps.maps
                        try {
                            val googleMapIntentUri =
                                Uri.parse("google.navigation:q=$latitude,$longitude")
                            val mapIntent = Intent(Intent.ACTION_VIEW, googleMapIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            activity.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val builder2 = android.app.AlertDialog.Builder(activity)
                            builder2.setMessage("Please install google map first.")
                            builder2.setCancelable(true)
                            builder2.setPositiveButton(
                                getString(R.string.ok)
                            ) { dialog, _ -> dialog.cancel() }
                            val alert2 = builder2.create()
                            alert2.show()
                        }
                    }
                    builder1.setNegativeButton(
                        getString(R.string.no)
                    ) { dialog, _ -> dialog.cancel() }
                    val alert11 = builder1.create()
                    alert11.show()
                }
                R.id.btnDeliveryStatus -> {
                    // setup the alert builder
                    updatedStatus = arrayOfNulls(1)
                    val builder = android.app.AlertDialog.Builder(activity)
                    builder.setTitle(R.string.update_status) // add a radio button list
                    val status = arrayOf(
                        Constant.RECEIVED,
                        Constant.PROCESSED,
                        Constant.SHIPPED,
                        Constant.DELIVERED,
                        Constant.CANCELLED,
                        Constant.RETURNED
                    )
                    when (btnDeliveryStatus.text.toString()) {
                        Constant.RECEIVED -> checkedItem = 0
                        Constant.PROCESSED -> checkedItem = 1
                        Constant.SHIPPED -> checkedItem = 2
                        Constant.DELIVERED -> checkedItem = 3
                        Constant.CANCELLED -> checkedItem = 4
                        Constant.RETURNED -> checkedItem = 5
                    }
                    builder.setSingleChoiceItems(status, checkedItem) { _, which ->
                        checkedItem = which
                        updatedStatus[0] = status[which]
                    }
                    builder.setPositiveButton(R.string.ok) { _, _ ->
                        Constant.CLICK = true
                        val alertDialog = AlertDialog.Builder(
                            activity
                        )
                        // Setting Dialog Message
                        alertDialog.setMessage(R.string.change_order_status_msg)
                        alertDialog.setCancelable(false)
                        val alertDialog1 = alertDialog.create()

                        // Setting OK Button
                        alertDialog.setPositiveButton(R.string.yes) { _, _ ->
                            if (otp == "0") {
                                changeOrderStatus(
                                    activity, updatedStatus[0]?.lowercase(Locale.ROOT)
                                )
                            } else {
                                if (checkedItem == 3) {
                                    confirmOtp()
                                } else {
                                    changeOrderStatus(
                                        activity, updatedStatus[0]?.lowercase(Locale.ROOT)
                                    )
                                }
                            }
                            btnDeliveryStatus.text = AppController.toTitleCase(updatedStatus[0])
                        }
                        alertDialog.setNegativeButton(R.string.no) { _, _ -> alertDialog1.dismiss() }
                        // Showing Alert Message
                        alertDialog.show()
                    }
                    builder.setNegativeButton(
                        R.string.cancel,
                        null
                    ) // create and show the alert dialog
                    val dialog = builder.create()
                    dialog.show()
                }
            }
        } else {
            setSnackBar(
                activity,
                getString(R.string.no_internet_message),
                getString(R.string.retry),
                Color.RED
            )
        }
    }

    private fun changeOrderStatus(activity: Activity, status: String?) {
        if (AppController.isConnected(activity)) {
            val params: MutableMap<String, String?> = HashMap()
            params[Constant.DELIVERY_BOY_ID] = session.getData(Constant.ID)
            params[Constant.ID] = orderID
            params[Constant.STATUS] = status
            params[Constant.UPDATE_ORDER_STATUS] = Constant.GetVal
            ApiConfig.requestToVolley({ result, response ->
                if (result) {
                    try {
                        val jsonObject = JSONObject(response.toString())
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            setSnackBar(
                                activity,
                                jsonObject.getString(Constant.MESSAGE),
                                getString(R.string.ok),
                                Color.GREEN
                            )
                            val category: OrderList? =
                                MainActivity.orderListArrayList[Constant.Position_Value]
                            category?.active_status = status
                            //orderList.getActive_status ()
                            Constant.CLICK = true
                        } else {
                            setSnackBar(
                                activity,
                                jsonObject.getString(Constant.MESSAGE),
                                getString(R.string.ok),
                                Color.RED
                            )
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }, activity, Constant.MAIN_URL, params)
        } else {
            setSnackBar(
                activity,
                getString(R.string.no_internet_message),
                getString(R.string.retry),
                Color.RED
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun setSnackBar(activity: Activity, message: String?, action: String?, color: Int) {
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            message.toString(),
            Snackbar.LENGTH_INDEFINITE
        )
        snackBar.setAction(action) {
            getOrderData(activity)
            snackBar.dismiss()
        }
        snackBar.setActionTextColor(color)
        val snackBarView = snackBar.view
        val textView = snackBarView.findViewById<TextView>(R.id.snackbar_text)
        textView.maxLines = 5
        snackBar.show()
    }
}