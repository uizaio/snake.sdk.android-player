package com.uiza.sdk.widget.previewseekbar;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

class PreviewAnimatorImpl extends PreviewAnimator {

    public static final int ALPHA_DURATION = 200;

    private AnimatorListenerAdapter hideListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);

            getPreviewFrameLayout().setVisibility(View.INVISIBLE);
        }
    };

    public PreviewAnimatorImpl(ViewGroup parent, PreviewView previewView, View morphView,
                               FrameLayout previewFrameLayout, View previewFrameView) {
        super(parent, previewView, morphView, previewFrameLayout, previewFrameView);
    }

    @Override
    public void move() {
        getPreviewFrameLayout().setX(getFrameX());
    }

    @Override
    public void show() {
        move();
        getPreviewFrameLayout().setVisibility(View.VISIBLE);
        getPreviewFrameLayout().setAlpha(0f);
        getPreviewFrameLayout().animate().cancel();
        getPreviewFrameLayout().animate()
                .setDuration(ALPHA_DURATION)
                .alpha(1f)
                .setListener(null);
    }

    @Override
    public void hide() {
        getPreviewFrameLayout().setAlpha(1f);
        getPreviewFrameLayout().animate().cancel();
        getPreviewFrameLayout().animate()
                .setDuration(ALPHA_DURATION)
                .alpha(0f)
                .setListener(hideListener);
    }

}