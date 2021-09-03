package com.uiza.sampleplayer.ui.playerlist

import androidx.annotation.Keep
import com.uiza.sdk.models.UZPlayback
import java.io.Serializable

@Keep
data class Item(
    var uzPlayback: UZPlayback? = null,
    var isPlaying: Boolean = false,
) : Serializable
