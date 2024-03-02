package com.groger.rider.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import com.groger.rider.R
import com.groger.rider.activity.LoginActivity

class Session(private var _context: Context) {
    private var pref: SharedPreferences
    private var editor: SharedPreferences.Editor
    private var privateMode = 0

    fun createUserLoginSession(
        fcmId: String,
        id: String,
        name: String,
        mobile: String,
        password: String,
        address: String,
        bonus: String,
        balance: String,
        status: String,
        is_available: String,
        created_at: String
    ) {
        editor.putBoolean(IS_USER_LOGIN, true)
        editor.putString(KEY_FCM_ID, fcmId)
        editor.putString(KEY_ID, id)
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_MOBILE, mobile)
        editor.putString(KEY_PASSWORD, password)
        editor.putString(KEY_ADDRESS, address)
        editor.putString(KEY_BONUS, bonus)
        editor.putString(KEY_BALANCE, balance)
        editor.putString(KEY_STATUS, status)
        editor.putString(KEY_IS_AVAILABLE, is_available)
        editor.putString(KEY_CREATED_AT, created_at)
        editor.commit()
    }

    fun getData(id: String): String? {
        return pref.getString(id, "")
    }

    fun setData(id: String, `val`: String) {
        editor.putString(id, `val`)
        editor.commit()
    }

    val isUserLoggedIn: Boolean
        get() = pref.getBoolean(IS_USER_LOGIN, false)
    
    fun logoutUserConfirmation(activity: Activity) {
        val alertDialog = AlertDialog.Builder(_context)
        // Setting Dialog Message
        alertDialog.setTitle(R.string.logout)
        alertDialog.setMessage(R.string.logout_msg)
        alertDialog.setCancelable(false)
        alertDialog.setIcon(R.drawable.ic_logout_dialog)
        val alertDialog1 = alertDialog.create()

        // Setting OK Button
        alertDialog.setPositiveButton(R.string.yes) { _, _ ->
            editor.clear()
            editor.commit()
            val i = Intent(activity, LoginActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(i)
            activity.finish()
        }
        alertDialog.setNegativeButton(R.string.no) { _, _ -> alertDialog1.dismiss() }
        // Showing Alert Message
        alertDialog.show()
    }

    companion object {
        const val PREFER_NAME = "EKart_dboy"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_MOBILE = "mobile"
        const val KEY_PASSWORD = "password"
        const val KEY_ADDRESS = "address"
        const val KEY_BONUS = "bonus"
        const val KEY_BALANCE = "balance"
        const val KEY_STATUS = "status"
        const val KEY_IS_AVAILABLE = "is_available"
        const val KEY_CREATED_AT = "date_created"
        const val KEY_FCM_ID = "fcm_id"
        const val IS_USER_LOGIN = "IsUserLoggedIn"
    }

    init {
        pref = _context.getSharedPreferences(PREFER_NAME, privateMode)
        editor = pref.edit()
    }
}