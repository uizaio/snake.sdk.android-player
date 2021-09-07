package com.uiza.sdk.view

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.source.MediaSourceEventListener.LoadEventInfo
import com.google.android.exoplayer2.source.MediaSourceEventListener.MediaLoadData
import com.uiza.sdk.BuildConfig
import com.uiza.sdk.R
import com.uiza.sdk.observers.AudioVolumeObserver
import com.uiza.sdk.observers.OnAudioVolumeChangedListener
import com.uiza.sdk.utils.Constants
import com.uiza.sdk.utils.StringUtils.doubleFormatted
import com.uiza.sdk.utils.StringUtils.humanReadableByteCount
import com.uiza.sdk.utils.UZViewUtils.goneViews
import com.uiza.sdk.utils.UZViewUtils.visibleViews
import kotlinx.android.synthetic.main.layout_stats_for_nerds.view.*
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

class StatsForNerdsView : RelativeLayout, AnalyticsListener, OnAudioVolumeChangedListener {
    private var mHandler = Handler(Looper.getMainLooper())
    private var mBufferedDurationUs: Long = 0
    private var volumeObserver: AudioVolumeObserver? = null
    private var droppedFrames = 0
    private var surfaceWidth = 0
    private var surfaceHeight = 0
    private var optimalResWidth = 0
    private var optimalResHeight = 0
    private var currentResWidth = 0
    private var currentResHeight = 0
    private var audioDecodesStr = ""
    private var videoDecoderStr = ""
    private var audioInfo = ""
    private var videoInfo = ""

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private fun init() {
        inflate(context, R.layout.layout_stats_for_nerds, this)
        btnClose.setOnClickListener {
            goneViews(this@StatsForNerdsView)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        init()
        if (volumeObserver == null) {
            volumeObserver = AudioVolumeObserver(mContext = context, handler = mHandler)
        }
        volumeObserver?.register(AudioManager.STREAM_MUSIC, this)
        depictVersionInfo()
        depictDeviceInfo()
        depictViewPortFrameInfo()
        volumeObserver?.let {
            post {
                setTextVolume(
                    String.format(
                        Locale.US,
                        "%d / %d",
                        it.currentVolume,
                        it.maxVolume
                    )
                )
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        volumeObserver?.let {
            it.unregister()
            volumeObserver = null
        }
    }

    fun setBufferedDurationUs(bufferedDurationUs: Long) {
        mBufferedDurationUs = bufferedDurationUs
    }

    override fun onDecoderEnabled(
        eventTime: EventTime,
        trackType: Int,
        decoderCounters: DecoderCounters
    ) {
        if (trackType == C.TRACK_TYPE_VIDEO) {
            videoDecoderStr = getDecoderCountersBufferCountString(decoderCounters)
            depictVideoDetailInfo()
        } else if (trackType == C.TRACK_TYPE_AUDIO) {
            audioDecodesStr = getDecoderCountersBufferCountString(decoderCounters)
            depictAudioDetailInfo()
        }
    }

    override fun onDecoderInputFormatChanged(eventTime: EventTime, trackType: Int, format: Format) {
        if (trackType == C.TRACK_TYPE_AUDIO) {
            audioInfo = resources.getString(
                R.string.format_audio_format,
                format.sampleMimeType,
                format.sampleRate,
                format.channelCount
            )
            depictAudioDetailInfo()
        } else if (trackType == C.TRACK_TYPE_VIDEO) {
            videoInfo = resources.getString(
                R.string.format_video_format,
                format.sampleMimeType,
                format.width,
                format.height,
                format.frameRate.roundToInt(),
                getPixelAspectRatioString(format.pixelWidthHeightRatio)
            )
            depictVideoDetailInfo()
        }
    }

    private fun depictAudioDetailInfo() {
        setTextAudioFormat(audioInfo + audioDecodesStr)
    }

    private fun depictVideoDetailInfo() {
        setTextVideoFormat(videoInfo + videoDecoderStr)
    }

    override fun onDroppedVideoFrames(eventTime: EventTime, droppedFrames: Int, elapsedMs: Long) {
        this.droppedFrames += droppedFrames
        depictViewPortFrameInfo()
    }

    override fun onSurfaceSizeChanged(eventTime: EventTime, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        depictViewPortFrameInfo()
    }

    override fun onAudioVolumeChanged(currentVolume: Int, maxVolume: Int) {
        mHandler.post {
            setTextVolume(String.format(Locale.US, "%d / %d", currentVolume, maxVolume))
        }
    }

    override fun onBandwidthEstimate(
        eventTime: EventTime,
        totalLoadTimeMs: Int,
        totalBytesLoaded: Long,
        bitrateEstimate: Long
    ) {
        mHandler.post {
            val formattedValue: String = if (bitrateEstimate < 1e6) {
                resources.getString(
                    R.string.format_connection_speed_k,
                    doubleFormatted(
                        value = bitrateEstimate / 10.0.pow(3.0), precision = 2
                    )
                )
            } else {
                resources.getString(
                    R.string.format_connection_speed_m,
                    doubleFormatted(
                        value = bitrateEstimate / 10.0.pow(6.0), precision = 2
                    )
                )
            }
            setTextConnectionSpeed(formattedValue)
            setTextNetworkActivity(
                humanReadableByteCount(
                    bytes = totalBytesLoaded,
                    si = true,
                    isBits = false
                )
            )
            setTextBufferHealth(
                resources.getString(
                    R.string.format_buffer_health,
                    doubleFormatted(
                        value = eventTime.totalBufferedDurationMs / 10.0.pow(3.0), precision = 1
                    )
                )
            )
        }
    }

    override fun onVideoSizeChanged(
        eventTime: EventTime,
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float
    ) {
        if (width <= 0 || height <= 0) {
            return
        }
        if (width != currentResWidth && height != currentResHeight) {
            currentResWidth = width
            currentResHeight = height
            mHandler.post {
                setTextResolution(
                    resources.getString(
                        R.string.format_resolution,
                        currentResWidth, currentResHeight, optimalResWidth, optimalResHeight
                    )
                )
            }
        }
    }

    override fun onLoadCompleted(
        eventTime: EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        val downloadFormat = mediaLoadData.trackFormat
        if (downloadFormat != null && downloadFormat.width != -1 && downloadFormat.height != -1) {
            if (downloadFormat.width != optimalResWidth && downloadFormat.height != optimalResHeight) {
                optimalResWidth = downloadFormat.width
                optimalResHeight = downloadFormat.height
                mHandler.post {
                    setTextResolution(
                        resources.getString(
                            R.string.format_resolution,
                            currentResWidth, currentResHeight, optimalResWidth, optimalResHeight
                        )
                    )
                }
            }
        }
    }

    /**
     * Depict Viewport and dropped frames
     *
     *
     * EX: 806x453 / 0 dropped frames
     */
    private fun depictViewPortFrameInfo() {
        if (surfaceWidth == 0 && surfaceHeight == 0) {
            // at first time, surface view size or viewport equals to uzVideo size
            surfaceWidth = this.width
            surfaceHeight = this.height
        }
        mHandler.post {
            setTextViewPortFrame(
                resources.getString(
                    R.string.format_viewport_frame,
                    surfaceWidth,
                    surfaceHeight,
                    droppedFrames
                )
            )
        }
    }

    private fun depictVersionInfo() {
        setTextVersion(
            resources.getString(
                R.string.format_version,
                Constants.PLAYER_SDK_VERSION, BuildConfig.EXO_VERSION
            )
        )
    }

    private fun depictDeviceInfo() {
        setTextDeviceInfo(
            resources.getString(R.string.format_device_info, Build.MODEL, Build.VERSION.RELEASE)
        )
    }

    /**
     * Depict Buffer health
     *
     * @param value should be formatted like below
     * EX: 20 s
     */
    @SuppressLint("SetTextI18n")
    fun setTextBufferHealth(value: String?) {
        tvBufferHealth.text = "Buffer Health: $value"
    }

    /**
     * Depict Network Activity
     *
     * @param value should be formatted like below
     * EX: 5 kB or 5 MB
     */
    @SuppressLint("SetTextI18n")
    fun setTextNetworkActivity(value: String?) {
        tvNetworkActivity.text = "Network Activity: $value"
    }

    /**
     * Depict Volume
     *
     * @param value should be formatted like below
     * EX: 50%
     */
    @SuppressLint("SetTextI18n")
    fun setTextVolume(value: String?) {
        tvVolume.text = "Volume: $value"
    }

    /**
     * Depict Viewport and dropped frames
     *
     * @param value should be formatted like below
     * EX: 806x453 / 0 dropped frames
     */
    @SuppressLint("SetTextI18n")
    fun setTextViewPortFrame(value: String?) {
        tvViewPortFrame.text = "Viewport / Frames: $value"
    }

    /**
     * Depict connection speed
     *
     * @param value should be formatted like below
     * EX: 40 mbps or 40 kbps
     */
    @SuppressLint("SetTextI18n")
    fun setTextConnectionSpeed(value: String?) {
        tvConnectionSpeed.text = "Connection Speed: $value"
    }

    /**
     * Depict version
     *
     * @param value should be formatted like below
     * EX: SDK Version / Player Version / API Version
     */
    @SuppressLint("SetTextI18n")
    private fun setTextVersion(value: String?) {
        tvVersion.text = "SDK / Player / API Version: $value"
    }

    /**
     * Depict Device Info
     *
     * @param value should be formatted like below
     * EX: Device Name / OS Version
     */
    @SuppressLint("SetTextI18n")
    fun setTextDeviceInfo(value: String?) {
        tvDeviceInfo.text = "Device Info: $value"
    }

    /**
     * Depict Video format
     *
     * @param value should be formatted like below
     * Ex: video/avc 1280x720@30
     */
    @SuppressLint("SetTextI18n")
    fun setTextVideoFormat(value: String?) {
        tvVideoFormat.text = "Video format: $value"
    }

    /**
     * Depict audio format
     *
     * @param value should be formatted like below
     * Ex: audio/mp4a-latm 48000Hz
     */
    @SuppressLint("SetTextI18n")
    fun setTextAudioFormat(value: String?) {
        tvAudioFormat.text = "Audio format: $value"
    }

    /**
     * Depict current / optimal (download) resolution
     *
     * @param value should be formatted like below
     * Ex: 1280x720 /
     */
    @SuppressLint("SetTextI18n")
    fun setTextResolution(value: String?) {
        tvResolution.text = "Current / Optimal Res: $value"
    }

    /**
     * Depict latency of live stream
     *
     * @param value should be formatted like below
     * Ex: 1000 ms /
     */
    @SuppressLint("SetTextI18n")
    fun setTextLiveStreamLatency(value: String?) {
        tvLiveStreamLatency.text =
            "Live stream latency: ${context.getString(R.string.format_live_stream_latency, value)}"
    }

    /**
     * Hide TextView latency of live stream
     */
    fun hideTextLiveStreamLatency() {
        goneViews(tvLiveStreamLatency)
    }

    /**
     * Show TextView latency of live stream
     */
    fun showTextLiveStreamLatency() {
        visibleViews(tvLiveStreamLatency)
    }

    companion object {
        private fun getPixelAspectRatioString(pixelAspectRatio: Float): String {
            return if (pixelAspectRatio == Format.NO_VALUE.toFloat() || pixelAspectRatio == 1f) {
                ""
            } else {
                " par:" + String.format(Locale.getDefault(), "%.02f", pixelAspectRatio)
            }
        }

        private fun getDecoderCountersBufferCountString(counters: DecoderCounters?): String {
            if (counters == null) {
                return ""
            }
            counters.ensureUpdated()
            return " (sib:" + counters.skippedInputBufferCount +
                    " sb:" + counters.skippedOutputBufferCount +
                    " rb:" + counters.renderedOutputBufferCount +
                    " db:" + counters.droppedBufferCount +
                    " mcdb:" + counters.maxConsecutiveDroppedBufferCount +
                    " dk:" + counters.droppedToKeyframeCount +
                    ")"
        }
    }
}
