package com.groger.rider.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.groger.rider.R
import com.groger.rider.helper.ApiConfig
import com.groger.rider.helper.Constant
import com.groger.rider.helper.Session

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)
        val session = Session(applicationContext)

        ApiConfig.setAppLocal(this, Constant.LANGUAGE)

        val splashTimeOut = 2000
        Handler().postDelayed({
            if (session.isUserLoggedIn) {
                session.setData(Constant.OFFSET, "" + 0)
                startActivity(
                    Intent(
                        this@SplashActivity,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } else {
                startActivity(
                    Intent(
                        this@SplashActivity,
                        LoginActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }, splashTimeOut.toLong())
    }
}