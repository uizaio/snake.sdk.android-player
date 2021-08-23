package com.uiza.sdk.view

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.uiza.sdk.listerner.UZBufferListener

internal class UZLoadControl private constructor(
    private val bufferCallback: UZBufferListener?
) :
    DefaultLoadControl(
        DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),  /* minBufferAudioMs= */
        DEFAULT_MIN_BUFFER_MS,  /* minBufferVideoMs= */
        DEFAULT_MAX_BUFFER_MS,
        DEFAULT_MAX_BUFFER_MS,
        DEFAULT_BUFFER_FOR_PLAYBACK_MS,
        DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
        DEFAULT_TARGET_BUFFER_BYTES,
        DEFAULT_PRIORITIZE_TIME_OVER_SIZE_THRESHOLDS,
        DEFAULT_BACK_BUFFER_DURATION_MS,
        DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME
    ) {

    override fun shouldContinueLoading(bufferedDurationUs: Long, playbackSpeed: Float): Boolean {
        bufferCallback?.onBufferChanged(bufferedDurationUs, playbackSpeed)
        return super.shouldContinueLoading(bufferedDurationUs, playbackSpeed)
    }

    companion object {
        /**
         * The default maximum duration of media that the player will attempt to buffer, in milliseconds.
         * For playbacks with video, this is also the default minimum duration of media that the player
         * will attempt to ensure is buffered.
         */
        private const val DEFAULT_MAX_BUFFER_MS = 60000

        /**
         * The default duration of media that must be buffered for playback to start or resume following a
         * user action such as a seek, in milliseconds.
         */
        private const val DEFAULT_BUFFER_FOR_PLAYBACK_MS = 10000

        @JvmStatic
        fun createControl(listener: UZBufferListener?): UZLoadControl {
            return UZLoadControl(listener)
        }
    }

}
