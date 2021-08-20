package com.uiza.sdk.models

import androidx.annotation.Keep

@Keep
enum class UZEventType {
    PLAYER_READY,  // playerready
    LOAD_START,  // loadstart
    VIEW_START,  //viewstart
    PAUSE,  // pause
    PLAY,  // play
    PLAYING,
    SEEKING,
    SEEKED,
    WAITING,
    RATE_CHANGE,
    REBUFFER_START,
    REBUFFER_END,
    VOLUME_CHANGE,
    FULLSCREEN_CHANGE,
    VIEW_END,
    ERROR,
    WATCHING,
    VIEW,
}
