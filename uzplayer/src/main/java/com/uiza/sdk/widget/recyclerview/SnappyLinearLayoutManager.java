package com.uiza.sdk.widget.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SnappyLinearLayoutManager extends LinearLayoutManager implements SnappyLayoutManager {

    private SnappySmoothScroller.Builder builder;

    public SnappyLinearLayoutManager(Context context) {
        super(context);
        init();
    }

    public SnappyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        init();
    }

    public SnappyLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        builder = new SnappySmoothScroller.Builder();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        SnappySmoothScroller scroller = builder
                .setPosition(position)
                .setScrollVectorDetector(new LinearLayoutScrollVectorDetector(this))
                .build(recyclerView.getContext());

        startSmoothScroll(scroller);
    }

    @Override
    public void setSnapType(SnapType snapType) {
        builder.setSnapType(snapType);
    }

    @Override
    public void setSnapDuration(int snapDuration) {
        builder.setSnapDuration(snapDuration);
    }

    @Override
    public void setSnapInterpolator(Interpolator snapInterpolator) {
        builder.setSnapInterpolator(snapInterpolator);
    }

    @Override
    public void setSnapPadding(int snapPadding) {
        builder.setSnapPadding(snapPadding);
    }

    @Override
    public void setSnapPaddingStart(int snapPaddingStart) {
        builder.setSnapPaddingStart(snapPaddingStart);
    }

    @Override
    public void setSnapPaddingEnd(int snapPaddingEnd) {
        builder.setSnapPaddingEnd(snapPaddingEnd);
    }

    @Override
    public void setSeekDuration(int seekDuration) {
        builder.setSeekDuration(seekDuration);
    }
}
