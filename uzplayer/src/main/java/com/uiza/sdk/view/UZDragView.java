package com.uiza.sdk.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.uiza.sdk.R;
import com.uiza.sdk.utils.UZViewUtils;

import timber.log.Timber;

public class UZDragView extends LinearLayout {
    private View headerView;
    private View bodyView;
    private ViewDragHelper mViewDragHelper;
    private int mAutoBackViewX;
    private int mAutoBackViewY;
    private int mDragRange;
    private float mDragOffset;
    private boolean isEnableRevertMaxSize = true;
    private boolean isMinimizedAtLeastOneTime;//header view is scaled at least 1
    private int sizeWHeaderViewOriginal;
    private int sizeHHeaderViewOriginal;
    private int sizeWHeaderViewMin;
    private int sizeHHeaderViewMin;
    private int newSizeWHeaderView;
    private int newSizeHHeaderView;
    private int mCenterY;
    private int mCenterX;
    private int screenW;
    private int screenH;
    private boolean maximizeView = true;
    private Callback callback;
    private State state = State.NULL;
    private Part part;
    private GestureDetectorCompat mDetector;
    private UZPlayerView.OnTouchEvent onTouchEvent;
    private UZPlayerView.OnSingleTap onSingleTap;
    private UZPlayerView.OnDoubleTap onDoubleTap;
    private UZPlayerView.OnLongPressed onLongPressed;
    private boolean isAppear = true;
    private boolean isEnableSlide;
    private boolean isInitSuccess;
    private boolean isLandscape;
    private boolean isControllerShowing;
    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (mDragOffset == (float) top / mDragRange) return;
            else
                mDragOffset = (float) top / mDragRange;
            if (mDragOffset >= 1) {
                mDragOffset = 1;
                if (maximizeView) {
                    maximizeView = false;
                    if (callback != null)
                        callback.onViewSizeChange(false);
                }
            }
            if (mDragOffset <= 0) {
                mDragOffset = 0;
                if (!maximizeView && isEnableRevertMaxSize) {
                    maximizeView = true;
                    if (callback != null)
                        callback.onViewSizeChange(true);
                }
            }
            if (callback != null)
                callback.onViewPositionChanged(left, top, mDragOffset);

            int x = 0;
            int y = headerView.getHeight() + top;
            bodyView.layout(x, y, x + bodyView.getMeasuredWidth(), y + bodyView.getMeasuredHeight());
            bodyView.setAlpha(1 - mDragOffset / 2);

            if (isMinimizedAtLeastOneTime) {
                if (isEnableRevertMaxSize) {
                    headerView.setPivotX(headerView.getWidth() / 2f);
                    headerView.setPivotY(headerView.getHeight());
                    headerView.setScaleX(1 - mDragOffset / 2);
                    headerView.setScaleY(1 - mDragOffset / 2);
                }
            } else {
                headerView.setPivotX(headerView.getWidth() / 2f);
                headerView.setPivotY(headerView.getHeight());
                headerView.setScaleX(1 - mDragOffset / 2);
                headerView.setScaleY(1 - mDragOffset / 2);
            }

            newSizeWHeaderView = (int) (sizeWHeaderViewOriginal * headerView.getScaleX());
            newSizeHHeaderView = (int) (sizeHHeaderViewOriginal * headerView.getScaleY());

            mCenterX = left + sizeWHeaderViewOriginal / 2;
            mCenterY = top + newSizeHHeaderView / 2 + sizeHHeaderViewOriginal - newSizeHHeaderView;
            int halfHeaderWidth = headerView.getWidth() / 2;
            if (mDragOffset == 0)
                //top_left, top, top_right
                changeState(left <= -halfHeaderWidth ? State.TOP_LEFT : left >= halfHeaderWidth ? State.TOP_RIGHT : State.TOP);
            else if (mDragOffset == 1) {
                //bottom_left, bottom, bottom_right
                changeState(left <= -halfHeaderWidth ? State.BOTTOM_LEFT : left >= halfHeaderWidth ? State.BOTTOM_RIGHT : State.BOTTOM);
                isMinimizedAtLeastOneTime = true;
            } else
                //mid_left, mid, mid_right
                changeState(left <= -halfHeaderWidth ? State.MID_LEFT : left >= halfHeaderWidth ? State.MID_RIGHT : State.MID);
            if (mCenterY < screenH / 2)
                changePart(mCenterX < screenW / 2 ? Part.TOP_LEFT : Part.TOP_RIGHT);
            else
                changePart(mCenterX < screenW / 2 ? Part.BOTTOM_LEFT : Part.BOTTOM_RIGHT);
        }

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return headerView == child;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            int minY = isEnableRevertMaxSize ? -child.getHeight() / 2 : -sizeHHeaderViewMin * 3 / 2;
            float scaledY = child.getScaleY();
            int sizeHScaled = (int) (scaledY * child.getHeight());
            int maxY = getHeight() - sizeHScaled * 3 / 2;
            if (top <= minY) return minY;
            else return Math.min(top, maxY);
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            int minX = -child.getWidth() / 2;
            int maxX = child.getWidth() / 2;
            if (left <= minX) return minX;
            else return Math.min(left, maxX);
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            if (releasedChild == headerView)
                mViewDragHelper.settleCapturedViewAt(mAutoBackViewX, mAutoBackViewY);
            invalidate();
        }
    };

    public UZDragView(@NonNull Context context) {
        this(context, null);
    }

    public UZDragView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UZDragView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initView();
    }

    private void initView() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mCallback);
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        mDetector = new GestureDetectorCompat(getContext(), new UZGestureListener());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        screenW = UZViewUtils.getScreenWidth();
        screenH = UZViewUtils.getScreenHeight();
        headerView = findViewById(R.id.header_view);
        bodyView = findViewById(R.id.svBodyView);
        headerView.post(() -> {
            sizeWHeaderViewOriginal = headerView.getMeasuredWidth();
            sizeHHeaderViewOriginal = headerView.getMeasuredHeight();
            sizeWHeaderViewMin = sizeWHeaderViewOriginal / 2;
            sizeHHeaderViewMin = sizeHHeaderViewOriginal / 2;
        });
        UZVideoView v = findFirstVideoView();
        if (v != null) {
            setOnSingleTap((x, y) -> {
                if (maximizeView) {
                    post(v::toggleShowHideController);
                }
            });
        }
    }

    private UZVideoView findFirstVideoView() {
        if (headerView instanceof ViewGroup) {
            ViewGroup hg = (ViewGroup) headerView;
            for (int i = 0; i < hg.getChildCount(); i++) {
                View v = hg.getChildAt(i);
                if (v instanceof UZVideoView) {
                    return (UZVideoView) v;
                }
            }
        }
        return null;
    }

    private void changeState(State newState) {
        if (state != newState) {
            state = newState;
            if (state == UZDragView.State.BOTTOM || state == UZDragView.State.BOTTOM_LEFT || state == UZDragView.State.BOTTOM_RIGHT)
                setEnableRevertMaxSize(false);
            if (callback != null)
                callback.onStateChange(state);
        }
    }

    private void changePart(Part newPart) {
        if (part != newPart) {
            part = newPart;
            if (callback != null)
                callback.onPartChange(part);
        }
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true))
            ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mViewDragHelper.shouldInterceptTouchEvent(event);
    }

    private boolean isTouchInSideHeaderView(float touchX, float touchY) {
        if (maximizeView)
            return true;
        float d2 = newSizeWHeaderView / 2f;
        float r2 = newSizeHHeaderView / 2f;
        float topLeftX = mCenterX - d2;
        float topLeftY = mCenterY - r2;
        float topRightX = mCenterX + d2;
        float bottomLeftY = mCenterY + r2;
        if (touchX < topLeftX || touchX > topRightX)
            return false;
        else
            return !(touchY < topLeftY) && !(touchY > bottomLeftY);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnableSlide) {
            if (isTouchInSideHeaderView(event.getX(), event.getY()))
                mViewDragHelper.processTouchEvent(event);
            else {
                mViewDragHelper.cancel();
                return false;
            }
        } else
            mViewDragHelper.cancel();

        mDetector.onTouchEvent(event);
        final float x = event.getX();
        final float y = event.getY();
        boolean isViewUnder = mViewDragHelper.isViewUnder(headerView, (int) x, (int) y);
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            if (state == null) return isViewUnder;
            if (state == State.TOP_LEFT || state == State.TOP_RIGHT || state == State.BOTTOM_LEFT || state == State.BOTTOM_RIGHT) {
                if (callback != null)
                    callback.onOverScroll(state, part);
            } else {
                if (part == Part.BOTTOM_LEFT)
                    minimizeBottomLeft();
                else if (part == Part.BOTTOM_RIGHT)
                    minimizeBottomRight();
                else if (part == Part.TOP_LEFT) {
                    if (isEnableRevertMaxSize)
                        maximize();
                    else if (isMinimizedAtLeastOneTime)
                        minimizeTopLeft();
                } else if (part == Part.TOP_RIGHT) {
                    if (isEnableRevertMaxSize)
                        maximize();
                    else if (isMinimizedAtLeastOneTime)
                        minimizeTopRight();
                }
            }
        }
        return isViewUnder;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (state == State.BOTTOM ||
                state == State.BOTTOM_RIGHT ||
                state == State.BOTTOM_LEFT ||
                state == State.TOP_RIGHT ||
                state == State.TOP_LEFT)
            return;
        super.onLayout(changed, l, t, r, b);
        mDragRange = getHeight() - headerView.getHeight();
        mAutoBackViewX = headerView.getLeft();
        mAutoBackViewY = headerView.getTop();
    }

    public State getState() {
        return state;
    }

    public Part getPart() {
        return part;
    }

    private void maximize() {
        if (isEnableRevertMaxSize)
            smoothSlideTo(0, 0);
        else
            Timber.e("Error: cannot maximize because isEnableRevertMaxSize is true");
    }

    public void minimizeBottomLeft() {
        if (!isAppear) return;
        int posX = getWidth() - sizeWHeaderViewOriginal - sizeWHeaderViewMin / 2;
        int posY = getHeight() - sizeHHeaderViewOriginal;
        smoothSlideTo(posX, posY);
    }

    public void minimizeBottomRight() {
        if (!isAppear) return;
        int posX = getWidth() - sizeWHeaderViewOriginal + sizeWHeaderViewMin / 2;
        int posY = getHeight() - sizeHHeaderViewOriginal;
        smoothSlideTo(posX, posY);
    }

    public void minimizeTopRight() {
        if (!isAppear) return;
        if (isEnableRevertMaxSize) {
            Timber.w("Error: cannot minimizeTopRight because isEnableRevertMaxSize is true");
            return;
        }
        if (!isMinimizedAtLeastOneTime) {
            Timber.w("Error: cannot minimizeTopRight because isMinimizedAtLeastOneTime is false. This function only works if the header view is scrolled BOTTOM");
            return;
        }
        int posX = screenW - sizeWHeaderViewMin * 3 / 2;
        int posY = -sizeHHeaderViewMin;
        smoothSlideTo(posX, posY);
    }

    public void minimizeTopLeft() {
        if (!isAppear) return;
        if (isEnableRevertMaxSize) {
            Timber.w("Error: cannot minimizeTopRight because isEnableRevertMaxSize is true");
            return;
        }
        if (!isMinimizedAtLeastOneTime) {
            Timber.w("Error: cannot minimizeTopRight because isMinimizedAtLeastOneTime is false. This function only works if the header view is scrolled BOTTOM");
            return;
        }
        int posX = -sizeWHeaderViewMin / 2;
        int posY = -sizeHHeaderViewMin;
        smoothSlideTo(posX, posY);
    }

    public void smoothSlideTo(int positionX, int positionY) {
        if (!isAppear) return;
        if (mViewDragHelper.smoothSlideViewTo(headerView, positionX, positionY)) {
            ViewCompat.postInvalidateOnAnimation(this);
            postInvalidate();
        }
    }

    public boolean isMinimizedAtLeastOneTime() {
        return isMinimizedAtLeastOneTime;
    }

    public boolean isEnableRevertMaxSize() {
        return isEnableRevertMaxSize;
    }

    private void setEnableRevertMaxSize(boolean enableRevertMaxSize) {
        this.isEnableRevertMaxSize = enableRevertMaxSize;
        setVisibilityBodyView(isEnableRevertMaxSize ? VISIBLE : INVISIBLE);
        if (callback != null)
            callback.onEnableRevertMaxSize(isEnableRevertMaxSize);
    }

    private void setVisibilityBodyView(int visibilityBodyView) {
        bodyView.setVisibility(visibilityBodyView);
    }

    public void onPause() {
        if (!isEnableRevertMaxSize) {
            minimizeBottomRight();
            headerView.setVisibility(INVISIBLE);
            setVisibilityBodyView(INVISIBLE);
        }
    }

    public void disappear() {
        headerView.setVisibility(GONE);
        setVisibilityBodyView(GONE);
        isAppear = false;
        if (callback != null)
            callback.onAppear(false);
    }

    public boolean isAppear() {
        return isAppear;
    }

    public boolean isMaximizeView() {
        return maximizeView;
    }

    public void appear() {
        headerView.setVisibility(VISIBLE);
        setVisibilityBodyView(VISIBLE);
        if (!isEnableRevertMaxSize) {
            headerView.setScaleX(1f);
            headerView.setScaleY(1f);
            isEnableRevertMaxSize = true;
        }
        isAppear = true;
        if (callback != null)
            callback.onAppear(true);
    }

    //private State stateBeforeDisappear;

    private void setEnableSlide(boolean isEnableSlide) {
        if (isInitSuccess)
            this.isEnableSlide = isEnableSlide;

    }

    public void setInitResult(boolean isInitSuccess) {
        this.isInitSuccess = isInitSuccess;
        if (isInitSuccess)
            setEnableSlide(true);
    }

    public void setScreenRotate(boolean isLandscape) {
        this.isLandscape = isLandscape;
        if (isControllerShowing)
            setEnableSlide(false);
        else
            setEnableSlide(!isLandscape);
    }

    public void setVisibilityChange(boolean isShow) {
        this.isControllerShowing = isShow;
        if (isLandscape)
            setEnableSlide(false);
        else
            setEnableSlide(!isShow);
    }

    public void setOnTouchEvent(UZPlayerView.OnTouchEvent onTouchEvent) {
        this.onTouchEvent = onTouchEvent;
    }

    private void setOnSingleTap(UZPlayerView.OnSingleTap onSingleTap) {
        this.onSingleTap = onSingleTap;
    }

    private void setOnDoubleTap(UZPlayerView.OnDoubleTap onDoubleTap) {
        this.onDoubleTap = onDoubleTap;
    }

    public void setOnLongPressed(UZPlayerView.OnLongPressed onLongPressed) {
        this.onLongPressed = onLongPressed;
    }

    public enum State {TOP, TOP_LEFT, TOP_RIGHT, BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT, MID, MID_LEFT, MID_RIGHT, NULL}

    public enum Part {TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT}

    public interface Callback {
        default void onViewSizeChange(boolean isMaximizeView) {
        }

        default void onStateChange(State state) {
        }

        default void onPartChange(Part part) {
        }

        default void onViewPositionChanged(int left, int top, float dragOffset) {
        }

        default void onOverScroll(State state, Part part) {
        }

        default void onEnableRevertMaxSize(boolean isEnableRevertMaxSize) {
        }

        default void onAppear(boolean isAppear) {
        }
    }

    private class UZGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent event) {
            if (onDoubleTap != null) {
                onDoubleTap.onDoubleTapProgressDown(event.getX(), event.getY());
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (onDoubleTap != null) {
                onDoubleTap.onDoubleTapProgressUp(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!isEnableRevertMaxSize)
                setEnableRevertMaxSize(true);
            maximize();
            if (onSingleTap != null)
                onSingleTap.onSingleTapConfirmed(e.getX(), e.getY());
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (onLongPressed != null)
                onLongPressed.onLongPressed(e.getX(), e.getY());
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (onDoubleTap != null) {
                onDoubleTap.onDoubleTapStarted(e.getX(), e.getY());
                return true;
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            // Second tap (ACTION_UP) of both taps
            if (e.getActionMasked() == MotionEvent.ACTION_UP && onDoubleTap != null) {
                onDoubleTap.onDoubleTapProgressUp(e.getX(), e.getY());
                return true;
            }
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (onTouchEvent != null) {
                            if (diffX > 0)
                                onTouchEvent.onSwipeRight();
                            else
                                onTouchEvent.onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (onTouchEvent != null) {
                            if (diffY > 0)
                                onTouchEvent.onSwipeBottom();
                            else
                                onTouchEvent.onSwipeTop();
                        }
                    }
                }
            } catch (Exception exception) {
                Timber.e(exception);
            }
            return true;
        }
    }
}
