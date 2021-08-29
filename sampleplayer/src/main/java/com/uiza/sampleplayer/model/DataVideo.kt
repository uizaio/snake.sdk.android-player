package com.uiza.sampleplayer.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class DataVideo(
    var linkPlay: String = "",
    var isPortraitVideo: Boolean = false
) : Serializable
