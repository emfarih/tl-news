package com.app.tlnewsapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.androidnewsapp.R;
import com.app.tlnewsapp.Config;
import com.app.tlnewsapp.fragment.FragmentCategory;
import com.app.tlnewsapp.fragment.FragmentFavorite;
import com.app.tlnewsapp.fragment.FragmentProfile;
import com.app.tlnewsapp.fragment.FragmentRecent;
import com.app.tlnewsapp.fragment.FragmentVideo;
import com.app.tlnewsapp.utils.AppBarLayoutBehavior;
import com.app.tlnewsapp.utils.Constant;
import com.app.tlnewsapp.utils.GDPR;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private long exitTime = 0;
    MyApplication myApplication;
    View view;
    private BottomNavigationView navigation;
    public static ViewPager viewPager;
    private Toolbar toolbar;
    MenuItem prevMenuItem;
    int pager_number = 5;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(android.R.id.content);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        AppBarLayout appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        myApplication = MyApplication.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(pager_number);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.navigation_category:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.navigation_video:
                        viewPager.setCurrentItem(2);
                        return true;
                    case R.id.navigation_favorite:
                        viewPager.setCurrentItem(3);
                        return true;
                    case R.id.navigation_profile:
                        viewPager.setCurrentItem(4);
                        return true;
                }
                return false;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPager.getCurrentItem() == 1) {
                    toolbar.setTitle(getResources().getString(R.string.title_nav_category));
                } else if (viewPager.getCurrentItem() == 2) {
                    toolbar.setTitle(getResources().getString(R.string.title_nav_video));
                } else if (viewPager.getCurrentItem() == 3) {
                    toolbar.setTitle(getResources().getString(R.string.title_nav_favorite));
                } else if (viewPager.getCurrentItem() == 4) {
                    toolbar.setTitle(getResources().getString(R.string.title_nav_profile));
                } else {
                    toolbar.setTitle(R.string.app_name);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (Config.ENABLE_RTL_MODE) {
            viewPager.setRotationY(180);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Constant.REGISTRATION_COMPLETE)) {
                    // now subscribe to global topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Constant.TOPIC_GLOBAL);

                } else if (intent.getAction().equals(Constant.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    String message = intent.getStringExtra("message");
                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();
                }
            }
        };

        Intent intent = getIntent();
        final String message = intent.getStringExtra("message");
        final String imageUrl = intent.getStringExtra("image");
        final long nid = intent.getLongExtra("id", 0);
        final String link = intent.getStringExtra("link");

        if (message != null) {

            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);
            View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_notif, null);

            final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setView(mView);

            final TextView notification_title = mView.findViewById(R.id.news_title);
            final TextView notification_message = mView.findViewById(R.id.news_message);
            final ImageView notification_image = mView.findViewById(R.id.news_image);

            if (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg") || imageUrl.endsWith(".png") || imageUrl.endsWith(".gif")) {
                notification_title.setText(message);
                notification_message.setVisibility(View.GONE);
                Picasso.with(MainActivity.this)
                        .load(imageUrl.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_thumbnail)
                        .resize(200, 200)
                        .centerCrop()
                        .into(notification_image);

                alert.setPositiveButton(R.string.dialog_read_more, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), ActivityNotificationDetail.class);
                        intent.putExtra("id", nid);
                        startActivity(intent);
                    }
                });
                alert.setNegativeButton(R.string.dialog_dismiss, null);

            } else {
                notification_title.setText(getResources().getString(R.string.app_name));

                notification_message.setVisibility(View.VISIBLE);
                notification_message.setText(message);

                notification_image.setVisibility(View.GONE);

                //Toast.makeText(getApplicationContext(), "link : " + link, Toast.LENGTH_SHORT).show();

                if (!link.equals("")) {
                    alert.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                            startActivity(open);
                        }
                    });
                    alert.setNegativeButton(R.string.dialog_dismiss, null);
                } else {
                    alert.setPositiveButton(R.string.dialog_ok, null);
                }
            }

            alert.setCancelable(false);
            alert.show();

        }

        GDPR.updateConsentStatus(this);

    }

    public class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FragmentRecent();
                case 1:
                    return new FragmentCategory();
                case 2:
                    return new FragmentVideo();
                case 3:
                    return new FragmentFavorite();
                case 4:
                    return new FragmentProfile();
            }
            return null;
        }

        @Override
        public int getCount() {
            return pager_number;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.search:
                Intent intent = new Intent(getApplicationContext(),ActivitySearch.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }


    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem((0), true);
        } else {
            exitApp();
        }
    }

    public void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
