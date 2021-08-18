package com.uiza.sampleplayer.app;

import androidx.multidex.MultiDexApplication;

import com.uiza.api.UZApi;
import com.uiza.api.UZEnvironment;
import com.uiza.sampleplayer.BuildConfig;
import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.utils.UZData;

import timber.log.Timber;

public class UZApplication extends MultiDexApplication {

    public static final String[] urls = new String[]{
            "https://hls.ted.com/talks/2639.m3u8?preroll=Thousands",
            "https://bitmovin-a.akamaihd.net/content/playhouse-vr/mpds/105560.mpd",
            "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"};

    public static final String thumbnailUrl = "https://1955897154.rsc.cdn77.org/6fd7eafc8e6c441ea3f14c528f7266e6-static/2020/05/27/94a04fa4-07e2-43e5-9b86-d65f01bca611/thumbnail-10-8-720.jpeg";

    @Override
    public void onCreate() {
        super.onCreate();
        UZPlayer.init(this, true);
        UZApi.init(this, UZPlayer.getVersionName(), UZEnvironment.PRODUCTION);
    }
}
