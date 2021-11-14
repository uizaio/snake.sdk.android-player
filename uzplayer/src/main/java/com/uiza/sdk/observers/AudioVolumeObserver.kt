package com.uiza.sdk.observers

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.provider.Settings

class AudioVolumeObserver(private val mContext: Context, handler: Handler) {
    val handler: Handler
    private val mAudioManager: AudioManager?
    private val audioStreamType = AudioManager.STREAM_MUSIC
    private var mAudioVolumeContentObserver: AudioVolumeContentObserver? = null

    init {
        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        this.handler = handler
    }

    fun register(
        audioStreamType: Int,
        listener: OnAudioVolumeChangedListener
    ) {
        if (mAudioManager != null) {
            mAudioVolumeContentObserver = AudioVolumeContentObserver(
                handler = handler,
                audioManager = mAudioManager,
                audioStreamType = audioStreamType,
                listener = listener
            )
            mAudioVolumeContentObserver?.let {
                mContext.contentResolver.registerContentObserver(
                    Settings.System.CONTENT_URI,
                    true,
                    it
                )
            }
        }
    }

    fun unregister() {
        mAudioVolumeContentObserver?.let {
            mContext.contentResolver.unregisterContentObserver(it)
            mAudioVolumeContentObserver = null
        }
    }

    val currentVolume: Int?
        get() = mAudioManager?.getStreamVolume(audioStreamType)

    val maxVolume: Int?
        get() = mAudioManager?.getStreamMaxVolume(audioStreamType)
}
