package com.uiza.sdk.interfaces

interface UZBufferListener {
    fun onBufferChanged(bufferedDurationUs: Long, playbackSpeed: Float)
}
