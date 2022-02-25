package com.uiza.sdk.utils

import java.util.*

object StringUtils {
    private fun convertSecondsToHMmSs(seconds: Long): String {
        if (seconds <= 0) {
            return "0:00"
        }
        val s = seconds % 60
        val m = seconds / 60 % 60
        val h = seconds / (60 * 60) % 24
        return if (h == 0L) {
            String.format(Locale.getDefault(), "%d:%02d", m, s)
        } else {
            String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
        }
    }

    @JvmStatic
    fun convertMlsecondsToHMmSs(mls: Long): String {
        return convertSecondsToHMmSs(mls / 1000)
    }
}
