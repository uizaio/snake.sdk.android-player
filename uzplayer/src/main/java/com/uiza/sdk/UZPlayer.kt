package com.uiza.sdk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import androidx.annotation.LayoutRes
import com.uiza.sdk.analytics.UZAnalytic.Companion.init
import com.uiza.sdk.chromecast.Casty
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.Constants
import com.uiza.sdk.utils.UZAppUtils
import com.uiza.sdk.utils.UZData

class UZPlayer {
    companion object {
        @JvmStatic
        var elapsedTime = SystemClock.elapsedRealtime()
            private set

        /**
         * default dev
         * init SDK
         */
        fun init(context: Context) {
            init(
                context = context,
                skinLayoutId = R.layout.uzplayer_skin_default,
                prodEnv = false
            )
        }

        /**
         * init SDK
         */
        fun init(context: Context, prodEnv: Boolean) {
            init(
                context = context,
                skinLayoutId = R.layout.uzplayer_skin_default,
                prodEnv = prodEnv
            )
        }

        /**
         * initSDK
         *
         * @param skinLayoutId Skin of player
         */
        @SuppressLint("HardwareIds")
        fun init(context: Context, @LayoutRes skinLayoutId: Int, prodEnv: Boolean) {
            if (!UZAppUtils.isDependencyAvailable(dependencyClass = "com.google.android.exoplayer2.SimpleExoPlayer")) {
                throw NoClassDefFoundError("Exo Player library is missing")
            }
            val deviceId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            init(deviceId = deviceId, prodEnv = prodEnv)
            setUZPlayerSkinLayoutId(skinLayoutId)
            elapsedTime = SystemClock.elapsedRealtime()
        }

        /**
         * set Casty
         *
         * @param activity: Activity
         */
        @JvmStatic
        fun setCasty(activity: Activity) {
            if (UZAppUtils.isTV(activity)) {
                return
            }
            if (!UZAppUtils.checkChromeCastAvailable()) {
                throw NoClassDefFoundError("Chromecast library is missing")
            }
            UZData.casty = Casty.create(activity)
        }

        /**
         * @return Casty
         */
        @JvmStatic
        val casty: Casty?
            get() = UZData.casty

        /**
         * set Player Skin layout_id
         *
         * @param resLayoutMain: id of layout xml
         */
        @JvmStatic
        fun setUZPlayerSkinLayoutId(@LayoutRes resLayoutMain: Int) {
            UZData.uzPlayerSkinLayoutId = resLayoutMain
        }

        /**
         * user with UZDragView
         *
         * @param useUZDragView: boolean
         */
        fun setUseWithUZDragView(useUZDragView: Boolean) {
            UZData.useUZDragView = useUZDragView
        }

        /**
         * set current UZPlayBack for Custom Link Play
         *
         * @param playback: [UZPlayback]
         */
        @JvmStatic
        var currentPlayback: UZPlayback?
            get() = UZData.getPlayback()
            set(playback) {
                UZData.setPlayback(playback)
            }

        fun getVersionName(): String {
            return Constants.PLAYER_SDK_VERSION
        }
    }

}
