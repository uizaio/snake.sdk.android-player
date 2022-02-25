package com.uiza.sdk.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

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
}
