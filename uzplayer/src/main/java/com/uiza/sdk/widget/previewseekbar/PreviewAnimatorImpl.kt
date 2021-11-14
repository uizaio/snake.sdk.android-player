package com.uiza.sdk.widget.previewseekbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

internal class PreviewAnimatorImpl(
    parent: ViewGroup,
    previewView: PreviewView,
    morphView: View,
    previewFrameLayout: FrameLayout,
    previewFrameView: View
) : PreviewAnimator(
    parent,
    previewView,
    morphView,
    previewFrameLayout,
    previewFrameView
) {
    companion object {
        const val ALPHA_DURATION = 200
    }

    private val hideListener: AnimatorListenerAdapter = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            previewFrameLayout.visibility = View.INVISIBLE
        }
    }

    override fun move() {
        previewFrameLayout.x = frameX
    }

    override fun show() {
        move()
        previewFrameLayout.visibility = View.VISIBLE
        previewFrameLayout.alpha = 0f
        previewFrameLayout.animate().cancel()
        previewFrameLayout.animate()
            .setDuration(ALPHA_DURATION.toLong())
            .alpha(1f)
            .setListener(null)
    }

    override fun hide() {
        previewFrameLayout.alpha = 1f
        previewFrameLayout.animate().cancel()
        previewFrameLayout.animate()
            .setDuration(ALPHA_DURATION.toLong())
            .alpha(0f)
            .setListener(hideListener)
    }
}
