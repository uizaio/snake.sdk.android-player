package com.uiza.sampleplayer.app

import androidx.multidex.MultiDexApplication
import com.uiza.sdk.UZPlayer

class UZApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        UZPlayer.init(com.uiza.sdk.R.layout.uzplayer_skin_default)
    }

}
