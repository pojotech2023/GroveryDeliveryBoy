package com.groger.rider.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_profile.*
import org.json.JSONException
import org.json.JSONObject
import com.groger.rider.R
import com.groger.rider.helper.ApiConfig
import com.groger.rider.helper.AppController
import com.groger.rider.helper.Constant
import com.groger.rider.helper.Session

class ProfileActivity : AppCompatActivity() {
    lateinit var session: Session
    lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        activity = this@ProfileActivity
        session = Session(activity)
        setSupportActionBar(toolbar)
        supportActionBar?.title = resources.getString(R.string.profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        edtname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_profile, 0, 0, 0)
        tvMobile_.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        edtaddress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edt_home, 0, 0, 0)

        edtname.setText(session.getData(Constant.NAME))
        tvMobile_.text = session.getData(Constant.MOBILE)
        edtaddress.setText(session.getData(Constant.ADDRESS))
    }

    private fun updateUserData() {
        val params: MutableMap<String, String?> = HashMap()
        params[Constant.ID] = session.getData(Constant.ID)
        params[Constant.NAME] = edtname.text.toString().trim { it <= ' ' }
        params[Constant.ADDRESS] = edtaddress.text.toString().trim { it <= ' ' }
        params[Constant.UPDATE_DELIVERY_BOY_PROFILE] = Constant.GetVal
        ApiConfig.requestToVolley({ result, response ->
            if (result) {
                try {
                    val jsonObject = JSONObject(response.toString())
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        setSnackBar(
                            jsonObject.getString(Constant.MESSAGE),
                            getString(R.string.ok),
                            Color.GREEN
                        )
                        getDeliveryBoyData(activity)
                    } else {
                        setSnackBar(
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
    }

    fun onButtonClick(view: View) {
        if (AppController.isConnected(activity)) {
            when (view.id) {
                R.id.imglogout -> {
                    session.logoutUserConfirmation(activity)
                }
                R.id.tvChangePassword -> {
                    startActivity(
                        Intent(activity, LoginActivity::class.java).putExtra(
                            Constant.FROM,
                            "lyt_update_password"
                        )
                    )
                }
                R.id.btnsubmit -> {
                    val address: String = edtaddress.text.toString()
                    val name: String = edtname.text.toString()
                    when {
                        name.isEmpty() -> {
                            edtname.error = getString(R.string.name_required)
                        }
                        address.isEmpty() -> {
                            edtaddress.error = getString(R.string.address_required)
                        }
                        else -> {
                            updateUserData()
                        }
                    }
                }
            }
        } else {
            setSnackBar(
                getString(R.string.no_internet_message),
                getString(R.string.retry),
                Color.RED
            )
        }
    }

    private fun getDeliveryBoyData(activity: Activity) {
        if (AppController.isConnected(activity)) {
            val params: MutableMap<String, String?> = HashMap()
            params[Constant.ID] = session.getData(Constant.ID)
            params[Constant.GET_DELIVERY_BOY_BY_ID] = Constant.GetVal
            ApiConfig.requestToVolley({ result, response -> //  System.out.println("============" + response);
                if (result) {
                    try {
                        val jsonObject = JSONObject(response.toString())
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            startMainActivity(
                                activity,
                                jsonObject.getJSONArray(Constant.DATA).getJSONObject(0)
                            )
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }, activity, Constant.MAIN_URL, params)
        } else {
            setSnackBar(
                getString(R.string.no_internet_message),
                getString(R.string.retry),
                Color.RED
            )
        }
    }

    @SuppressLint("SetTextI18n")
    fun startMainActivity(activity: Activity, jsonObject: JSONObject) {
        if (AppController.isConnected(activity)) {
            try {
                Session(activity).createUserLoginSession(
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
                edtname.setText(session.getData(Constant.NAME))
//                tvName.text = session.getData(Constant.NAME)
                tvMobile_.text = session.getData(Constant.MOBILE)
                edtaddress.setText(session.getData(Constant.ADDRESS))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            setSnackBar(
                getString(R.string.no_internet_message),
                getString(R.string.retry),
                Color.RED
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setSnackBar(message: String?, action: String?, color: Int) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message.toString(), Snackbar.LENGTH_INDEFINITE)
        snackBar.setAction(action) { snackBar.dismiss() }
        snackBar.setActionTextColor(color)
        val snackBarView = snackBar.view
        val textView = snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = 5
        snackBar.show()
    }
}