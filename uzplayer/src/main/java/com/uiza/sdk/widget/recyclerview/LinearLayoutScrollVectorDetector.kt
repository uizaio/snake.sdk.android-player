package com.uiza.sdk.widget.recyclerview

import androidx.recyclerview.widget.LinearLayoutManager
import com.uiza.sdk.widget.recyclerview.SnappySmoothScroller.ScrollVectorDetector
import android.graphics.PointF

class LinearLayoutScrollVectorDetector(
    private val layoutManager: LinearLayoutManager
) : ScrollVectorDetector {

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        return layoutManager.computeScrollVectorForPosition(targetPosition)
    }
}
