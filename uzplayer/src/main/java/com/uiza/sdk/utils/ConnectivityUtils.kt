package com.uiza.sdk.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager

object ConnectivityUtils {
    private fun getConnectivityManager(context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @kotlin.jvm.JvmStatic
    fun isConnected(context: Context): Boolean {
        val cm = getConnectivityManager(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork
            if (network != null) {
                val ncs = cm.getNetworkCapabilities(network)
                return ncs != null && (
                    ncs.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || ncs.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    )
                    )
            }
            false
        } else {
            val info = cm.activeNetworkInfo
            info != null && info.isConnected
        }
    }

    fun isConnectedWifi(context: Context): Boolean {
        val cm = getConnectivityManager(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork
            if (network != null) {
                val ncs = cm.getNetworkCapabilities(network)
                return ncs != null && ncs.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
            false
        } else {
            val info = cm.activeNetworkInfo
            info != null && info.isConnected && info.type == ConnectivityManager.TYPE_WIFI
        }
    }

    fun isConnectedMobile(context: Context): Boolean {
        val cm = getConnectivityManager(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork
            if (network != null) {
                val ncs = cm.getNetworkCapabilities(network)
                return ncs != null && ncs.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            }
            false
        } else {
            val info = cm.activeNetworkInfo
            info != null && info.isConnected && info.type == ConnectivityManager.TYPE_MOBILE
        }
    }

    /**
     * Check if there is fast connectivity
     */
    fun isConnectedFast(context: Context): Boolean {
        val info = getConnectivityManager(context).activeNetworkInfo
        return info != null && info.isConnected && isConnectionFast(info.type, info.subtype)
    }

    /**
     * Check if the connection is fast
     */
    private fun isConnectionFast(type: Int, subType: Int): Boolean {
        return when (type) {
            ConnectivityManager.TYPE_WIFI -> {
                true
            }
            ConnectivityManager.TYPE_MOBILE -> {
                when (subType) {
                    TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_CDMA,
                    TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_GPRS,
                    TelephonyManager.NETWORK_TYPE_IDEN -> false
                    TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A,
                    TelephonyManager.NETWORK_TYPE_HSDPA,
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_UMTS,
                    TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_HSPAP,
                    TelephonyManager.NETWORK_TYPE_LTE,
                    TelephonyManager.NETWORK_TYPE_IWLAN,
                    19 -> true
                    TelephonyManager.NETWORK_TYPE_UNKNOWN -> false
                    else -> false
                }
            }
            else -> {
                false
            }
        }
    }
}
