package com.uiza.sdk.view

import android.content.Context
import android.view.View
import androidx.annotation.UiThread
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener
import com.uiza.sdk.chromecast.Casty.OnConnectChangeListener
import com.uiza.sdk.exceptions.ErrorConstant
import com.uiza.sdk.listerner.UZChromeCastListener
import com.uiza.sdk.utils.UZAppUtils
import com.uiza.sdk.utils.UZData
import com.uiza.sdk.utils.UZViewUtils
import com.uiza.sdk.widget.UZMediaRouteButton

//chromecast https://github.com/DroidsOnRoids/Casty
class UZChromeCast {
    var mediaRouteButton: UZMediaRouteButton? = null
        private set
    private var listener: UZChromeCastListener? = null

    companion object {
        init {
            if (!UZAppUtils.checkChromeCastAvailable()) {
                throw NoClassDefFoundError(ErrorConstant.ERR_505)
            }
        }
    }

    fun setUZChromeCastListener(listener: UZChromeCastListener?) {
        this.listener = listener
    }

    fun setupChromeCast(context: Context) {
        if (UZAppUtils.isTV(context)) {
            return
        }
        mediaRouteButton = UZMediaRouteButton(context)
        setUpMediaRouteButton()
        addUIChromecastLayer(context)
    }

    @UiThread
    private fun setUpMediaRouteButton() {
        UZData.casty?.let {
            mediaRouteButton?.let { bt ->
                it.setUpMediaRouteButton(bt)
            }
            it.setOnConnectChangeListener(object : OnConnectChangeListener {
                override fun onConnected() {
                    listener?.onConnected()
                }

                override fun onDisconnected() {
                    listener?.onDisconnected()
                }
            })
        }
    }

    private fun updateMediaRouteButtonVisibility(state: Int) {
        UZViewUtils.setVisibilityViews(
            if (state == CastState.NO_DEVICES_AVAILABLE) {
                View.GONE
            } else {
                View.VISIBLE
            },
            mediaRouteButton
        )
    }

    //tự tạo layout chromecast và background đen
    //Gen layout chromecast with black background programmatically
    private fun addUIChromecastLayer(context: Context) {
        //listener check state of chromecast
        var castContext: CastContext? = null
        try {
            castContext = CastContext.getSharedInstance(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (castContext == null) {
            UZViewUtils.goneViews(mediaRouteButton)
            return
        }
        updateMediaRouteButtonVisibility(castContext.castState)
        castContext.addCastStateListener(CastStateListener { state: Int ->
            updateMediaRouteButtonVisibility(state)
        })
        listener?.addUIChromeCast()
    }

    fun setTintMediaRouteButton(color: Int) {
        mediaRouteButton?.let {
            it.post {
                it.applyTint(color)
            }
        }
    }
}
