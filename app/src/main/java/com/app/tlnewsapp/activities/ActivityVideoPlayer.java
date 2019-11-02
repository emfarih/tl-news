package com.app.tlnewsapp.activities;

import android.os.Handler;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityVideoPlayer extends AppCompatActivity {

    private static final String TAG = "ActivityStreamPlayer";
    String video_url;
//    private SimpleExoPlayerView exoPlayerView;
//    private SimpleExoPlayer player;
//    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
//    private DataSource.Factory mediaDataSourceFactory;
    private Handler mainHandler;
    private ProgressBar progressBar;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_video_player);
//
//        if (Config.ENABLE_RTL_MODE) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
//            }
//        }
//
//        video_url = getIntent().getStringExtra("video_url");
//
//        progressBar = findViewById(R.id.progressBar);
//
////        mediaDataSourceFactory = buildDataSourceFactory(true);
//
//        mainHandler = new Handler();
////        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory videoTrackSelectionFactory =
//                new AdaptiveTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
//
//        LoadControl loadControl = new DefaultLoadControl();
//
//        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
//        exoPlayerView = findViewById(R.id.exoPlayerView);
//        exoPlayerView.setPlayer(player);
//        exoPlayerView.setUseController(true);
//        exoPlayerView.requestFocus();
//
//        Uri uri = Uri.parse(video_url);
//
//        MediaSource mediaSource = buildMediaSource(uri, null);
//
//        player.prepare(mediaSource);
//        player.setPlayWhenReady(true);
//
//        player.addListener(new Player.EventListener() {
//            @Override
//            public void onTimelineChanged(Timeline timeline, Object manifest) {
//                Log.d(TAG, "onTimelineChanged: ");
//            }
//
//            @Override
//            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
//                Log.d(TAG, "onTracksChanged: " + trackGroups.length);
//            }
//
//            @Override
//            public void onLoadingChanged(boolean isLoading) {
//                Log.d(TAG, "onLoadingChanged: " + isLoading);
//            }
//
//            @Override
//            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                Log.d(TAG, "onPlayerStateChanged: " + playWhenReady);
//                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
//                    progressBar.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onRepeatModeChanged(int repeatMode) {
//
//            }
//
//            @Override
//            public void onPlayerError(ExoPlaybackException error) {
//                Log.e(TAG, "onPlayerError: ", error);
//                player.stop();
//                errorDialog();
//            }
//
//            @Override
//            public void onPositionDiscontinuity() {
//                Log.d(TAG, "onPositionDiscontinuity: true");
//            }
//
//            @Override
//            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
//
//            }
//        });
//
//    }

//    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
//        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
//                : Util.inferContentType("." + overrideExtension);
//        switch (type) {
//            case C.TYPE_SS:
//                return new SsMediaSource(uri, buildDataSourceFactory(false),
//                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
//            case C.TYPE_DASH:
//                return new DashMediaSource(uri, buildDataSourceFactory(false),
//                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, null);
//            case C.TYPE_HLS:
//                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, null);
//            case C.TYPE_OTHER:
//                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
//                        mainHandler, null);
//            default: {
//                throw new IllegalStateException("Unsupported type: " + type);
//            }
//        }
//    }

//    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
//        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
//    }

//    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
//        return new DefaultDataSourceFactory(this, bandwidthMeter,
//                buildHttpDataSourceFactory(bandwidthMeter));
//    }

//    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
//        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "ExoPlayerDemo"), bandwidthMeter);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        player.stop();
    }

//    public void errorDialog() {
//        new AlertDialog.Builder(this)
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .setTitle("Oops!")
//                .setCancelable(false)
//                .setMessage("Failed to load stream, probably the stream server currently down!")
//                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        retryLoad();
//                    }
//
//                })
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        finish();
//                    }
//                })
//                .show();
//    }

//    public void retryLoad() {
//        Uri uri = Uri.parse(video_url);
//        MediaSource mediaSource = buildMediaSource(uri, null);
//        player.prepare(mediaSource);
//        player.setPlayWhenReady(true);
//    }

}
