package com.uiza.sdk.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.graphics.drawable.DrawableCompat
import androidx.mediarouter.app.MediaRouteButton
import com.uiza.sdk.exceptions.ErrorConstant
import com.uiza.sdk.utils.UZAppUtils

class UZMediaRouteButton : MediaRouteButton {
    var mRemoteIndicatorDrawable: Drawable? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        checkChromeCastAvailable()
    }

    override fun setRemoteIndicatorDrawable(d: Drawable) {
        mRemoteIndicatorDrawable = d
        super.setRemoteIndicatorDrawable(d)
    }

    fun applyTint(color: Int) {
        mRemoteIndicatorDrawable?.let {
            val wrapDrawable = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrapDrawable, color)
        }
    }

    private fun checkChromeCastAvailable() {
        if (!UZAppUtils.checkChromeCastAvailable()) {
            throw NoClassDefFoundError(ErrorConstant.ERR_505)
        }
    }
}
