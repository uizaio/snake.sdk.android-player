package com.uiza.sdk.utils;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static android.content.Context.ACTIVITY_SERVICE;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.NonNull;

public class UZAppUtils {
    private UZAppUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }


    public static String getUserAgent(@NonNull Context context) {
        return context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
    }

    public static boolean checkChromeCastAvailable() {
        return UZAppUtils.isDependencyAvailable("com.google.android.gms.cast.framework.OptionsProvider")
                && UZAppUtils.isDependencyAvailable("androidx.mediarouter.app.MediaRouteButton");
    }

    public static boolean isAdsDependencyAvailable() {
        return UZAppUtils.isDependencyAvailable("com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer");
    }

    public static boolean isDependencyAvailable(String dependencyClass) {
        try {
            Class.forName(dependencyClass);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    //return true if app is in foreground
    public static boolean isAppInForeground(@NonNull Context context) {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
            return true;
        }
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        // App is foreground, but screen is locked, so show notification
        return km != null && km.inKeyguardRestrictedInputMode();
    }

    public static boolean checkServiceRunning(@NonNull Context context, String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceName.equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTablet(@NonNull Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isTV(@NonNull Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    /**
     * Feature for {@link PackageManager#getSystemAvailableFeatures} and {@link PackageManager#hasSystemFeature}:
     * The device supports picture-in-picture multi-window mode.
     */
    public static boolean hasSupportPIP(@NonNull Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
    }

}
