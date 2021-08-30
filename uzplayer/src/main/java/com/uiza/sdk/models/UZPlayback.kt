package com.uiza.sdk.models

import androidx.annotation.Keep

@Keep
data class UZPlayback(
    var name: String? = null,
    var description: String? = null,
    var linkPlay: String? = null,
    var poster: String? = null,
)
