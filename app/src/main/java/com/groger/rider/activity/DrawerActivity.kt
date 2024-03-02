package com.groger.rider.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_drawer.*
import com.groger.rider.R
import com.groger.rider.helper.Constant
import com.groger.rider.helper.Session

open class DrawerActivity : AppCompatActivity() {
    lateinit var drawerToggle: ActionBarDrawerToggle
    lateinit var session: Session
    lateinit var activity: Activity

    private var offset: Int = 0

    private lateinit var navigationView: NavigationView
    private lateinit var headerView: View


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_drawer)

        activity = this@DrawerActivity
        session = Session(activity)

        navigationView = findViewById(R.id.nav_view)
        headerView = navigationView.getHeaderView(0)
        tvName = headerView.findViewById(R.id.tvName)
        tvMobile = headerView.findViewById(R.id.tvMobile)
        tvWallet = headerView.findViewById(R.id.tvWallet)
        lytWallet = headerView.findViewById(R.id.lytWallet)
        lytProfile = headerView.findViewById(R.id.lytProfile)

        if (session.isUserLoggedIn) {
            tvName.text = session.getData(Session.KEY_NAME)
            tvMobile.text = session.getData(Session.KEY_MOBILE)
            lytWallet.visibility = View.VISIBLE
            tvWallet.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wallet_white, 0, 0, 0)
            tvWallet.text = getString(R.string.wallet_balance,Session(activity).getData(Constant.CURRENCY),session.getData(Constant.BALANCE))
        } else {
            lytWallet.visibility = View.GONE
            tvName.text = resources.getString(R.string.is_login)
        }
        lytProfile.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext, ProfileActivity::class.java
                )
            )
        }
        setupNavigationDrawer()
    }

    private fun setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawer_layout.closeDrawers()
            when (menuItem.itemId) {
                R.id.menu_home -> startActivity(
                    Intent(
                        applicationContext,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                R.id.menu_notifications -> {
                    offset=0
                    startActivity(Intent(applicationContext, NotificationListActivity::class.java))
                }
                R.id.menu_profile -> startActivity(
                    Intent(
                        applicationContext,
                        ProfileActivity::class.java
                    )
                )
                R.id.menu_wallet_history -> {
                    offset=0
                    startActivity(Intent(applicationContext, WalletHistoryActivity::class.java))
                }
                R.id.menu_policy -> {
                    val policy = Intent(this@DrawerActivity, WebViewActivity::class.java)
                    policy.putExtra("title", getString(R.string.privacy_policy))
                    policy.putExtra("link", Constant.DELIVERY_BOY_POLICY)
                    startActivity(policy)
                }
                R.id.menu_terms -> {
                    val terms = Intent(this@DrawerActivity, WebViewActivity::class.java)
                    terms.putExtra("title", getString(R.string.terms_conditions))
                    terms.putExtra("link", Constant.DELIVERY_BOY_TERMS)
                    startActivity(terms)
                }
                R.id.menu_logout -> session.logoutUserConfirmation(activity)
            }
            true
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        //Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var tvName: TextView
        @SuppressLint("StaticFieldLeak")
        lateinit var tvMobile: TextView
        @SuppressLint("StaticFieldLeak")
        lateinit var tvWallet: TextView
        @SuppressLint("StaticFieldLeak")
        lateinit var lytWallet: LinearLayout
        @SuppressLint("StaticFieldLeak")
        lateinit var lytProfile: LinearLayout
    }
}