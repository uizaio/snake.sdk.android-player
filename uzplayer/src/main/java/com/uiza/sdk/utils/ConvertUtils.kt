package com.uiza.sdk.utils

import android.content.res.Resources
import android.text.TextUtils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist

object ConvertUtils {
    private const val EXT_X_PROGRAM_DATE_TIME = "#EXT-X-PROGRAM-DATE-TIME:"
    private const val EXTINF = "#EXTINF:"
    private const val EXT_X_UZ_TIMESHIFT = "#EXT-X-UZ-TIMESHIFT:"

    @JvmStatic
    fun dp2px(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dp(pxValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun sp2px(spValue: Float): Int {
        val fontScale = Resources.getSystem().displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    fun px2sp(pxValue: Float): Int {
        val fontScale = Resources.getSystem().displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    @JvmStatic
    fun getTimeShiftUrl(playlist: HlsMasterPlaylist?): String? {
        if (playlist == null || ListUtils.isEmpty(playlist.tags)) return null
        for (tag in playlist.tags) {
            if (tag.contains(EXT_X_UZ_TIMESHIFT)) {
                return tag.replace(EXT_X_UZ_TIMESHIFT, "")
            }
        }
        return null
    }

    @JvmStatic
    fun getProgramDateTime(playlist: HlsMediaPlaylist?, timeToEndChunk: Long): Long {
        if (playlist == null || ListUtils.isEmpty(playlist.tags)) return C.INDEX_UNSET.toLong()
        val emptyStr = ""
        val tagSize = playlist.tags.size
        var totalTime: Long = 0
        var playingIndex = tagSize
        // Find the playing frame index
        while (playingIndex > 0) {
            val tag = playlist.tags[playingIndex - 1]
            if (tag.contains(EXTINF)) {
                totalTime += (tag.replace(",", emptyStr).replace(
                    oldValue = EXTINF,
                    newValue = emptyStr
                )
                    .toDouble() * 1000).toLong()
                if (totalTime >= timeToEndChunk) {
                    break
                }
            }
            playingIndex--
        }
        if (playingIndex >= tagSize) {
            // That means the livestream latency is larger than 1 segment (duration).
            // we should skip to calc latency in this case
            return C.INDEX_UNSET.toLong()
        }
        // Find the playing frame EXT_X_PROGRAM_DATE_TIME
        var playingDateTime = emptyStr
        for (i in playingIndex until tagSize) {
            val tag = playlist.tags[i]
            if (tag.contains(EXT_X_PROGRAM_DATE_TIME)) {
                playingDateTime = tag.replace(
                    oldValue = EXT_X_PROGRAM_DATE_TIME,
                    newValue = emptyStr
                )
                break
            }
        }
        return if (TextUtils.isEmpty(playingDateTime)) {
            // That means something wrong with the format, check with server
            // we should skip to calc latency in this case
            C.INDEX_UNSET.toLong()
        } else {
            StringUtils.convertUTCMs(playingDateTime)
        }
        // int list of frame, we get the EXT_X_PROGRAM_DATE_TIME of current playing frame
    }
}
