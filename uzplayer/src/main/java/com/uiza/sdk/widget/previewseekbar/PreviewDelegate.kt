package com.uiza.sdk.widget.previewseekbar

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.uiza.sdk.R
import com.uiza.sdk.widget.previewseekbar.PreviewView.OnPreviewChangeListener

class PreviewDelegate(
    private val previewView: PreviewView,
    scrubberColor: Int
) : OnPreviewChangeListener {
    private var morphView: View? = null
    private var previewFrameView: View? = null
    private var previewParent: ViewGroup? = null
    private var animator: PreviewAnimator? = null
    private var previewLoader: PreviewLoader? = null
    private val scrubberColor: Int
    var isShowing = false
        private set
    private var startTouch = false
    var isSetup = false
        private set
    private var enabled = false
    private val alwaysHide = false

    init {
        previewView.addOnPreviewChangeListener(this)
        this.scrubberColor = scrubberColor
    }

    fun setPreviewLoader(previewLoader: PreviewLoader?) {
        this.previewLoader = previewLoader
    }

    fun onLayout(previewParent: ViewGroup?, frameLayoutId: Int) {
        if (!isSetup) {
            this.previewParent = previewParent
            val frameLayout = findFrameLayout(previewParent, frameLayoutId)
            frameLayout?.let {
                attachPreviewFrameLayout(it)
            }
        }
    }

    fun attachPreviewFrameLayout(frameLayout: FrameLayout) {
        if (isSetup) {
            return
        }
        previewParent = frameLayout.parent as ViewGroup
        inflateViews(frameLayout)
        morphView?.visibility = View.INVISIBLE
        frameLayout.visibility = View.INVISIBLE
        previewFrameView?.visibility = View.INVISIBLE

        previewParent?.let { pp ->
            morphView?.let { mv ->
                previewFrameView?.let { pfv ->
                    animator =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            PreviewAnimatorLollipopImpl(
                                parent = pp,
                                previewView = previewView,
                                morphView = mv,
                                previewFrameLayout = frameLayout,
                                previewFrameView = pfv
                            )
                        } else {
                            PreviewAnimatorImpl(
                                parent = pp,
                                previewView = previewView,
                                morphView = mv,
                                previewFrameLayout = frameLayout,
                                previewFrameView = pfv
                            )
                        }
                }
            }
        }
        isSetup = true
    }

    fun show() {
        if (!isShowing && isSetup) {
            animator?.show()
            isShowing = true
        }
    }

    fun hide() {
        if (isShowing) {
            animator?.hide()
            isShowing = false
        }
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun setPreviewColorTint(@ColorInt color: Int) {
        morphView?.let { mv ->
            val drawable = DrawableCompat.wrap(mv.background)
            DrawableCompat.setTint(drawable, color)
            mv.background = drawable
            previewFrameView?.setBackgroundColor(color)
        }
    }

    fun setPreviewColorResourceTint(@ColorRes color: Int) {
        previewParent?.context?.let {
            setPreviewColorTint(ContextCompat.getColor(it, color))
        }
    }

    override fun onStartPreview(previewView: PreviewView?, progress: Int) {
        startTouch = true
    }

    override fun onStopPreview(previewView: PreviewView?, progress: Int) {
        if (isShowing) {
            animator?.hide()
        }
        isShowing = false
        startTouch = false
    }

    override fun onPreview(previewView: PreviewView?, progress: Int, fromUser: Boolean) {
        if (isSetup && enabled) {
            animator?.move()
            if (!isShowing && !startTouch && fromUser) {
                show()
            }
            previewView?.let { pv ->
                previewLoader?.loadPreview(
                    currentPosition = progress.toLong(),
                    max = pv.max.toLong()
                )
            }
        }
        startTouch = false
    }

    private fun inflateViews(frameLayout: FrameLayout) {

        // Create morph view
        morphView = View(frameLayout.context)
        morphView?.setBackgroundResource(R.drawable.previewseekbar_morph)

        // Setup morph view
        val layoutParams = ViewGroup.LayoutParams(0, 0)
        layoutParams.width = frameLayout.resources
            .getDimensionPixelSize(R.dimen.previewseekbar_indicator_width)
        layoutParams.height = layoutParams.width
        previewParent?.addView(morphView, layoutParams)

        // Create frame view for the circular reveal
        previewFrameView = View(frameLayout.context)
        val frameLayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        frameLayout.addView(previewFrameView, frameLayoutParams)

        // Apply same color for the morph and frame views
        setPreviewColorTint(scrubberColor)
        frameLayout.requestLayout()
    }

    private fun findFrameLayout(parent: ViewGroup?, id: Int): FrameLayout? {
        if (id == View.NO_ID || parent == null) return null
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.id == id && child is FrameLayout) return child
        }
        return null
    }
}
