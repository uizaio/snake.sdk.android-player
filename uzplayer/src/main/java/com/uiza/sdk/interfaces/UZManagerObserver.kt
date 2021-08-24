package com.uiza.sdk.interfaces

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Timeline
import com.uiza.sdk.utils.UZData
import com.uiza.sdk.view.UZPlayerView

interface UZManagerObserver {
    val title: String?
        get() = UZData.getEntityName()
    val isPIPEnable: Boolean
    val playerView: UZPlayerView?

    // options
    val isCastingChromecast: Boolean
    val isAutoStart: Boolean
    val adPlayerCallback: UZAdPlayerCallback?

    // progress
    fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int)
    fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int)
    fun onPlayerEnded()
    fun onPlayerError(error: ExoPlaybackException?)
}
