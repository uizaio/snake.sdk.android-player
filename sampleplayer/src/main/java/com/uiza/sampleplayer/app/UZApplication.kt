package com.uiza.sampleplayer.app

import androidx.multidex.MultiDexApplication
import com.uiza.sdk.UZPlayer

class UZApplication : MultiDexApplication() {

    companion object {
        @JvmField
        val urls = arrayOf(
            "https://hls.ted.com/talks/2639.m3u8?preroll=Thousands",
            "https://bitmovin-a.akamaihd.net/content/playhouse-vr/mpds/105560.mpd",
            "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        )
    }

    override fun onCreate() {
        super.onCreate()

        UZPlayer.init()
    }

}
