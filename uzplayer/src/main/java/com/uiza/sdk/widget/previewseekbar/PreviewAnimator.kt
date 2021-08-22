package com.uiza.sdk.widget.previewseekbar

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import kotlin.math.min

internal abstract class PreviewAnimator(
    var parent: ViewGroup,
    var previewView: PreviewView,
    var morphView: View,
    var previewFrameLayout: FrameLayout,
    var previewFrameView: View
) {
    abstract fun move()
    abstract fun show()
    abstract fun hide()// Don't move if we still haven't reached half of the width

    /**
     * Get x position for the preview frame. This method takes into account a margin
     * that'll make the frame not move until the scrub position exceeds half of the frame's width.
     */
    val frameX: Float
        get() {
            val params = previewFrameLayout.layoutParams as MarginLayoutParams
            val offset = getWidthOffset(previewView.progress)
            val low = previewFrameLayout.left.toFloat()
            val high = (parent.width - params.rightMargin - previewFrameLayout.width).toFloat()
            val startX = previewViewStartX + previewView.thumbOffset
            val endX = previewViewEndX - previewView.thumbOffset
            val center = (endX - startX) * offset + startX
            val nextX = center - previewFrameLayout.width / 2f

            // Don't move if we still haven't reached half of the width
            return if (nextX < low) {
                low
            } else min(nextX, high)
        }
    val previewViewStartX: Float
        get() = (previewView as View).x

    val previewViewEndX: Float
        get() = previewViewStartX + (previewView as View).width

    fun getWidthOffset(progress: Int): Float {
        return progress.toFloat() / previewView.max
    }
}
