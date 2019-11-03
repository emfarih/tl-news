package com.app.tlnewsapp.activities

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.app.tlnewsapp.R
import com.app.tlnewsapp.Config
import com.app.tlnewsapp.models.News
import com.app.tlnewsapp.realm.RealmController
import com.app.tlnewsapp.utils.AppBarLayoutBehavior
import com.app.tlnewsapp.utils.Constant
import com.app.tlnewsapp.utils.GDPR
import com.app.tlnewsapp.utils.Tools
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.squareup.picasso.Picasso

class ActivityNewsDetail : AppCompatActivity() {

    private var post: News? = null
    private var flag_read_later: Boolean = false
    private lateinit var parent_view: View
    private var menu: Menu? = null
    private lateinit var txt_title: TextView
    private lateinit var txt_category: TextView
    private lateinit var txt_date: TextView
    private lateinit var txt_comment_count: TextView
    private lateinit var txt_comment_text: TextView
    private lateinit var btn_comment: ImageView
    private lateinit var img_thumb_video: ImageView
    private var webview: WebView? = null
    private var adView: AdView? = null
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)
        (appBarLayout.layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayoutBehavior()

        parent_view = findViewById(android.R.id.content)
        webview = findViewById(R.id.news_description)

        txt_title = findViewById(R.id.title)
        txt_category = findViewById(R.id.category)
        txt_date = findViewById(R.id.date)
        txt_comment_count = findViewById(R.id.txt_comment_count)
        txt_comment_text = findViewById(R.id.txt_comment_text)
        btn_comment = findViewById(R.id.btn_comment)
        img_thumb_video = findViewById(R.id.thumbnail_video)

        btn_comment.setOnClickListener {
            val intent = Intent(applicationContext, ActivityComments::class.java)
            intent.putExtra("nid", post!!.nid)
            intent.putExtra("count", post!!.comments_count)
            startActivity(intent)
        }

        txt_comment_text.setOnClickListener {
            val intent = Intent(applicationContext, ActivityComments::class.java)
            intent.putExtra("nid", post!!.nid)
            intent.putExtra("count", post!!.comments_count)
            startActivity(intent)
        }

        // animation transition
        ViewCompat.setTransitionName(findViewById(R.id.image), EXTRA_OBJC)

        // get extra object
        post = intent.getSerializableExtra(EXTRA_OBJC) as News

        initToolbar()

        displayData()

        loadBannerAd()
        loadInterstitialAd()

    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.title = ""
        }
    }

    private fun displayData() {
        txt_title.text = Html.fromHtml(post!!.news_title)
        txt_comment_count.text = "" + post!!.comments_count

        Handler().postDelayed({
            if (post!!.comments_count == 0L) {
                txt_comment_text.setText(R.string.txt_no_comment)
            }
            if (post!!.comments_count == 1L) {
                txt_comment_text.text = resources.getString(R.string.txt_read) + " " + post!!.comments_count + " " + resources.getString(R.string.txt_comment)
            } else if (post!!.comments_count > 1) {
                txt_comment_text.text = resources.getString(R.string.txt_read) + " " + post!!.comments_count + " " + resources.getString(R.string.txt_comments)
            }
        }, 1000)

        webview!!.setBackgroundColor(Color.parseColor("#ffffff"))
        webview!!.isFocusableInTouchMode = false
        webview!!.isFocusable = false
        webview!!.settings.defaultTextEncodingName = "UTF-8"
        webview!!.settings.javaScriptEnabled = true

        val webSettings = webview!!.settings
        val res = resources
        val fontSize = res.getInteger(R.integer.font_size)
        webSettings.defaultFontSize = fontSize

        val mimeType = "text/html; charset=UTF-8"
        val encoding = "utf-8"
        val htmlText = post!!.news_description

        val text = ("<html><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + "<style type=\"text/css\">body{color: #000000;}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>")

        webview!!.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Toast.makeText(applicationContext, url, Toast.LENGTH_SHORT).show()
                if (url.startsWith("http://")) {
                    val intent = Intent(applicationContext, ActivityWebView::class.java)
                    intent.putExtra("url", url)
                    Toast.makeText(applicationContext, "HTTP", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                }
                if (url.startsWith("https://")) {
                    val intent = Intent(applicationContext, ActivityWebView::class.java)
                    intent.putExtra("url", url)
                    Toast.makeText(applicationContext, "HTTPS", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                }
                if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                    val intent = Intent(applicationContext, ActivityWebViewImage::class.java)
                    intent.putExtra("image_url", url)
                    startActivity(intent)
                }
                if (url.endsWith(".pdf")) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }
                return true
            }
        }
        webview!!.loadDataWithBaseURL(null,text,"text/html", "utf-8", null)

//        webview!!.loadData(text, mimeType, null)

        txt_category.text = post!!.category_name
        txt_category.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCategory))

        txt_date.text = Tools.getFormatedDate(post!!.news_date)

        val news_image = findViewById<ImageView>(R.id.image)

        if (post!!.content_type != null && post!!.content_type == "youtube") {
            Picasso.with(this)
                    .load(Constant.YOUTUBE_IMG_FRONT + post!!.video_id + Constant.YOUTUBE_IMG_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image)

            news_image.setOnClickListener {
                val intent = Intent(applicationContext, ActivityYoutubePlayer::class.java)
                intent.putExtra("video_id", post!!.video_id)
                startActivity(intent)

                showInterstitialAd()
            }

        } else if (post!!.content_type != null && post!!.content_type == "Url") {

            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post!!.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image)

            news_image.setOnClickListener {
                val intent = Intent(applicationContext, ActivityVideoPlayer::class.java)
                intent.putExtra("video_url", post!!.video_url)
                startActivity(intent)

                showInterstitialAd()
            }
        } else if (post!!.content_type != null && post!!.content_type == "Upload") {

            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post!!.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image)

            news_image.setOnClickListener {
                val intent = Intent(applicationContext, ActivityVideoPlayer::class.java)
                intent.putExtra("video_url", Config.ADMIN_PANEL_URL + "/upload/video/" + post!!.video_url)
                startActivity(intent)

                showInterstitialAd()
            }
        } else {
            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post!!.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image)

            news_image.setOnClickListener {
                val intent = Intent(applicationContext, ActivityFullScreenImage::class.java)
                intent.putExtra("image", post!!.news_image)
                startActivity(intent)
            }
        }

        if (post!!.content_type != "Post") {
            img_thumb_video.visibility = View.VISIBLE
        } else {
            img_thumb_video.visibility = View.GONE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_news_detail, menu)
        this.menu = menu
        val read_later_menu = menu.findItem(R.id.action_later)
        refreshReadLaterMenu()
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> onBackPressed()

            R.id.action_later -> {

                if (post!!.isDraft) {
                    Snackbar.make(parent_view, R.string.cannot_add_to_favorite, Snackbar.LENGTH_SHORT).show()
                    return true
                }
                val str: String
                if (flag_read_later) {
                    RealmController.with(this).deleteNews(post!!.nid)
                    str = getString(R.string.favorite_removed)
                } else {
                    RealmController.with(this).saveNews(post!!)
                    str = getString(R.string.favorite_added)
                }
                Snackbar.make(parent_view, str, Snackbar.LENGTH_SHORT).show()
                refreshReadLaterMenu()
            }

            R.id.action_share -> {

                val formattedString = android.text.Html.fromHtml(post!!.news_description).toString()
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, post!!.news_title + "\n" + formattedString + "\n" + resources.getString(R.string.share_content) + "https://play.google.com/store/apps/details?id=" + packageName)
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            }

            else -> return super.onOptionsItemSelected(menuItem)
        }
        return true
    }

    private fun refreshReadLaterMenu() {
        flag_read_later = RealmController.with(this).getNews(post!!.nid) != null
        if (flag_read_later) {
            menu!!.getItem(0).icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_favorite_white)
        } else {
            menu!!.getItem(0).icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_favorite_outline_white)
        }
    }

    fun loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            adView = findViewById(R.id.adView)
            val adRequest = AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, GDPR.getBundleAd(this@ActivityNewsDetail)).build()
            adView!!.loadAd(adRequest)
            adView!!.adListener = object : AdListener() {

                override fun onAdClosed() {}

                override fun onAdFailedToLoad(error: Int) {
                    adView!!.visibility = View.GONE
                }

                override fun onAdLeftApplication() {}

                override fun onAdOpened() {}

                override fun onAdLoaded() {
                    adView!!.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_ON_CLICK_VIDEO) {
            interstitialAd = InterstitialAd(applicationContext)
            interstitialAd!!.adUnitId = resources.getString(R.string.admob_interstitial_unit_id)
            val adRequest = AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, GDPR.getBundleAd(this@ActivityNewsDetail)).build()
            interstitialAd!!.loadAd(adRequest)
            interstitialAd!!.adListener = object : AdListener() {
                override fun onAdClosed() {
                    interstitialAd!!.loadAd(AdRequest.Builder().build())
                }
            }
        }
    }

    private fun showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_ON_CLICK_VIDEO) {
            if (interstitialAd != null && interstitialAd!!.isLoaded) {
                interstitialAd!!.show()
            }
        }
    }

    companion object {

        val EXTRA_OBJC = "key.EXTRA_OBJC"

        // give preparation animation activity transition
        fun navigate(activity: AppCompatActivity, transitionView: View, obj: News) {
            val intent = Intent(activity, ActivityNewsDetail::class.java)
            intent.putExtra(EXTRA_OBJC, obj)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, EXTRA_OBJC)
            ActivityCompat.startActivity(activity, intent, options.toBundle())
        }
    }

}
