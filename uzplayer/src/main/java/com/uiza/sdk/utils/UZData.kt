package com.uiza.sdk.utils

import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.models.UZPlaybackInfo
import com.uiza.sdk.utils.StringUtils.parserInfo
import java.util.*

object UZData {

    private val logTag = javaClass.simpleName
    private var playback: UZPlayback? = null
    var urlIMAAd = ""
    private var playList: ArrayList<UZPlayback>? = null
    private var playbackInfo: UZPlaybackInfo? = null
    private var currentPositionOfPlayList = 0
    var isSettingPlayer = false

    fun getPlayback(): UZPlayback? {
        return playback
    }

    fun getPosterUrl(): String? {
        return playback?.poster
    }

    fun getPlaybackInfo(): UZPlaybackInfo? {
        return playbackInfo
    }

    fun setPlayback(playback: UZPlayback?) {
        this.playback = playback
        val firstLinkPlay = playback?.firstLinkPlay
        if (firstLinkPlay != null) {
            playbackInfo = parserInfo(linkPlay = firstLinkPlay)
        }
    }

    fun clear() {
        playback = null
        playbackInfo = null
        urlIMAAd = ""
    }

    fun getEntityId(): String? {
        return playback?.id
    }

    fun getEntityName(): String? {
        return playback?.name
    }

    fun getHost(): String? {
        if (playback == null) {
            return null
        }
        val url = playback?.firstPlayUrl ?: return null
        return url.host
    }

    fun isPlayWithPlaylistFolder(): Boolean {
        return playList != null
    }

    fun getPlayList(): ArrayList<UZPlayback>? {
        return playList
    }

    fun setPlayList(playlist: ArrayList<UZPlayback>?) {
        playList = playlist
    }

    fun getCurrentPositionOfPlayList(): Int {
        return currentPositionOfPlayList
    }

    fun setCurrentPositionOfPlayList(currentPositionOfPlayList: Int) {
        this.currentPositionOfPlayList = currentPositionOfPlayList
        playList?.let { list ->
            val currentPlayback = list[currentPositionOfPlayList]
            playback = currentPlayback
            val firstLinkPlay = playback?.firstLinkPlay
            if (firstLinkPlay != null) {
                playbackInfo = parserInfo(firstLinkPlay)
            }
        }
    }

    fun clearDataForPlaylistFolder() {
        playList = null
        currentPositionOfPlayList = 0
    }

}
