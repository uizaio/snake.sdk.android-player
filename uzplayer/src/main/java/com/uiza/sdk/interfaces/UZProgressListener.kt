package com.uiza.sdk.interfaces

interface UZProgressListener {
    fun onVideoProgress(currentMls: Long, s: Int, duration: Long, percent: Int) {}
    fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}
    fun onBufferProgress(bufferedPosition: Long, bufferedPercentage: Int, duration: Long) {}
}
