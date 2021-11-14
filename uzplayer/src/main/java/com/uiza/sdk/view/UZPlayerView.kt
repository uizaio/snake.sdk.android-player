package com.uiza.sdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlin.math.abs

// I want to to show playback controls only when onTouch event is fired.
// How to prevent control buttons being showed up when on long pressing, dragging etc.?
class UZPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : StyledPlayerView(
    context,
    attrs,
    defStyleAttr
) {

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    private var controllerVisible = false
    private val mDetector: GestureDetectorCompat
    private var onTouchEvent: OnTouchEvent? = null
    private var onSingleTap: OnSingleTap? = null
    private var onDoubleTap: OnDoubleTap? = null
    private var onLongPressed: OnLongPressed? = null
    private var controllerStateCallback: ControllerStateCallback? = null
    private val doubleTapActivated = true

    // Variable to save current state
    private var isDoubleTap = false
    private var useUZDragView = false

    /**
     * Default time window in which the double tap is active
     * Resets if another tap occurred within the time window by calling
     * [UZPlayerView.keepInDoubleTapMode]
     */
    var doubleTapDelay: Long = 650
    private val mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable {
        isDoubleTap = false
        onDoubleTap?.onDoubleTapFinished()
    }

    init {
        if (!isInEditMode) {
            setControllerVisibilityListener {
                controllerVisible = it == VISIBLE
                controllerStateCallback?.onVisibilityChange(controllerVisible)
            }
        }
        mDetector = GestureDetectorCompat(context, UZGestureListener())
    }

    fun isControllerVisible(): Boolean {
        return controllerVisible
    }

    fun setControllerStateCallback(controllerStateCallback: ControllerStateCallback?) {
        this.controllerStateCallback = controllerStateCallback
    }

    fun toggleShowHideController() {
        if (controllerVisible) {
            hideController()
        } else {
            showController()
        }
    }

    fun setOnTouchEvent(onTouchEvent: OnTouchEvent?) {
        this.onTouchEvent = onTouchEvent
    }

    fun setOnSingleTap(onSingleTap: OnSingleTap?) {
        this.onSingleTap = onSingleTap
    }

    fun setOnDoubleTap(onDoubleTap: OnDoubleTap?) {
        this.onDoubleTap = onDoubleTap
    }

    /**
     * Resets the timeout to keep in double tap mode.
     *
     *
     * Called once in [OnDoubleTap.onDoubleTapStarted] Needs to be called
     * from outside if the double tap is customized / overridden to detect ongoing taps
     */
    fun keepInDoubleTapMode() {
        isDoubleTap = true
        mHandler.removeCallbacks(mRunnable)
        mHandler.postDelayed(mRunnable, doubleTapDelay)
    }

    /**
     * Cancels double tap mode instantly by calling [OnDoubleTap.onDoubleTapFinished]
     */
    fun cancelInDoubleTapMode() {
        mHandler.removeCallbacks(mRunnable)
        isDoubleTap = false
        onDoubleTap?.onDoubleTapFinished()
    }

    fun setOnLongPressed(onLongPressed: OnLongPressed?) {
        this.onLongPressed = onLongPressed
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (useUZDragView) {
            false
        } else {
            mDetector.onTouchEvent(ev)
        }
    }

    interface ControllerStateCallback {
        fun onVisibilityChange(visible: Boolean)
    }

    interface OnSingleTap {
        fun onSingleTapConfirmed(x: Float, y: Float)
    }

    interface OnLongPressed {
        fun onLongPressed(x: Float, y: Float)
    }

    interface OnDoubleTap {
        /**
         * Called when double tapping starts, after double tap gesture
         *
         * @param posX x tap position on the root view
         * @param posY y tap position on the root view
         */
        fun onDoubleTapStarted(posX: Float, posY: Float) {}

        /**
         * Called for each ongoing tap (also single tap) (MotionEvent#ACTION_DOWN)
         * when double tap started and still in double tap mode defined
         * by [UZPlayerView.doubleTapDelay]
         *
         * @param posX x tap position on the root view
         * @param posY y tap position on the root view
         */
        fun onDoubleTapProgressDown(posX: Float, posY: Float) {}

        /**
         * Called for each ongoing tap (also single tap) (MotionEvent#ACTION_UP}
         * when double tap started and still in double tap mode defined
         * by [UZPlayerView.doubleTapDelay]
         *
         * @param posX x tap position on the root view
         * @param posY y tap position on the root view
         */
        fun onDoubleTapProgressUp(posX: Float, posY: Float) {}

        /**
         * Called when [UZPlayerView.doubleTapDelay] is over
         */
        fun onDoubleTapFinished() {}
    }

    interface OnTouchEvent {
        fun onSwipeRight()
        fun onSwipeLeft()
        fun onSwipeBottom()
        fun onSwipeTop()
    }

    private inner class UZGestureListener : SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            if (isDoubleTap) {
                onDoubleTap?.onDoubleTapProgressDown(event.x, event.y)
            }
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (isDoubleTap) {
                onDoubleTap?.onDoubleTapProgressUp(e.x, e.y)
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (!controllerVisible) {
                showController()
            } else if (controllerHideOnTouch) {
                hideController()
            }
            onSingleTap?.onSingleTapConfirmed(e.x, e.y)
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            onLongPressed?.onLongPressed(e.x, e.y)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (!isDoubleTap) {
                isDoubleTap = true
                keepInDoubleTapMode()
                onDoubleTap?.onDoubleTapStarted(e.x, e.y)
                return true
            }
            return false
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            // Second tap (ACTION_UP) of both taps
            if (e.actionMasked == MotionEvent.ACTION_UP && isDoubleTap) {
                onDoubleTap?.onDoubleTapProgressUp(e.x, e.y)
                return true
            }
            return super.onDoubleTapEvent(e)
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onTouchEvent?.onSwipeRight()
                        } else {
                            onTouchEvent?.onSwipeLeft()
                        }
                    }
                } else {
                    if (abs(diffY) > Companion.SWIPE_THRESHOLD && abs(velocityY) > Companion.SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onTouchEvent?.onSwipeBottom()
                        } else {
                            onTouchEvent?.onSwipeTop()
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return true
        }
    }

    fun setUseUZDragView(useUZDragView: Boolean) {
        this.useUZDragView = useUZDragView
    }

    fun isUseUZDragView(): Boolean {
        return useUZDragView
    }
}
