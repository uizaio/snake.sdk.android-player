package com.uiza.sampleplayer.app

import androidx.multidex.MultiDexApplication
import com.uiza.sampleplayer.R
import com.uiza.sdk.UZPlayer

//firebase https://console.firebase.google.com/u/0/project/snake-sdk-android-player/overview
class UZApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        UZPlayer.init(R.layout.uzplayer_skin_default)
//        UZPlayer.init(com.uiza.sdk.R.layout.uzplayer_skin_0)
//        UZPlayer.init(com.uiza.sdk.R.layout.uzplayer_skin_1)
//        UZPlayer.init(com.uiza.sdk.R.layout.uzplayer_skin_2)
//        UZPlayer.init(com.uiza.sdk.R.layout.uzplayer_skin_3)
    }
}
