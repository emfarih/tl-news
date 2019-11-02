package com.app.tlnewsapp.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.app.tlnewsapp.R
import com.app.tlnewsapp.Config
import com.app.tlnewsapp.fragment.FragmentCategory
import com.app.tlnewsapp.fragment.FragmentFavorite
import com.app.tlnewsapp.fragment.FragmentProfile
import com.app.tlnewsapp.fragment.FragmentRecent
import com.app.tlnewsapp.fragment.FragmentVideo
import com.app.tlnewsapp.utils.AppBarLayoutBehavior
import com.app.tlnewsapp.utils.Constant
import com.app.tlnewsapp.utils.GDPR
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso

import java.util.Objects

class MainActivity : AppCompatActivity() {

    private var exitTime: Long = 0
    private var myApplication: MyApplication? = null
    private lateinit var view: View
    private var navigation: BottomNavigationView? = null
    private var toolbar: Toolbar? = null
    private var prevMenuItem: MenuItem? = null
    private var pager_number = 5
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        view = findViewById(android.R.id.content)

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
            }
        }

        val appBarLayout = findViewById<AppBarLayout>(R.id.tab_appbar_layout)
        (appBarLayout.layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayoutBehavior()

        myApplication = MyApplication.instance

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar!!.setTitle(R.string.app_name)

        viewPager = findViewById(R.id.viewpager)
        viewPager.adapter = MyAdapter(supportFragmentManager)
        viewPager.offscreenPageLimit = pager_number

        navigation = findViewById(R.id.navigation)
        navigation!!.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    viewPager.currentItem = 0
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_category -> {
                    viewPager.currentItem = 1
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_video -> {
                    viewPager.currentItem = 2
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_favorite -> {
                    viewPager.currentItem = 3
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_profile -> {
                    viewPager.currentItem = 4
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem!!.isChecked = false
                } else {
                    navigation!!.menu.getItem(0).isChecked = false
                }
                navigation!!.menu.getItem(position).isChecked = true
                prevMenuItem = navigation!!.menu.getItem(position)

                if (viewPager.currentItem == 1) {
                    toolbar!!.title = resources.getString(R.string.title_nav_category)
                } else if (viewPager.currentItem == 2) {
                    toolbar!!.title = resources.getString(R.string.title_nav_video)
                } else if (viewPager.currentItem == 3) {
                    toolbar!!.title = resources.getString(R.string.title_nav_favorite)
                } else if (viewPager.currentItem == 4) {
                    toolbar!!.title = resources.getString(R.string.title_nav_profile)
                } else {
                    toolbar!!.setTitle(R.string.app_name)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        if (Config.ENABLE_RTL_MODE) {
            viewPager.rotationY = 180f
        }

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                // checking for type intent filter
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (intent.action == Constant.REGISTRATION_COMPLETE) {
                        // now subscribe to global topic to receive app wide notifications
                        FirebaseMessaging.getInstance().subscribeToTopic(Constant.TOPIC_GLOBAL)

                    } else if (intent.action == Constant.PUSH_NOTIFICATION) {
                        // new push notification is received
                        val message = intent.getStringExtra("message")
                        Toast.makeText(applicationContext, "Push notification: " + message!!, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        val intent = intent
        val message = intent.getStringExtra("message")
        val imageUrl = intent.getStringExtra("image")
        val nid = intent.getLongExtra("id", 0)
        val link = intent.getStringExtra("link")

        if (message != null) {

            val layoutInflaterAndroid = LayoutInflater.from(this@MainActivity)
            @SuppressLint("InflateParams") val mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_notif, null)

            val alert = AlertDialog.Builder(this@MainActivity)
            alert.setView(mView)

            val notification_title = mView.findViewById<TextView>(R.id.news_title)
            val notification_message = mView.findViewById<TextView>(R.id.news_message)
            val notification_image = mView.findViewById<ImageView>(R.id.news_image)

            if (imageUrl != null) {
                if (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg") || imageUrl.endsWith(".png") || imageUrl.endsWith(".gif")) {
                    notification_title.text = message
                    notification_message.visibility = View.GONE
                    Picasso.with(this@MainActivity)
                            .load(imageUrl.replace(" ", "%20"))
                            .placeholder(R.drawable.ic_thumbnail)
                            .resize(200, 200)
                            .centerCrop()
                            .into(notification_image)

                    alert.setPositiveButton(R.string.dialog_read_more) { dialog, which ->
                        val intent = Intent(applicationContext, ActivityNotificationDetail::class.java)
                        intent.putExtra("id", nid)
                        startActivity(intent)
                    }
                    alert.setNegativeButton(R.string.dialog_dismiss, null)

                } else {
                    notification_title.text = resources.getString(R.string.app_name)

                    notification_message.visibility = View.VISIBLE
                    notification_message.text = message

                    notification_image.visibility = View.GONE

                    //Toast.makeText(getApplicationContext(), "link : " + link, Toast.LENGTH_SHORT).show();

                    if (link != null) {
                        if (link != "") {
                            alert.setPositiveButton("Continue") { dialog, which ->
                                val open = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                startActivity(open)
                            }
                            alert.setNegativeButton(R.string.dialog_dismiss, null)
                        } else {
                            alert.setPositiveButton(R.string.dialog_ok, null)
                        }
                    }
                }
            }

            alert.setCancelable(false)
            alert.show()

        }

        GDPR.updateConsentStatus(this)

    }

    inner class MyAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {

            return when (position) {
                0 -> FragmentRecent()
                1 -> FragmentCategory()
                2 -> FragmentVideo()
                3 -> FragmentFavorite()
                4 -> FragmentProfile()
                else -> FragmentRecent()
            }
        }

        override fun getCount(): Int {
            return pager_number
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.search) {
            val intent = Intent(applicationContext, ActivitySearch::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }


    override fun onBackPressed() {
        if (viewPager.currentItem != 0) {
            viewPager.setCurrentItem(0, true)
        } else {
            exitApp()
        }
    }

    fun exitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
            exitTime = System.currentTimeMillis()
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    companion object {
        lateinit var viewPager: ViewPager
    }

}
