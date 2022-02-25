package com.uiza.sdk.utils

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build

object UZAppUtils {

    @JvmStatic
    fun checkChromeCastAvailable(): Boolean {
        return (
                isDependencyAvailable("com.google.android.gms.cast.framework.OptionsProvider") &&
                        isDependencyAvailable("androidx.mediarouter.app.MediaRouteButton")
                )
    }

    @JvmStatic
    fun isDependencyAvailable(dependencyClass: String): Boolean {
        try {
            Class.forName(dependencyClass)
            return true
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    @JvmStatic
    fun isTablet(context: Context): Boolean {
        return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    @JvmStatic
    fun isTV(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    /**
     * Feature for [PackageManager.getSystemAvailableFeatures] and [PackageManager.hasSystemFeature]:
     * The device supports picture-in-picture multi-window mode.
     */
    @JvmStatic
    fun hasSupportPIP(context: Context): Boolean {
        return (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                        context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                )
    }
}
