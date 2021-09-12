package com.uiza.sdk.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.uiza.sdk.R
import com.uiza.sdk.utils.Constants
import com.uiza.sdk.utils.ConvertUtils.dp2px
import com.uiza.sdk.utils.UZAppUtils
import com.uiza.sdk.utils.UZViewUtils
import com.uiza.sdk.utils.UZViewUtils.isFullScreen
import com.uiza.sdk.utils.UZViewUtils.screenWidth

class UZImageButton : AppCompatImageButton {
    private var drawableEnabled: Drawable? = null
    private var drawableDisabled: Drawable? = null
    private var screenWPortrait = 0
    private var screenWLandscape = 0
    private var isUseDefault = false
    private var ratioLand = 7
    private var ratioPort = 5
    var size = 0
        private set

    constructor(context: Context) : super(context) {
        initSizeScreenW(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initSizeScreenW(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initSizeScreenW(attrs, defStyleAttr)
    }

    fun setSrcDrawableEnabled() {
        drawableEnabled?.let {
            isClickable = true
            isFocusable = true
            setImageDrawable(it)
        }
        clearColorFilter()
        invalidate()
    }

    fun setSrcDrawableDisabled() {
        isClickable = false
        isFocusable = false
        if (drawableDisabled == null) {
            setColorFilter(Color.GRAY)
        } else {
            setImageDrawable(drawableDisabled)
            clearColorFilter()
        }
        invalidate()
    }

    fun setSrcDrawableDisabledCanTouch() {
        isClickable = true
        isFocusable = true
        if (drawableDisabled == null) {
            setColorFilter(Color.GRAY)
        } else {
            setImageDrawable(drawableDisabled)
            clearColorFilter()
        }
        invalidate()
    }

    private fun initSizeScreenW(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs != null) {
            val a =
                context.obtainStyledAttributes(attrs, R.styleable.UZImageButton, defStyleAttr, 0)
            try {
                isUseDefault = a.getBoolean(R.styleable.UZImageButton_useDefaultIB, true)
                drawableDisabled = a.getDrawable(R.styleable.UZImageButton_srcDisabled)
            } finally {
                a.recycle()
            }
        } else {
            isUseDefault = true
            drawableDisabled = null
        }
        //disable click sound of a particular button in android app
        isSoundEffectsEnabled = false
        if (!isUseDefault) {
            drawableEnabled = drawable
            return
        }
        val isTablet = UZAppUtils.isTablet(context)
        if (isTablet) {
            ratioLand = Constants.RATIO_LAND_TABLET
            ratioPort = Constants.RATIO_PORTRAIT_TABLET
        } else {
            ratioLand = Constants.RATIO_LAND_MOBILE
            ratioPort = Constants.RATIO_PORTRAIT_MOBILE
        }
        screenWPortrait = screenWidth
        screenWLandscape = UZViewUtils.getScreenHeightIncludeNavigationBar(this.context)
        //set padding 5dp
        val px = dp2px(5f)
        setPadding(px, px, px, px)
        post {
            if (isFullScreen(context)) {
                updateSizeLandscape()
            } else {
                updateSizePortrait()
            }
        }
        drawableEnabled = drawable
    }

    fun getRatioLand(): Int {
        return ratioLand
    }

    fun setRatioLand(ratioLand: Int) {
        this.ratioLand = ratioLand
        if (isFullScreen(context)) {
            updateSizeLandscape()
        } else {
            updateSizePortrait()
        }
    }

    fun getRatioPort(): Int {
        return ratioPort
    }

    fun setRatioPort(ratioPortrait: Int) {
        this.ratioPort = ratioPortrait
        if (isFullScreen(context)) {
            updateSizeLandscape()
        } else {
            updateSizePortrait()
        }
    }

    private fun updateSizePortrait() {
        if (!isUseDefault) {
            return
        }
        if (ratioPort == 0) {
            throw IllegalArgumentException("Invalid: ratioPort == 0")
        }
        size = screenWPortrait / ratioPort
        this.layoutParams.width = size
        this.layoutParams.height = size
        requestLayout()
    }

    private fun updateSizeLandscape() {
        if (!isUseDefault) {
            return
        }
        if (ratioLand == 0) {
            throw IllegalArgumentException("Invalid: ratioLand == 0")
        }
        size = screenWLandscape / ratioLand
        this.layoutParams.width = size
        this.layoutParams.height = size
        requestLayout()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            updateSizeLandscape()
        } else {
            updateSizePortrait()
        }
    }

    fun setUIVisible(isVisible: Boolean) {
        isClickable = isVisible
        isFocusable = isVisible
        if (isVisible) {
            setSrcDrawableEnabled()
        } else {
            setImageResource(0)
        }
    }
}
