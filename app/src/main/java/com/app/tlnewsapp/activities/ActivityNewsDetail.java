package com.app.tlnewsapp.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.tlnewsapp.R;
import com.app.tlnewsapp.Config;
import com.app.tlnewsapp.models.News;
import com.app.tlnewsapp.realm.RealmController;
import com.app.tlnewsapp.utils.AppBarLayoutBehavior;
import com.app.tlnewsapp.utils.Constant;
import com.app.tlnewsapp.utils.GDPR;
import com.app.tlnewsapp.utils.Tools;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

public class ActivityNewsDetail extends AppCompatActivity {

    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";

    // give preparation animation activity transition
    public static void navigate(AppCompatActivity activity, View transitionView, News obj) {
        Intent intent = new Intent(activity, ActivityNewsDetail.class);
        intent.putExtra(EXTRA_OBJC, obj);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, EXTRA_OBJC);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    private MenuItem read_later_menu;
    private News post;
    private boolean flag_read_later;
    View parent_view, lyt_parent;
    private Menu menu;
    TextView txt_title, txt_category, txt_date, txt_comment_count, txt_comment_text;
    ImageView btn_comment, img_thumb_video;
    private WebView webview;
    private AdView adView;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        parent_view = findViewById(android.R.id.content);
        webview = findViewById(R.id.news_description);
        lyt_parent = findViewById(R.id.lyt_parent);

        txt_title = findViewById(R.id.title);
        txt_category = findViewById(R.id.category);
        txt_date = findViewById(R.id.date);
        txt_comment_count = findViewById(R.id.txt_comment_count);
        txt_comment_text = findViewById(R.id.txt_comment_text);
        btn_comment = findViewById(R.id.btn_comment);
        img_thumb_video = findViewById(R.id.thumbnail_video);

        btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivityComments.class);
                intent.putExtra("nid", post.nid);
                intent.putExtra("count", post.comments_count);
                startActivity(intent);
            }
        });

        txt_comment_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivityComments.class);
                intent.putExtra("nid", post.nid);
                intent.putExtra("count", post.comments_count);
                startActivity(intent);
            }
        });

        // animation transition
        ViewCompat.setTransitionName(findViewById(R.id.image), EXTRA_OBJC);

        // get extra object
        post = (News) getIntent().getSerializableExtra(EXTRA_OBJC);

        initToolbar();

        if (Config.ENABLE_RTL_MODE) {
            displayDataRTL();
        } else {
            displayData();
        }

        loadBannerAd();
        loadInterstitialAd();

    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void displayData() {
        txt_title.setText(Html.fromHtml(post.news_title));
        txt_comment_count.setText("" + post.comments_count);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (post.comments_count == 0) {
                    txt_comment_text.setText(R.string.txt_no_comment);
                }
                if (post.comments_count == 1) {
                    txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comment));
                } else if (post.comments_count > 1) {
                    txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comments));
                }
            }
        }, 1000);

        webview.setBackgroundColor(Color.parseColor("#ffffff"));
        webview.setFocusableInTouchMode(false);
        webview.setFocusable(false);
        if (!Config.ENABLE_TEXT_SELECTION) {
            webview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
            webview.setLongClickable(false);
        }
        webview.getSettings().setDefaultTextEncodingName("UTF-8");
        webview.getSettings().setJavaScriptEnabled(true);

        WebSettings webSettings = webview.getSettings();
        Resources res = getResources();
        int fontSize = res.getInteger(R.integer.font_size);
        webSettings.setDefaultFontSize(fontSize);

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = post.news_description;

        String text = "<html><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + "<style type=\"text/css\">body{color: #000000;}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Config.OPEN_LINK_INSIDE_APP) {
                    if (url.startsWith("http://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.startsWith("https://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebViewImage.class);
                        intent.putExtra("image_url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".pdf")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
                return true;
            }
        });

        webview.loadData(text, mimeType, encoding);

        txt_category.setText(post.category_name);
        txt_category.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCategory));

        txt_date.setText(Tools.getFormatedDate(post.news_date));

        ImageView news_image = findViewById(R.id.image);

        if (post.content_type != null && post.content_type.equals("youtube")) {
            Picasso.with(this)
                    .load(Constant.YOUTUBE_IMG_FRONT + post.video_id + Constant.YOUTUBE_IMG_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityYoutubePlayer.class);
                    intent.putExtra("video_id", post.video_id);
                    startActivity(intent);

                    showInterstitialAd();
                }
            });

        } else if (post.content_type != null && post.content_type.equals("Url")) {

            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", post.video_url);
                    startActivity(intent);

                    showInterstitialAd();
                }
            });
        } else if (post.content_type != null && post.content_type.equals("Upload")) {

            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", Config.ADMIN_PANEL_URL + "/upload/video/" + post.video_url);
                    startActivity(intent);

                    showInterstitialAd();
                }
            });
        } else {
            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ActivityFullScreenImage.class);
                    intent.putExtra("image", post.news_image);
                    startActivity(intent);
                }
            });
        }

        if (!post.content_type.equals("Post")) {
            img_thumb_video.setVisibility(View.VISIBLE);
        } else {
            img_thumb_video.setVisibility(View.GONE);
        }

    }

    private void displayDataRTL() {
        txt_title.setText(Html.fromHtml(post.news_title));
        txt_comment_count.setText("" + post.comments_count);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (post.comments_count == 0) {
                    txt_comment_text.setText(R.string.txt_no_comment);
                }
                if (post.comments_count == 1) {
                    txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comment));
                } else if (post.comments_count > 1) {
                    txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comments));
                }
            }
        }, 1000);

        webview.setBackgroundColor(Color.parseColor("#ffffff"));
        webview.setFocusableInTouchMode(false);
        webview.setFocusable(false);
        if (!Config.ENABLE_TEXT_SELECTION) {
            webview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
            webview.setLongClickable(false);
        }
        webview.getSettings().setDefaultTextEncodingName("UTF-8");
        webview.getSettings().setJavaScriptEnabled(true);

        WebSettings webSettings = webview.getSettings();
        Resources res = getResources();
        int fontSize = res.getInteger(R.integer.font_size);
        webSettings.setDefaultFontSize(fontSize);

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = post.news_description;

        String text = "<html dir='rtl'><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + "<style type=\"text/css\">body{color: #000000;}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Config.OPEN_LINK_INSIDE_APP) {
                    if (url.startsWith("http://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.startsWith("https://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebViewImage.class);
                        intent.putExtra("image_url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".pdf")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
                return true;
            }
        });

        webview.loadData(text, mimeType, encoding);

        txt_category.setText(post.category_name);
        txt_category.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCategory));

        txt_date.setText(Tools.getFormatedDate(post.news_date));

        ImageView news_image = findViewById(R.id.image);

        if (post.content_type != null && post.content_type.equals("youtube")) {
            Picasso.with(this)
                    .load(Constant.YOUTUBE_IMG_FRONT + post.video_id + Constant.YOUTUBE_IMG_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityYoutubePlayer.class);
                    intent.putExtra("video_id", post.video_id);
                    startActivity(intent);

                    showInterstitialAd();
                }
            });

        } else if (post.content_type != null && post.content_type.equals("Url")) {

            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", post.video_url);
                    startActivity(intent);

                    showInterstitialAd();
                }
            });
        } else if (post.content_type != null && post.content_type.equals("Upload")) {

            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", Config.ADMIN_PANEL_URL + "/upload/video/" + post.video_url);
                    startActivity(intent);

                    showInterstitialAd();
                }
            });
        } else {
            Picasso.with(this)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ActivityFullScreenImage.class);
                    intent.putExtra("image", post.news_image);
                    startActivity(intent);
                }
            });
        }

        if (!post.content_type.equals("Post")) {
            img_thumb_video.setVisibility(View.VISIBLE);
        } else {
            img_thumb_video.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_detail, menu);
        this.menu = menu;
        read_later_menu = menu.findItem(R.id.action_later);
        refreshReadLaterMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_later:

                if (post.isDraft()) {
                    Snackbar.make(parent_view, R.string.cannot_add_to_favorite, Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                String str;
                if (flag_read_later) {
                    RealmController.with(this).deleteNews(post.nid);
                    str = getString(R.string.favorite_removed);
                } else {
                    RealmController.with(this).saveNews(post);
                    str = getString(R.string.favorite_added);
                }
                Snackbar.make(parent_view, str, Snackbar.LENGTH_SHORT).show();
                refreshReadLaterMenu();

                break;

            case R.id.action_share:

                String formattedString = android.text.Html.fromHtml(post.news_description).toString();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, post.news_title + "\n" + formattedString + "\n" + getResources().getString(R.string.share_content) + "https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void refreshReadLaterMenu() {
        flag_read_later = RealmController.with(this).getNews(post.nid) != null;
        if (flag_read_later) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_white));
        } else {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_outline_white));
        }
    }

    public void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(ActivityNewsDetail.this)).build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdFailedToLoad(int error) {
                    adView.setVisibility(View.GONE);
                }

                @Override
                public void onAdLeftApplication() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onAdLoaded() {
                    adView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_ON_CLICK_VIDEO) {
            interstitialAd = new InterstitialAd(getApplicationContext());
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
            AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(ActivityNewsDetail.this)).build();
            interstitialAd.loadAd(adRequest);
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_ON_CLICK_VIDEO) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
        }
    }

}
