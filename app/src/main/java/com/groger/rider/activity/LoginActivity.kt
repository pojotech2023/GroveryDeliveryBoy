package com.groger.rider.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import com.groger.rider.helper.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.concurrent.TimeUnit
import com.groger.rider.R

class LoginActivity : AppCompatActivity() {

    private lateinit var session: Session
    private lateinit var activity: Activity

    private lateinit var mobile: String
    private lateinit var firebaseOtp: String
    private lateinit var auth: FirebaseAuth
    private lateinit var mCallback: OnVerificationStateChangedCallbacks
    private var otpFrom = ""
    private var forMultipleCountryUse: Boolean = false
    private var doubleBackToExitPressedOnce: Boolean = false
    private var resendOTP: Boolean = false
    private lateinit var animShow: Animation
    private lateinit var animHide: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        activity = this@LoginActivity
        session = Session(activity)

        animShow = AnimationUtils.loadAnimation(this, R.anim.view_show)
        animHide = AnimationUtils.loadAnimation(this, R.anim.view_hide)

        tvForgotPass.text = underlineSpannable(getString(R.string.forgot_text))

        edtLoginPassword.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_pass,
            0,
            R.drawable.ic_show,
            0
        )
        edtResetPass.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_pass,
            0,
            R.drawable.ic_show,
            0
        )
        edtResetCPass.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_pass,
            0,
            R.drawable.ic_show,
            0
        )

        Utils.setHideShowPassword(edtResetPass)
        Utils.setHideShowPassword(edtResetCPass)
        Utils.setHideShowPassword(edtLoginPassword)
        Utils.setHideShowPassword(edtResetPass)
        Utils.setHideShowPassword(edtResetCPass)

        lytResetPass.visibility = View.GONE
        lytLogin.visibility = View.VISIBLE
        lytVerify.visibility = View.GONE
        lytOtp.visibility = View.GONE
        lytWebView.visibility = View.GONE

        tvWelcome.text = (getString(R.string.welcome, getString(R.string.app_name)))

        edtCountryCodePicker.setCountryForNameCode("IN")
        forMultipleCountryUse = false

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            if (token != session.getData(Constant.FCM_ID)) {
                session.setData(Constant.FCM_ID, token)
            }
        }

        startFirebaseLogin()
        privacyPolicy()
    }

    private fun underlineSpannable(text: String): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(UnderlineSpan(), 0, text.length, 0)
        return spannableString
    }

    fun onButtonClick(view: View) {
        if (AppController.isConnected(activity)) {
            when (view.id) {
                R.id.btnLogin -> {
                    val email = edtLoginMobile.text.toString()
                    val password = edtLoginPassword.text.toString()
                    when {
                        edtLoginMobile.text.isEmpty() -> {
                            edtLoginMobile.error = getString(R.string.enter_valid_mobile_number)
                        }
                        edtLoginPassword.text.isEmpty() -> {
                            edtLoginPassword.error = getString(R.string.password_required)
                        }

                        AppController.isConnected(activity) -> {
                            startButtonLoading(btnLogin, btnLoginProgress)
                            val params: MutableMap<String, String?> = HashMap()
                            params[Constant.MOBILE] = email
                            params[Constant.PASSWORD] = password
                            params[Constant.LOGIN] = Constant.GetVal
                            params[Constant.FCM_ID] = "" + session.getData(Constant.FCM_ID)

                            ApiConfig.requestToVolley({ result, response ->
                                if (result) {
                                    try {
                                        val jsonObject = JSONObject(response.toString())
                                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                                            session.setData(
                                                Constant.CURRENCY,
                                                jsonObject.getString(Constant.CURRENCY)
                                            )
                                            startMainActivity(
                                                jsonObject.getJSONArray(Constant.DATA)
                                                    .getJSONObject(0), password
                                            )
                                            startActivity(
                                                Intent(
                                                    activity,
                                                    MainActivity::class.java
                                                )
                                            )
                                        } else {
                                            setSnackBar(
                                                activity,
                                                jsonObject.getString(Constant.MESSAGE),
                                                getString(R.string.ok),
                                                Color.RED
                                            )
                                        }
                                        stopButtonLoading(btnLogin, btnLoginProgress)
                                    } catch (e: JSONException) {
                                        stopButtonLoading(btnLogin, btnLoginProgress)
                                        e.printStackTrace()
                                    }
                                }
                            }, activity, Constant.MAIN_URL, params)
                        }
                    }
                }
                R.id.btnResetPass -> {
                    val password: String = edtResetPass.text.trim().toString()
                    val confirmPassword: String = edtResetCPass.text.trim().toString()
                    when {
                        password.isEmpty() -> {
                            edtResetPass.error = getString(R.string.new_password_required)
                        }
                        confirmPassword.isEmpty() -> {
                            edtResetCPass.error = getString(R.string.confirm_password_required)
                        }
                        (password != confirmPassword) -> {
                            edtResetCPass.error = getString(R.string.confirm_password_miss_match)
                        }
                        else -> {
                            startButtonLoading(btnResetPass, btnResetPassProgress)
                            val params: MutableMap<String, String?> = HashMap()
                            params[Constant.MOBILE] = session.getData(Constant.MOBILE)
                            params[Constant.PASSWORD] = edtResetCPass.text.toString()
                            params[Constant.DELIVERY_BOY_FORGOT_PASSWORD] = Constant.GetVal
                            ApiConfig.requestToVolley({ result, response ->
                                if (result) {
                                    try {
                                        val jsonObject = JSONObject(response.toString())
                                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                                            setSnackBar(
                                                activity,
                                                jsonObject.getString(Constant.MESSAGE),
                                                getString(R.string.login_now),
                                                Color.GREEN
                                            )
                                        } else {
                                            setSnackBar(
                                                activity,
                                                jsonObject.getString(Constant.MESSAGE),
                                                getString(R.string.ok),
                                                Color.RED
                                            )
                                        }
                                        stopButtonLoading(btnResetPass, btnResetPassProgress)
                                    } catch (e: JSONException) {
                                        stopButtonLoading(btnResetPass, btnResetPassProgress)
                                        e.printStackTrace()
                                    }
                                }
                            }, activity, Constant.MAIN_URL, params)
                        }
                    }
                }
                R.id.tvForgotPass -> {
                    lytVerify.visibility = View.VISIBLE
                    lytVerify.startAnimation(animShow)
                }
                R.id.btnVerify -> {
                    if (lytOtp.visibility == View.GONE) {
                        if (edtMobileVerify.text.isNotEmpty()) {
                            edtCountryCodePicker.setCcpClickable(false)
                            edtMobileVerify.isEnabled = false

                            startButtonLoading(btnVerify, btnVerifyProgress)
                            verifyDeliveryBoy(
                                activity,
                                edtMobileVerify.text.toString(),
                                edtCountryCodePicker.selectedCountryCode.toString()
                            )
                        } else {
                            Toast.makeText(
                                activity,
                                getString(R.string.enter_valid_mobile_number),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        otpVerification()
                    }
                }
                R.id.tvResend -> {
                    otpFrom = "resend"
                    sentRequest("+" + edtCountryCodePicker.selectedCountryCode.toString() + mobile)
                }
                R.id.imgVerifyClose -> {
                    lytVerify.startAnimation(animHide)
                    lytVerify.visibility = View.GONE
                    btnVerify.text = activity.getString(R.string.send_otp)
                    edtMobileVerify.text.clear()
                    pinView.clearValue()
                }
                R.id.imgResetPasswordClose -> {
                    lytResetPass.startAnimation(animHide)
                    lytResetPass.visibility = View.GONE
                    edtResetPass.text.clear()
                    edtResetCPass.text.clear()
                }
                R.id.imgWebViewClose -> {
                    lytWebView.startAnimation(animHide)
                    lytWebView.visibility = View.GONE
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

    private fun startButtonLoading(btn: View, loading: View) {
        btn.visibility = View.GONE
        loading.visibility = View.VISIBLE
    }

    private fun stopButtonLoading(btn: View, loading: View) {
        btn.visibility = View.VISIBLE
        loading.visibility = View.GONE
    }

    private fun verifyDeliveryBoy(activity: Activity, mobileNumber: String, countryCode: String) {
        if (AppController.isConnected(activity)) {
            val params: MutableMap<String, String?> = HashMap()
            params[Constant.CHECK_DELIVERY_BOY_BY_MOBILE] = Constant.GetVal
            params[Constant.MOBILE] = mobileNumber
            ApiConfig.requestToVolley({ result: Boolean, response: String? ->
                if (result) {
                    try {
                        val jsonObject = JSONObject(response.toString())
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            sentRequest("+$countryCode$mobileNumber")
                            session.setData(Constant.MOBILE, mobileNumber)
                        } else {
                            stopButtonLoading(btnVerify, btnVerifyProgress)
                            Toast.makeText(
                                applicationContext,
                                jsonObject.get(Constant.MESSAGE).toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        stopButtonLoading(btnVerify, btnVerifyProgress)
                        e.printStackTrace()
                    }
                }
            }, activity, Constant.MAIN_URL, params)
        } else {
            stopButtonLoading(btnVerify, btnVerifyProgress)
            setSnackBar(
                activity,
                getString(R.string.no_internet_message),
                getString(R.string.retry),
                Color.RED
            )
        }
    }

    private fun sentRequest(phoneNumber: String?) {
        val options = phoneNumber?.let {
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(it)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallback)          // OnVerificationStateChangedCallbacks
                .build()
        }
        if (options != null) {
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun otpVerification() {
        val otpText = pinView.value
        if (otpText.isNotEmpty()) {
            val credential = PhoneAuthProvider.getCredential(firebaseOtp, otpText)
            signInWithPhoneAuthCredential(credential)
        } else {
            stopButtonLoading(btnVerify, btnVerifyProgress)
            setSnackBar(activity, getString(R.string.enter_otp), getString(R.string.ok), Color.RED)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this@LoginActivity) { task ->
                if (task.isSuccessful) {
                    //verification successful we will start the profile activity
                    lytResetPass.visibility = View.VISIBLE
                    lytResetPass.startAnimation(animShow)

                } else {
                    edtCountryCodePicker.setCcpClickable(true)
                    edtMobileVerify.isEnabled = true

                    //verification unsuccessful.. display an error message
                    var message = getString(R.string.otp_error)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        message = getString(R.string.invalid_code_entered)
                    }
                    setSnackBar(activity, message, getString(R.string.ok), Color.RED)
                }
            }
    }

    private fun startFirebaseLogin() {
        auth = FirebaseAuth.getInstance()
        mCallback = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {}
            override fun onVerificationFailed(e: FirebaseException) {
                setSnackBar(activity, e.localizedMessage, getString(R.string.ok), Color.RED)
            }

            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                firebaseOtp = s
                btnVerify.text = activity.getString(R.string.verify)
                stopButtonLoading(btnVerify, btnVerifyProgress)
                lytOtp.visibility = View.VISIBLE
                lytOtp.startAnimation(animShow)

                object : CountDownTimer(120000, 1000) {
                    @SuppressLint("SetTextI18n")
                    override fun onTick(millisUntilFinished: Long) {
                        // Used for formatting digit to be in 2 digits only
                        val f: NumberFormat = DecimalFormat("00")
                        val min = millisUntilFinished / 60000 % 60
                        val sec = millisUntilFinished / 1000 % 60
                        tvTimer.text = f.format(min) + ":" + f.format(sec)
                    }

                    override fun onFinish() {
                        resendOTP = false
                        tvTimer.visibility = View.GONE
                        img.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimary))
                        tvResend.setTextColor(
                            ContextCompat.getColor(
                                activity,
                                R.color.colorPrimary
                            )
                        )
                        tvResend.setOnClickListener {
                            resendOTP = true
                            sentRequest(
                                "+" + edtCountryCodePicker.selectedCountryCode.toString() + mobile
                            )
                            object : CountDownTimer(120000, 1000) {
                                @SuppressLint("SetTextI18n")
                                override fun onTick(millisUntilFinished: Long) {
                                    tvTimer.visibility = View.VISIBLE
                                    img.setColorFilter(
                                        ContextCompat.getColor(
                                            activity,
                                            R.color.gray
                                        )
                                    )
                                    tvResend.setTextColor(
                                        ContextCompat.getColor(
                                            activity,
                                            R.color.gray
                                        )
                                    )

                                    // Used for formatting digit to be in 2 digits only
                                    val f: NumberFormat = DecimalFormat("00")
                                    val min = millisUntilFinished / 60000 % 60
                                    val sec = millisUntilFinished / 1000 % 60
                                    tvTimer.text = f.format(min) + ":" + f.format(sec)
                                }

                                override fun onFinish() {
                                    resendOTP = false
                                    tvTimer.visibility = View.GONE
                                    img.setColorFilter(
                                        ContextCompat.getColor(
                                            activity,
                                            R.color.colorPrimary
                                        )
                                    )
                                    tvResend.setTextColor(
                                        ContextCompat.getColor(
                                            activity,
                                            R.color.colorPrimary
                                        )
                                    )
                                    tvResend.setOnClickListener {
                                        resendOTP = true
                                        sentRequest(
                                            "+" + edtCountryCodePicker.selectedCountryCode.toString() + mobile
                                        )
                                    }
                                }
                            }.start()
                        }
                    }
                }.start()

                if (otpFrom == "resend") {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.otp_resend),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.otp_has_been_sent),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun startMainActivity(jsonObject: JSONObject, password: String) {
        try {
            Session(activity).createUserLoginSession(
                jsonObject.getString(Constant.FCM_ID),
                jsonObject.getString(Constant.ID),
                jsonObject.getString(Constant.NAME),
                jsonObject.getString(Constant.MOBILE),
                password,
                jsonObject.getString(Constant.ADDRESS),
                jsonObject.getString(Constant.BONUS),
                jsonObject.getString(Constant.BALANCE),
                jsonObject.getString(Constant.STATUS),
                jsonObject.getString(Constant.IS_AVAILABLE),
                jsonObject.getString(Constant.CREATED_AT)
            )
            session.setData(Constant.BALANCE, jsonObject.getString(Constant.BALANCE))
            session.setData(Constant.ID, jsonObject.getString(Constant.ID))
            val intent = Intent(activity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun setSnackBar(activity: Activity, message: String?, action: String?, color: Int) {
        val snackBar =
            message?.let {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    it, Snackbar.LENGTH_INDEFINITE
                )
            }
        snackBar?.setAction(action) {
            if (action.equals(getString(R.string.retry)) || action.equals(getString(R.string.login_now))) {
                startActivity(
                    Intent(activity, LoginActivity::class.java).putExtra(
                        Constant.FROM,
                        "login"
                    )
                )
            }
            snackBar.dismiss()
        }
        snackBar?.setActionTextColor(color)
        val snackBarView = snackBar?.view
        val textView = snackBarView?.findViewById<TextView>(R.id.snackbar_text)
        textView?.maxLines = 5
        snackBar?.show()
    }

    private fun privacyPolicy() {
        tvPrivacy.isClickable = true
        tvPrivacy.movementMethod = LinkMovementMethod.getInstance()
        val message = getString(R.string.msg_privacy_terms)
        val s2 = getString(R.string.terms_conditions)
        val s1: String = getString(R.string.privacy_policy)
        val wordToSpan: Spannable = SpannableString(message)
        wordToSpan.setSpan(object : ClickableSpan() {
            override fun onClick(view: View) {
                webView.loadUrl(Constant.DELIVERY_BOY_POLICY)
                try {
                    Thread.sleep(500)
                    lytWebView.visibility = View.VISIBLE
                    lytWebView.startAnimation(animShow)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(activity, R.color.colorPrimary)
                ds.isUnderlineText
            }
        }, message.indexOf(s1), message.indexOf(s1) + s1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        wordToSpan.setSpan(object : ClickableSpan() {
            override fun onClick(view: View) {
                webView.loadUrl(Constant.DELIVERY_BOY_TERMS)
                try {
                    Thread.sleep(500)
                    lytWebView.visibility = View.VISIBLE
                    lytWebView.startAnimation(animShow)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(activity, R.color.colorPrimary)
                ds.isUnderlineText
            }
        }, message.indexOf(s2), message.indexOf(s2) + s2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvPrivacy.text = wordToSpan
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            lytVerify.visibility == View.VISIBLE -> {
                lytVerify.startAnimation(animHide)
                lytVerify.visibility = View.GONE
                btnVerify.text = activity.getString(R.string.send_otp)
                edtMobileVerify.text.clear()
                pinView.clearValue()
            }
            lytResetPass.visibility == View.VISIBLE -> {
                lytResetPass.startAnimation(animHide)
                lytResetPass.visibility = View.GONE
                edtResetPass.text.clear()
                edtResetCPass.text.clear()
            }
            lytWebView.visibility == View.VISIBLE -> {
                lytWebView.startAnimation(animHide)
                lytWebView.visibility = View.GONE
            }
            else -> {
                if (doubleBackToExitPressedOnce) { super.onBackPressed() }
                this.doubleBackToExitPressedOnce = true
                Toast.makeText(this, getString(R.string.exit_msg), Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
        }
    }
}