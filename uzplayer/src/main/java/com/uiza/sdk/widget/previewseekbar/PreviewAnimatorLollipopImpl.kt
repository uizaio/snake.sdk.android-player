package com.uiza.sdk.widget.previewseekbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PreviewAnimatorLollipopImpl extends PreviewAnimator {

    static final int MORPH_REVEAL_DURATION = 150;
    static final int MORPH_MOVE_DURATION = 200;
    static final int UNMORPH_MOVE_DURATION = 200;
    static final int UNMORPH_UNREVEAL_DURATION = 150;
    private Animator.AnimatorListener hideListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);

            getMorphView().setVisibility(View.INVISIBLE);
            getMorphView().animate().setListener(null);
        }
    };
    private boolean mShowing;
    private Animator.AnimatorListener showListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            getMorphView().animate().setListener(null);
            startReveal();
            mShowing = false;
        }
    };

    PreviewAnimatorLollipopImpl(ViewGroup parent, PreviewView previewView, View morphView,
                                FrameLayout previewFrameLayout, View previewFrameView) {
        super(parent, previewView, morphView, previewFrameLayout, previewFrameView);
    }

    @Override
    public void move() {
        getPreviewFrameLayout().setX(getFrameX());
        getMorphView().animate().x(mShowing ? getMorphEndX() : getMorphStartX());
    }

    @Override
    public void show() {
        mShowing = true;
        move();
        getPreviewFrameLayout().setVisibility(View.INVISIBLE);
        getPreviewFrameView().setVisibility(View.INVISIBLE);
        getMorphView().setY(((View) getPreviewView()).getY());
        getMorphView().setX(getMorphStartX());
        getMorphView().setScaleX(0f);
        getMorphView().setScaleY(0f);
        getMorphView().setVisibility(View.VISIBLE);
        getMorphView().animate()
                .x(getMorphEndX())
                .y(getMorphEndY())
                .scaleY(4.0f)
                .scaleX(4.0f)
                .setDuration(MORPH_MOVE_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(showListener);
    }

    @Override
    public void hide() {
        mShowing = false;
        getPreviewFrameView().setVisibility(View.VISIBLE);
        getPreviewFrameLayout().setVisibility(View.VISIBLE);
        getMorphView().setX(getMorphEndX());
        getMorphView().setY(getMorphEndY());
        getMorphView().setScaleX(4.0f);
        getMorphView().setScaleY(4.0f);
        getMorphView().setVisibility(View.INVISIBLE);
        getMorphView().animate().cancel();
        getPreviewFrameLayout().animate().cancel();
        startUnreveal();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startReveal() {
        Animator animator = ViewAnimationUtils.createCircularReveal(getPreviewFrameLayout(),
                getCenterX(getPreviewFrameLayout()),
                getCenterY(getPreviewFrameLayout()),
                getMorphView().getWidth() * 2,
                getRadius(getPreviewFrameLayout()));

        animator.setTarget(getPreviewFrameLayout());
        animator.setDuration(MORPH_REVEAL_DURATION);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                getPreviewFrameView().setAlpha(1f);
                getPreviewFrameLayout().setVisibility(View.VISIBLE);
                getPreviewFrameView().setVisibility(View.VISIBLE);
                getMorphView().setVisibility(View.INVISIBLE);
                getPreviewFrameView().animate()
                        .alpha(0f)
                        .setDuration(MORPH_REVEAL_DURATION);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getPreviewFrameLayout().animate().setListener(null);
                getPreviewFrameView().setVisibility(View.INVISIBLE);
            }

        });

        animator.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startUnreveal() {
        Animator animator = ViewAnimationUtils.createCircularReveal(getPreviewFrameLayout(),
                getCenterX(getPreviewFrameLayout()),
                getCenterY(getPreviewFrameLayout()),
                getRadius(getPreviewFrameLayout()), getMorphView().getWidth() * 2);
        animator.setTarget(getPreviewFrameLayout());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getPreviewFrameLayout().animate().setListener(null);
                getPreviewFrameView().setVisibility(View.INVISIBLE);
                getPreviewFrameLayout().setVisibility(View.INVISIBLE);
                getMorphView().setVisibility(View.VISIBLE);
                getMorphView().setX(getMorphEndX());
                getMorphView().animate()
                        .x(getMorphStartX())
                        .y(getMorphStartY())
                        .scaleY(0f)
                        .scaleX(0f)
                        .setDuration(UNMORPH_MOVE_DURATION)
                        .setInterpolator(new AccelerateInterpolator())
                        .setListener(hideListener);
            }
        });
        getPreviewFrameView().animate().alpha(1f).setDuration(UNMORPH_UNREVEAL_DURATION)
                .setInterpolator(new AccelerateInterpolator());
        animator.setDuration(UNMORPH_UNREVEAL_DURATION)
                .setInterpolator(new AccelerateInterpolator());
        animator.start();
    }

    private int getRadius(View view) {
        return (int) Math.hypot((float) view.getWidth() / 2, (float) view.getHeight() / 2);
    }

    private int getCenterX(View view) {
        return view.getWidth() / 2;
    }

    private int getCenterY(View view) {
        return view.getHeight() / 2;
    }

    /**
     * Get the x position for the view that'll morph into the preview FrameLayout
     */
    private float getMorphStartX() {
        float startX = getPreviewViewStartX() + getPreviewView().getThumbOffset();
        float endX = getPreviewViewEndX() - getPreviewView().getThumbOffset();
        return (endX - startX) * getWidthOffset(getPreviewView().getProgress())
                + startX - getPreviewView().getThumbOffset();
    }

    private float getMorphEndX() {
        return getFrameX() + getPreviewFrameLayout().getWidth() / 2f - getPreviewView().getThumbOffset();
    }

    private float getMorphStartY() {
        return ((View) getPreviewView()).getY() + getPreviewView().getThumbOffset();
    }

    private float getMorphEndY() {
        return (int) (getPreviewFrameLayout().getY() + getPreviewFrameLayout().getHeight() / 2f);
    }

}
