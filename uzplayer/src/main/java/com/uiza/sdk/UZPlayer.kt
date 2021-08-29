package com.uiza.sdk

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import com.uiza.sdk.analytics.UZAnalytic.Companion.init
import com.uiza.sdk.utils.UZAppUtils

class UZPlayer {
    companion object {
        @JvmStatic
        var elapsedTime = SystemClock.elapsedRealtime()
            private set

        @SuppressLint("HardwareIds")
        fun init(context: Context, prodEnv: Boolean) {
            if (!UZAppUtils.isDependencyAvailable(dependencyClass = "com.google.android.exoplayer2.SimpleExoPlayer")) {
                throw NoClassDefFoundError("Exo Player library is missing")
            }
            val deviceId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            init(deviceId = deviceId, prodEnv = prodEnv)
            elapsedTime = SystemClock.elapsedRealtime()
        }

    }

}
