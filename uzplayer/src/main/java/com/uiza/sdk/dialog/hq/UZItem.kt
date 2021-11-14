package com.uiza.sdk.dialog.hq

import android.widget.CheckedTextView
import com.google.android.exoplayer2.Format

// https://www.image-engineering.de/library/technotes/991-separating-sd-hd-full-hd-4k-and-8k
class UZItem {

    companion object {
        fun create(format: Format, description: String): UZItem {
            return UZItem(format, description)
        }

        @JvmStatic
        fun create(): UZItem {
            return UZItem()
        }
    }

    var checkedTextView: CheckedTextView? = null
    var description: String
    var format: Format? = null

    private constructor(format: Format, description: String) {
        this.format = format
        this.description = description
    }

    private constructor() {
        description = "Unknown"
    }
}
