package com.uiza.sdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.uiza.sdk.R
import com.uiza.sdk.utils.UZViewUtils.screenHeight
import com.uiza.sdk.utils.UZViewUtils.screenWidth
import com.uiza.sdk.view.UZPlayerView.*
import kotlin.math.abs
import kotlin.math.min

class UZDragView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    private var headerView: View? = null
    private var bodyView: View? = null
    private var mViewDragHelper: ViewDragHelper? = null
    private var mAutoBackViewX = 0
    private var mAutoBackViewY = 0
    private var mDragRange = 0
    private var mDragOffset = 0f
    private var isEnableRevertMaxSize = true

    //header view is scaled at least 1
    var isMinimizedAtLeastOneTime = false
        private set
    private var sizeWHeaderViewOriginal = 0
    private var sizeHHeaderViewOriginal = 0
    private var sizeWHeaderViewMin = 0
    private var sizeHHeaderViewMin = 0
    private var newSizeWHeaderView = 0
    private var newSizeHHeaderView = 0
    private var mCenterY = 0
    private var mCenterX = 0
    private var screenW = 0
    private var screenH = 0
    var isMaximizeView = true
        private set
    private var callback: Callback? = null
    var state: State? = State.NULL
        private set
    private var part: Part? = null
    private var mDetector: GestureDetectorCompat? = null
    private var onTouchEvent: OnTouchEvent? = null
    private var onSingleTap: OnSingleTap? = null
    private var onDoubleTap: OnDoubleTap? = null
    private var onLongPressed: OnLongPressed? = null
    var isAppear = true
        private set
    private var isEnableSlide = false
    private var isInitSuccess = false
    private var isLandscape = false
    private var isControllerShowing = false

    private val mCallback: ViewDragHelper.Callback = object : ViewDragHelper.Callback() {
        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            mDragOffset =
                if (mDragOffset == top.toFloat() / mDragRange) {
                    return
                } else {
                    top.toFloat() / mDragRange
                }
            if (mDragOffset >= 1) {
                mDragOffset = 1f
                if (isMaximizeView) {
                    isMaximizeView = false
                    callback?.onViewSizeChange(false)
                }
            }
            if (mDragOffset <= 0) {
                mDragOffset = 0f
                if (!isMaximizeView && isEnableRevertMaxSize) {
                    isMaximizeView = true
                    callback?.onViewSizeChange(true)
                }
            }
            callback?.onViewPositionChanged(left = left, top = top, dragOffset = mDragOffset)

            bodyView?.let { bv ->
                headerView?.let { hv ->
                    val x = 0
                    val y = hv.height + top
                    bv.layout(x, y, x + bv.measuredWidth, y + bv.measuredHeight)
                    bv.alpha = 1 - mDragOffset / 2

                    if (isMinimizedAtLeastOneTime) {
                        if (isEnableRevertMaxSize) {
                            hv.pivotX = hv.width / 2f
                            hv.pivotY = hv.height.toFloat()
                            hv.scaleX = 1 - mDragOffset / 2
                            hv.scaleY = 1 - mDragOffset / 2
                        }
                    } else {
                        hv.pivotX = hv.width / 2f
                        hv.pivotY = hv.height.toFloat()
                        hv.scaleX = 1 - mDragOffset / 2
                        hv.scaleY = 1 - mDragOffset / 2
                    }

                    newSizeWHeaderView = (sizeWHeaderViewOriginal * hv.scaleX).toInt()
                    newSizeHHeaderView = (sizeHHeaderViewOriginal * hv.scaleY).toInt()
                    mCenterX = left + sizeWHeaderViewOriginal / 2
                    mCenterY =
                        top + newSizeHHeaderView / 2 + sizeHHeaderViewOriginal - newSizeHHeaderView

                    val halfHeaderWidth = hv.width / 2
                    when (mDragOffset) {
                        //top_left, top, top_right
                        0f -> {
                            changeState(
                                when {
                                    left <= -halfHeaderWidth -> {
                                        State.TOP_LEFT
                                    }
                                    left >= halfHeaderWidth -> {
                                        State.TOP_RIGHT
                                    }
                                    else -> {
                                        State.TOP
                                    }
                                }
                            )
                        }
                        //bottom_left, bottom, bottom_right
                        1f -> {
                            changeState(
                                when {
                                    left <= -halfHeaderWidth -> {
                                        State.BOTTOM_LEFT
                                    }
                                    left >= halfHeaderWidth -> {
                                        State.BOTTOM_RIGHT
                                    }
                                    else -> {
                                        State.BOTTOM
                                    }
                                }
                            )

                            isMinimizedAtLeastOneTime = true
                        }
                        //mid_left, mid, mid_right
                        else -> {
                            changeState(
                                when {
                                    left <= -halfHeaderWidth -> {
                                        State.MID_LEFT
                                    }
                                    left >= halfHeaderWidth -> {
                                        State.MID_RIGHT
                                    }
                                    else -> {
                                        State.MID
                                    }
                                }
                            )
                        }
                    }
                    if (mCenterY < screenH / 2) {
                        changePart(
                            if (mCenterX < screenW / 2) {
                                Part.TOP_LEFT
                            } else {
                                Part.TOP_RIGHT
                            }
                        )
                    } else {
                        changePart(
                            if (mCenterX < screenW / 2) {
                                Part.BOTTOM_LEFT
                            } else {
                                Part.BOTTOM_RIGHT
                            }
                        )
                    }
                }
            }

        }

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return headerView === child
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val minY = if (isEnableRevertMaxSize) {
                -child.height / 2
            } else {
                -sizeHHeaderViewMin * 3 / 2
            }
            val scaledY = child.scaleY
            val sizeHScaled = (scaledY * child.height).toInt()
            val maxY = height - sizeHScaled * 3 / 2
            return if (top <= minY) {
                minY
            } else {
                min(top, maxY)
            }
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val minX = -child.width / 2
            val maxX = child.width / 2
            return if (left <= minX) {
                minX
            } else {
                min(left, maxX)
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (releasedChild === headerView) {
                mViewDragHelper?.settleCapturedViewAt(mAutoBackViewX, mAutoBackViewY)
            }
            invalidate()
        }
    }

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initView()
    }

    private fun initView() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mCallback)
        mViewDragHelper?.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT)
        mDetector = GestureDetectorCompat(context, UZGestureListener())
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        screenW = screenWidth
        screenH = screenHeight
        headerView = findViewById(R.id.headerView)
        bodyView = findViewById(R.id.svBodyView)

        headerView?.let { hv ->
            hv.post {
                sizeWHeaderViewOriginal = hv.measuredWidth
                sizeHHeaderViewOriginal = hv.measuredHeight
                sizeWHeaderViewMin = sizeWHeaderViewOriginal / 2
                sizeHHeaderViewMin = sizeHHeaderViewOriginal / 2
            }
        }
        val v = findFirstVideoView()
        setOnSingleTap(object : OnSingleTap {
            override fun onSingleTapConfirmed(x: Float, y: Float) {
                if (isMaximizeView) {
                    post {
                        v?.toggleShowHideController()
                    }
                }
            }
        })
    }

    private fun findFirstVideoView(): UZVideoView? {
        if (headerView is ViewGroup) {
            val hg = headerView as ViewGroup
            for (i in 0 until hg.childCount) {
                val v = hg.getChildAt(i)
                if (v is UZVideoView) {
                    return v
                }
            }
        }
        return null
    }

    private fun changeState(newState: State) {
        if (state != newState) {
            state = newState
            if (state == State.BOTTOM || state == State.BOTTOM_LEFT || state == State.BOTTOM_RIGHT) {
                setEnableRevertMaxSize(false)
            }
            callback?.onStateChange(state)
        }
    }

    private fun changePart(newPart: Part) {
        if (part != newPart) {
            part = newPart
            callback?.onPartChange(part)
        }
    }

    override fun computeScroll() {
        if (mViewDragHelper?.continueSettling(true) == true) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return mViewDragHelper?.shouldInterceptTouchEvent(event) ?: false
    }

    private fun isTouchInSideHeaderView(touchX: Float, touchY: Float): Boolean {
        if (isMaximizeView) {
            return true
        }
        val d2 = newSizeWHeaderView / 2f
        val r2 = newSizeHHeaderView / 2f
        val topLeftX = mCenterX - d2
        val topLeftY = mCenterY - r2
        val topRightX = mCenterX + d2
        val bottomLeftY = mCenterY + r2
        return if (touchX < topLeftX || touchX > topRightX) {
            false
        } else {
            touchY in topLeftY..bottomLeftY
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEnableSlide) {
            if (isTouchInSideHeaderView(touchX = event.x, touchY = event.y)) {
                mViewDragHelper?.processTouchEvent(event)
            } else {
                mViewDragHelper?.cancel()
                return false
            }
        } else {
            mViewDragHelper?.cancel()
        }
        mDetector?.onTouchEvent(event)
        val x = event.x
        val y = event.y
        val isViewUnder = mViewDragHelper?.isViewUnder(headerView, x.toInt(), y.toInt()) ?: false
        if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
            if (state == null) {
                return isViewUnder
            }
            if (state == State.TOP_LEFT || state == State.TOP_RIGHT || state == State.BOTTOM_LEFT || state == State.BOTTOM_RIGHT) {
                callback?.onOverScroll(state = state, part = part)
            } else {
                if (part == Part.BOTTOM_LEFT) {
                    minimizeBottomLeft()
                } else if (part == Part.BOTTOM_RIGHT) {
                    minimizeBottomRight()
                } else if (part == Part.TOP_LEFT) {
                    if (isEnableRevertMaxSize) {
                        maximize()
                    } else if (isMinimizedAtLeastOneTime) {
                        minimizeTopLeft()
                    }
                } else if (part == Part.TOP_RIGHT) {
                    if (isEnableRevertMaxSize) {
                        maximize()
                    } else if (isMinimizedAtLeastOneTime) {
                        minimizeTopRight()
                    }
                }
            }
        }
        return isViewUnder
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (state == State.BOTTOM || state == State.BOTTOM_RIGHT || state == State.BOTTOM_LEFT || state == State.TOP_RIGHT || state == State.TOP_LEFT) {
            return
        }
        super.onLayout(changed, l, t, r, b)
        headerView?.let { hv ->
            mDragRange = height - hv.height
            mAutoBackViewX = hv.left
            mAutoBackViewY = hv.top
        }
    }

    private fun maximize() {
        if (isEnableRevertMaxSize) smoothSlideTo(
            0,
            0
        ) else {
            log("Error: cannot maximize because isEnableRevertMaxSize is true")
        }
    }

    fun minimizeBottomLeft() {
        if (!isAppear) return
        val posX = width - sizeWHeaderViewOriginal - sizeWHeaderViewMin / 2
        val posY = height - sizeHHeaderViewOriginal
        smoothSlideTo(posX, posY)
    }

    fun minimizeBottomRight() {
        if (!isAppear) return
        val posX = width - sizeWHeaderViewOriginal + sizeWHeaderViewMin / 2
        val posY = height - sizeHHeaderViewOriginal
        smoothSlideTo(posX, posY)
    }

    fun minimizeTopRight() {
        if (!isAppear) return
        if (isEnableRevertMaxSize) {
            log("Error: cannot minimizeTopRight because isEnableRevertMaxSize is true")
            return
        }
        if (!isMinimizedAtLeastOneTime) {
            log("Error: cannot minimizeTopRight because isMinimizedAtLeastOneTime is false. This function only works if the header view is scrolled BOTTOM")
            return
        }
        val posX = screenW - sizeWHeaderViewMin * 3 / 2
        val posY = -sizeHHeaderViewMin
        smoothSlideTo(posX, posY)
    }

    fun minimizeTopLeft() {
        if (!isAppear) return
        if (isEnableRevertMaxSize) {
            log("Error: cannot minimizeTopRight because isEnableRevertMaxSize is true")
            return
        }
        if (!isMinimizedAtLeastOneTime) {
            log("Error: cannot minimizeTopRight because isMinimizedAtLeastOneTime is false. This function only works if the header view is scrolled BOTTOM")
            return
        }
        val posX = -sizeWHeaderViewMin / 2
        val posY = -sizeHHeaderViewMin
        smoothSlideTo(positionX = posX, positionY = posY)
    }

    fun smoothSlideTo(positionX: Int, positionY: Int) {
        if (!isAppear) {
            return
        }
        headerView?.let { hv ->
            if (mViewDragHelper?.smoothSlideViewTo(hv, positionX, positionY) == true) {
                ViewCompat.postInvalidateOnAnimation(this)
                postInvalidate()
            }
        }
    }

    fun isEnableRevertMaxSize(): Boolean {
        return isEnableRevertMaxSize
    }

    private fun setEnableRevertMaxSize(enableRevertMaxSize: Boolean) {
        isEnableRevertMaxSize = enableRevertMaxSize
        setVisibilityBodyView(
            if (isEnableRevertMaxSize) {
                VISIBLE
            } else {
                INVISIBLE
            }
        )
        callback?.onEnableRevertMaxSize(isEnableRevertMaxSize)
    }

    private fun setVisibilityBodyView(visibilityBodyView: Int) {
        bodyView?.visibility = visibilityBodyView
    }

    fun onPause() {
        if (!isEnableRevertMaxSize) {
            minimizeBottomRight()
            headerView?.visibility = INVISIBLE
            setVisibilityBodyView(INVISIBLE)
        }
    }

    fun disappear() {
        headerView?.visibility = GONE
        setVisibilityBodyView(GONE)
        isAppear = false
        callback?.onAppear(false)
    }

    fun appear() {
        headerView?.let { hv ->
            hv.visibility = VISIBLE
            setVisibilityBodyView(VISIBLE)
            if (!isEnableRevertMaxSize) {
                hv.scaleX = 1f
                hv.scaleY = 1f
                isEnableRevertMaxSize = true
            }
            isAppear = true
            callback?.onAppear(true)
        }
    }

    //private State stateBeforeDisappear;
    private fun setEnableSlide(isEnableSlide: Boolean) {
        if (isInitSuccess) {
            this.isEnableSlide = isEnableSlide
        }
    }

    fun setInitResult(isInitSuccess: Boolean) {
        this.isInitSuccess = isInitSuccess
        if (isInitSuccess) {
            setEnableSlide(true)
        }
    }

    fun setScreenRotate(isLandscape: Boolean) {
        this.isLandscape = isLandscape
        if (isControllerShowing) {
            setEnableSlide(false)
        } else {
            setEnableSlide(!isLandscape)
        }
    }

    fun setVisibilityChange(isShow: Boolean) {
        isControllerShowing = isShow
        if (isLandscape) {
            setEnableSlide(false)
        } else {
            setEnableSlide(!isShow)
        }
    }

    fun setOnTouchEvent(onTouchEvent: OnTouchEvent?) {
        this.onTouchEvent = onTouchEvent
    }

    private fun setOnSingleTap(onSingleTap: OnSingleTap) {
        this.onSingleTap = onSingleTap
    }

    private fun setOnDoubleTap(onDoubleTap: OnDoubleTap) {
        this.onDoubleTap = onDoubleTap
    }

    fun setOnLongPressed(onLongPressed: OnLongPressed?) {
        this.onLongPressed = onLongPressed
    }

    enum class State {
        TOP, TOP_LEFT, TOP_RIGHT, BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT, MID, MID_LEFT, MID_RIGHT, NULL
    }

    enum class Part {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    interface Callback {
        fun onViewSizeChange(isMaximizeView: Boolean) {}
        fun onStateChange(state: State?) {}
        fun onPartChange(part: Part?) {}
        fun onViewPositionChanged(left: Int, top: Int, dragOffset: Float) {}
        fun onOverScroll(state: State?, part: Part?) {}
        fun onEnableRevertMaxSize(isEnableRevertMaxSize: Boolean) {}
        fun onAppear(isAppear: Boolean) {}
    }

    private inner class UZGestureListener : SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            onDoubleTap?.onDoubleTapProgressDown(event.x, event.y)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onDoubleTap?.onDoubleTapProgressUp(e.x, e.y)
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (!isEnableRevertMaxSize) {
                setEnableRevertMaxSize(true)
            }
            maximize()
            onSingleTap?.onSingleTapConfirmed(e.x, e.y)
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            onLongPressed?.onLongPressed(e.x, e.y)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (onDoubleTap != null) {
                onDoubleTap?.onDoubleTapStarted(e.x, e.y)
                return true
            }
            return false
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            // Second tap (ACTION_UP) of both taps
            if (e.actionMasked == MotionEvent.ACTION_UP && onDoubleTap != null) {
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
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
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
}
