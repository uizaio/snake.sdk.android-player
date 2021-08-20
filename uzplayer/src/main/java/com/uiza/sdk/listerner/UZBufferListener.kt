package com.uiza.sdk.listerner

interface UZBufferListener {
    fun onBufferChanged(bufferedDurationUs: Long, playbackSpeed: Float)
}
