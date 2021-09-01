package com.uiza.sdk.utils

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.uiza.sdk.R

object DebugUtils {

    //return button audio in debug layout
    @JvmStatic
    fun getAudioButton(debugRootView: LinearLayout): View? {
        for (i in 0 until debugRootView.childCount) {
            val childView = debugRootView.getChildAt(i)
            if (childView is Button) {
                if (childView.text.toString()
                        .equals(debugRootView.context.getString(R.string.audio), ignoreCase = true)
                ) {
                    return childView
                }
            }
        }
        return null
    }

    @JvmStatic
    fun getVideoButton(debugRootView: LinearLayout): View? {
        for (i in 0 until debugRootView.childCount) {
            val childView = debugRootView.getChildAt(i)
            if (childView is Button) {
                if (childView.text.toString()
                        .equals(debugRootView.context.getString(R.string.video), ignoreCase = true)
                ) {
                    return childView
                }
            }
        }
        return null
    }

    @JvmStatic
    fun getCaptionsButton(debugRootView: LinearLayout): View? {
        for (i in 0 until debugRootView.childCount) {
            val childView = debugRootView.getChildAt(i)
            if (childView is Button) {
                if (childView.text.toString()
                        .equals(debugRootView.context.getString(R.string.text), ignoreCase = true)
                ) {
                    return childView
                }
            }
        }
        return null
    }
}
