package com.uiza.sdk

import android.os.SystemClock
import com.uiza.sdk.utils.UZAppUtils

class UZPlayer {
    companion object {
        @JvmStatic
        var elapsedTime = SystemClock.elapsedRealtime()
            private set

        var skinDefault = R.layout.uzplayer_skin_default

        fun init(skinDefault: Int) {
            if (!UZAppUtils.isDependencyAvailable(dependencyClass = "com.google.android.exoplayer2.SimpleExoPlayer")) {
                throw NoClassDefFoundError("Exo Player library is missing")
            }
            this.elapsedTime = SystemClock.elapsedRealtime()
            this.skinDefault = skinDefault
        }
    }

}
