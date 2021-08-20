package com.uiza.sdk.interfaces

import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate

interface UZAdPlayerCallback {
    fun onPlay() {}
    fun onVolumeChanged(i: Int) {}
    fun onAdProgress(videoProgressUpdate: VideoProgressUpdate?) {}
    fun onPause() {}
    fun onLoaded() {}
    fun onResume() {}
    fun onEnded() {}
    fun onError() {}
    fun onBuffering() {}
}
