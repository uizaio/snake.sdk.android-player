package com.uiza.sampleplayer.ui.playertiktokslidehorizontal

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Data(
    var linkPlay: String = "",
    var isPortraitVideo: Boolean = false
) : Serializable
