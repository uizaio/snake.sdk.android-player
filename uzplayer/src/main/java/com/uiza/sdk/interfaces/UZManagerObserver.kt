package com.uiza.sdk.interfaces

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Timeline
import com.uiza.sdk.view.UZPlayerView

interface UZManagerObserver {
    val isPIPEnable: Boolean
    val playerView: UZPlayerView?
    val isAutoStart: Boolean
    // progress
    fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int)
    fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int)
    fun onPlayerEnded()
    fun onPlayerError(error: ExoPlaybackException?)
}
