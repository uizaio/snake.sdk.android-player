package com.uiza.sdk

import android.os.SystemClock
import com.uiza.sdk.utils.UZAppUtils

class UZPlayer {
    companion object {
        @JvmStatic
        var elapsedTime = SystemClock.elapsedRealtime()
            private set

        fun init() {
            if (!UZAppUtils.isDependencyAvailable(dependencyClass = "com.google.android.exoplayer2.SimpleExoPlayer")) {
                throw NoClassDefFoundError("Exo Player library is missing")
            }
            elapsedTime = SystemClock.elapsedRealtime()
        }

    }

}
