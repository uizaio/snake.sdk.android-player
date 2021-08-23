package com.uiza.sdk.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import com.uiza.sdk.R

class UZTextView : AppCompatTextView {
    private var isUseDefault = false
    private var isLandscape = false

    //sp
    var textSizeLand = -1F
        get() = if (field == -1F) 15F else field

    //sp
    var textSizePortrait = -1F
        get() = if (field == -1F) 10F else field

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        isUseDefault = if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.UZTextView, defStyleAttr, 0)
            try {
                a.getBoolean(R.styleable.UZTextView_useDefaultTV, true)
            } finally {
                a.recycle()
            }
        } else {
            true
        }
        setShadowLayer(
            1f,  // radius
            1f,  // dx
            1f,  // dy
            Color.BLACK // shadow color
        )
        updateSize()
        setSingleLine()
    }

    private fun updateSize() {
        if (!isUseDefault) {
            return
        }
        setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            if (isLandscape) {
                textSizeLand
            } else {
                textSizePortrait
            }
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        updateSize()
    }
}
