package com.uiza.sdk.widget

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.ui.TimeBar.OnScrubListener
import com.uiza.sdk.R
import com.uiza.sdk.widget.previewseekbar.PreviewDelegate
import com.uiza.sdk.widget.previewseekbar.PreviewLoader
import com.uiza.sdk.widget.previewseekbar.PreviewView
import com.uiza.sdk.widget.previewseekbar.PreviewView.OnPreviewChangeListener
import java.util.*

class UZPreviewTimeBar(context: Context, attrs: AttributeSet?) : DefaultTimeBar(context, attrs),
    PreviewView, OnScrubListener {

    companion object {
        fun getDefaultScrubberColor(playedColor: Int): Int {
            return -0x1000000 or playedColor
        }
    }

    private val listeners = ArrayList<OnPreviewChangeListener?>()
    private var delegate: PreviewDelegate? = null
    private var scrubProgress = 0
    private var duration = 0
    private val scrubberColor: Int
    private val frameLayoutId: Int
    private val scrubberDiameter: Int

    init {
        var a = context.theme.obtainStyledAttributes(
            attrs,
            com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar, 0, 0
        )
        val playedColor = a.getInt(
            com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar_played_color,
            DEFAULT_PLAYED_COLOR
        )
        scrubberColor = a.getInt(
            com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar_scrubber_color,
            getDefaultScrubberColor(playedColor)
        )
        val defaultScrubberDraggedSize = dpToPx(
            context.resources.displayMetrics,
            DEFAULT_SCRUBBER_DRAGGED_SIZE_DP
        )
        scrubberDiameter = a.getDimensionPixelSize(
            com.google.android.exoplayer2.ui.R.styleable.DefaultTimeBar_scrubber_dragged_size,
            defaultScrubberDraggedSize
        )
        a.recycle()
        a = context.theme.obtainStyledAttributes(attrs, R.styleable.UZPreviewTimeBar, 0, 0)
        frameLayoutId = a.getResourceId(R.styleable.UZPreviewTimeBar_previewFrameLayout, NO_ID)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        delegate = PreviewDelegate(this, scrubberColor)
        delegate?.setEnabled(isEnabled)
        addListener(this)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        delegate?.let { pd ->
            if (!pd.isSetup && width != 0 && height != 0 && !isInEditMode) {
                if (parent is ViewGroup) {
                    pd.onLayout(parent as ViewGroup, frameLayoutId)
                }
            }
        }
    }

    override fun setPreviewColorTint(color: Int) {
        delegate?.setPreviewColorTint(color)
    }

    override fun setPreviewColorResourceTint(@ColorRes color: Int) {
        delegate?.setPreviewColorResourceTint(color)
    }

    override fun setPreviewLoader(previewLoader: PreviewLoader) {
        delegate?.setPreviewLoader(previewLoader)
    }

    override fun attachPreviewFrameLayout(frameLayout: FrameLayout?) {
        delegate?.attachPreviewFrameLayout(frameLayout)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        delegate?.setEnabled(enabled)
    }

    override fun setDuration(duration: Long) {
        super.setDuration(duration)
        this.duration = duration.toInt()
    }

    override fun setPosition(position: Long) {
        super.setPosition(position)
        scrubProgress = position.toInt()
    }

    override fun isShowingPreview(): Boolean {
        return delegate?.isShowing ?: false
    }

    override fun showPreview() {
        if (isEnabled) {
            delegate?.show()
        }
    }

    override fun hidePreview() {
        if (isEnabled) {
            delegate?.hide()
        }
    }

    override fun getProgress(): Int {
        return scrubProgress
    }

    override fun getMax(): Int {
        return duration
    }

    override fun getThumbOffset(): Int {
        return scrubberDiameter / 2
    }

    override fun getDefaultColor(): Int {
        return scrubberColor
    }

    override fun addOnPreviewChangeListener(listener: OnPreviewChangeListener) {
        listeners.add(listener)
    }

    override fun removeOnPreviewChangeListener(listener: OnPreviewChangeListener?) {
        listeners.remove(listener)
    }

    override fun onScrubStart(timeBar: TimeBar, position: Long) {
        for (listener in listeners) {
            scrubProgress = position.toInt()
            listener?.onStartPreview(this, position.toInt())
        }
    }

    override fun onScrubMove(timeBar: TimeBar, position: Long) {
        for (listener in listeners) {
            scrubProgress = position.toInt()
            listener?.onPreview(this, position.toInt(), true)
        }
    }

    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
        for (listener in listeners) {
            listener?.onStopPreview(this, position.toInt())
        }
    }

    private fun dpToPx(displayMetrics: DisplayMetrics, dp: Int): Int {
        return (dp * displayMetrics.density + 0.5f).toInt()
    }

}
