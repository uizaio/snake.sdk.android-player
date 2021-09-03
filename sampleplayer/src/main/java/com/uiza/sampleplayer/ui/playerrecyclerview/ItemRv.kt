package com.uiza.sampleplayer.ui.playerrecyclerview

import androidx.annotation.Keep
import com.uiza.sdk.models.UZPlayback
import java.io.Serializable

@Keep
data class ItemRv(
    var uzPlayback: UZPlayback? = null,
    var isFocussed: Boolean = false,
) : Serializable
