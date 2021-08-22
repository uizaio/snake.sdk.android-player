package com.uiza.sdk.widget.recyclerview

import android.view.animation.Interpolator

interface SnappyLayoutManager {
    fun setSnapType(snapType: SnapType)
    fun setSnapDuration(snapDuration: Int)
    fun setSnapInterpolator(snapInterpolator: Interpolator)
    fun setSnapPadding(snapPadding: Int)
    fun setSnapPaddingStart(snapPaddingStart: Int)
    fun setSnapPaddingEnd(snapPaddingEnd: Int)
    fun setSeekDuration(seekDuration: Int)
}
