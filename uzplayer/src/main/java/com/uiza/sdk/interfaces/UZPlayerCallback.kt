package com.uiza.sdk.interfaces

import com.uiza.sdk.exceptions.UZException
import com.uiza.sdk.view.UZPlayerView

interface UZPlayerCallback {
    //when video init done with result
    //isInitSuccess onStateReadyFirst
//    fun playerViewCreated(playerView: UZPlayerView) {}

//    fun isInitResult(linkPlay: String) {}

    //when skin is changed
//    fun onSkinChange() {}
    fun onTimeShiftChange(timeShiftOn: Boolean) {}

    //when screen rotate
    fun onScreenRotate(isLandscape: Boolean) {}

    //when UZVideoView had an error
    fun onError(e: UZException) {}
}
