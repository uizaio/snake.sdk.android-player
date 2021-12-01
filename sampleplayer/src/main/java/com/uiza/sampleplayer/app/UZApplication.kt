package com.uiza.sampleplayer.app

import androidx.multidex.MultiDexApplication
import com.uiza.sdk.UZPlayer

// TODO pip bi loi khi xoay man hinh, luc xem video portrait
class UZApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        UZPlayer.init(com.uiza.sdk.R.layout.uzplayer_skin_default)
//        UZPlayer.init(com.uiza.sdk.R.layout.uzplayer_skin_0)
//        UZPlayer.init(com.uiza.sdk.R.layout.uzplayer_skin_1)
//        UZPlayer.init(com.uiza.sdk.R.layout.uzplayer_skin_2)
//        UZPlayer.init(com.uiza.sdk.R.layout.uzplayer_skin_3)
    }
}
