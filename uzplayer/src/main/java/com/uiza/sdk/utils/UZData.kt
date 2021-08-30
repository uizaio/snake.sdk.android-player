package com.uiza.sdk.utils

import com.uiza.sdk.models.UZPlayback

object UZData {
    private var playback: UZPlayback? = null

    fun getPlayback(): UZPlayback? {
        return playback
    }

    fun setPlayback(playback: UZPlayback?) {
        this.playback = playback
    }

    fun getEntityName(): String? {
        return playback?.name
    }

}
