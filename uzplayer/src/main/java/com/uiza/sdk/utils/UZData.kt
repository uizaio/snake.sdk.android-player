package com.uiza.sdk.utils

import com.uiza.sdk.models.UZPlayback

object UZData {
    private var playback: UZPlayback? = null

    fun getPlayback(): UZPlayback? {
        return playback
    }

    fun getPosterUrl(): String? {
        return playback?.poster
    }

    fun setPlayback(playback: UZPlayback?) {
        this.playback = playback
    }

    fun clear() {
        playback = null
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
}
