package com.uiza.sdk.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.Pair
import android.util.Rational
import android.view.*
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ezralazuardy.orb.Orb
import com.ezralazuardy.orb.OrbHelper
import com.ezralazuardy.orb.OrbListener
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.MediaItem.AdsConfiguration
import com.google.android.exoplayer2.MediaItem.Builder
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.decoder.DecoderReuseEvaluation
import com.google.android.exoplayer2.device.DeviceInfo
import com.google.android.exoplayer2.drm.FrameworkMediaDrm
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.ErrorMessageProvider
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoSize
import com.uiza.sdk.R
import com.uiza.sdk.UZPlayer
import com.uiza.sdk.dialog.hq.UZItem
import com.uiza.sdk.dialog.speed.Callback
import com.uiza.sdk.dialog.speed.Speed
import com.uiza.sdk.dialog.speed.UZSpeedDialog
import com.uiza.sdk.exceptions.ErrorUtils
import com.uiza.sdk.exceptions.UZException
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.observers.SensorOrientationChangeNotifier
import com.uiza.sdk.utils.*
import com.uiza.sdk.view.UZPlayerView.OnDoubleTap
import com.uiza.sdk.widget.UZImageButton
import com.uiza.sdk.widget.UZPreviewTimeBar
import com.uiza.sdk.widget.UZTextView
import com.uiza.sdk.widget.previewseekbar.PreviewLoader
import com.uiza.sdk.widget.previewseekbar.PreviewView
import com.uiza.sdk.widget.previewseekbar.PreviewView.OnPreviewChangeListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_uz_ima_video_core.view.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class UZVideoView : RelativeLayout,
    SensorOrientationChangeNotifier.Listener,
    View.OnClickListener {

    companion object {
        private const val HYPHEN = "-"
        private const val FAST_FORWARD_REWIND_INTERVAL = 10000L // 10s
        private const val DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS = 5000 // 5s
        private const val ARG_VIDEO_POSITION = "ARG_VIDEO_POSITION"
    }

    private fun log(msg: String) {
        Log.d("loitpp" + javaClass.simpleName, msg)
    }

    private var defaultSeekValue = FAST_FORWARD_REWIND_INTERVAL
    private var llTopUZ: LinearLayout? = null
    private var layoutPreviewUZ: FrameLayout? = null
    private var timeBarUZ: UZPreviewTimeBar? = null
    private var ivThumbnailUZ: ImageView? = null
    private var tvPositionUZ: UZTextView? = null
    private var tvDurationUZ: UZTextView? = null
    private var tvTitleUZ: TextView? = null
    private var btFullscreenUZ: UZImageButton? = null
    private var btPauseUZ: UZImageButton? = null
    private var btPlayUZ: UZImageButton? = null
    private var btReplayUZ: UZImageButton? = null
    private var btRewUZ: UZImageButton? = null
    private var btFfwdUZ: UZImageButton? = null
    private var btBackScreenUZ: UZImageButton? = null
    private var btVolumeUZ: UZImageButton? = null
    private var btSettingUZ: UZImageButton? = null
    private var btPipUZ: UZImageButton? = null
    private var btSpeedUZ: UZImageButton? = null
    var uzPlayerView: UZPlayerView? = null
    var isAutoStart: Boolean = true
    private var autoMoveToLiveEdge = false
    private var isInPipMode = false
    private var isPIPModeEnabled = false
    private var isUSeControllerRestorePip = false
    private var positionPIPPlayer = 0L
    private var isAutoReplay = false
    private var isFreeSize = false
    private var isPlayerControllerAlwayVisible = false
    private var isControllerHideOnTouch = true
    private var timestampOnStartPreviewTimeBar = 0L
    private var isOnPreviewTimeBar = false
    private var maxSeekLastDurationTimeBar = 0L
    private var isLandscape = false
    private var isAlwaysPortraitScreen = false
    private var isEnableDoubleTapToSeek = false
    private var isOnPlayerEnded = false
    private var isShowLayoutDebug = false

    private var isRefreshFromChangeSkin = false
    private var currentPositionBeforeChangeSkin = 0L
    private var isCalledFromChangeSkin = false
    private var isFirstStateReady = false

    private var isViewCreated = false
    private var skinId = UZPlayer.skinDefault
    var uzPlayback: UZPlayback? = null

    var listRemoteAction: List<RemoteAction>? = null

    var onPlayerViewCreated: ((playerView: UZPlayerView) -> Unit)? = null
    var onIsInitResult: ((linkPlay: String) -> Unit)? = null
    var onSkinChange: ((skinId: Int) -> Unit)? = null
    var onScreenRotate: ((isLandscape: Boolean) -> Unit)? = null
    var onError: ((e: UZException) -> Unit)? = null
    var onPlayerStateChanged: ((playbackState: Int) -> Unit)? = null
    var onFirstStateReady: (() -> Unit)? = null

    var onStartPreviewTimeBar: ((previewView: PreviewView?, progress: Int) -> Unit)? = null
    var onStopPreviewTimeBar: ((previewView: PreviewView?, progress: Int) -> Unit)? = null
    var onPreviewTimeBar: ((previewView: PreviewView?, progress: Int, fromUser: Boolean) -> Unit)? =
        null
    var onNetworkChange: ((isConnected: Boolean) -> Unit)? = null
    var onCurrentWindowDynamic: ((isLIVE: Boolean) -> Unit)? = null
    var onSurfaceRedrawNeeded: ((holder: SurfaceHolder) -> Unit)? = null
    var onSurfaceCreated: ((holder: SurfaceHolder) -> Unit)? = null
    var onSurfaceChanged: ((holder: SurfaceHolder, format: Int, width: Int, height: Int) -> Unit)? =
        null
    var onSurfaceDestroyed: ((holder: SurfaceHolder) -> Unit)? = null

    var onDoubleTapFinished: (() -> Unit)? = null
    var onDoubleTapProgressDown: ((posX: Float, posY: Float) -> Unit)? = null
    var onDoubleTapStarted: ((posX: Float, posY: Float) -> Unit)? = null
    var onDoubleTapProgressUp: ((posX: Float, posY: Float) -> Unit)? = null

    var onShuffleModeChanged: ((eventTime: AnalyticsListener.EventTime, shuffleModeEnabled: Boolean) -> Unit)? =
        null
    var onLoadStarted: ((
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) -> Unit)? = null
    var onLoadCompleted: ((
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) -> Unit)? = null
    var onLoadCanceled: ((
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) -> Unit)? = null
    var onLoadError: ((
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) -> Unit)? = null
    var onDownstreamFormatChanged: ((
        eventTime: AnalyticsListener.EventTime,
        mediaLoadData: MediaLoadData
    ) -> Unit)? = null
    var onUpstreamDiscarded: ((
        eventTime: AnalyticsListener.EventTime,
        mediaLoadData: MediaLoadData
    ) -> Unit)? = null
    var onBandwidthEstimate: ((
        eventTime: AnalyticsListener.EventTime,
        totalLoadTimeMs: Int,
        totalBytesLoaded: Long,
        bitrateEstimate: Long
    ) -> Unit)? = null
    var onAudioEnabled: ((
        eventTime: AnalyticsListener.EventTime,
        decoderCounters: DecoderCounters
    ) -> Unit)? = null
    var onAudioDecoderInitialized: ((
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) -> Unit)? = null
    var onAudioInputFormatChanged: ((
        eventTime: AnalyticsListener.EventTime,
        format: Format,
        decoderReuseEvaluation: DecoderReuseEvaluation?
    ) -> Unit)? = null
    var onAudioPositionAdvancing: ((
        eventTime: AnalyticsListener.EventTime,
        playoutStartSystemTimeMs: Long
    ) -> Unit)? = null
    var onAudioUnderrun: ((
        eventTime: AnalyticsListener.EventTime,
        bufferSize: Int,
        bufferSizeMs: Long,
        elapsedSinceLastFeedMs: Long
    ) -> Unit)? = null
    var onAudioDecoderReleased: ((
        eventTime: AnalyticsListener.EventTime,
        decoderName: String
    ) -> Unit)? = null
    var onAudioDisabled: ((
        eventTime: AnalyticsListener.EventTime,
        decoderCounters: DecoderCounters
    ) -> Unit)? = null
    var onAudioSinkError: ((
        eventTime: AnalyticsListener.EventTime,
        audioSinkError: java.lang.Exception
    ) -> Unit)? = null
    var onAudioCodecError: ((
        eventTime: AnalyticsListener.EventTime,
        audioCodecError: java.lang.Exception
    ) -> Unit)? = null
    var onVideoEnabled: ((
        eventTime: AnalyticsListener.EventTime,
        decoderCounters: DecoderCounters
    ) -> Unit)? = null
    var onVideoDecoderInitialized: ((
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) -> Unit)? = null
    var onVideoInputFormatChanged: ((
        eventTime: AnalyticsListener.EventTime,
        format: Format,
        decoderReuseEvaluation: DecoderReuseEvaluation?
    ) -> Unit)? = null
    var onDroppedVideoFrames: ((
        eventTime: AnalyticsListener.EventTime,
        droppedFrames: Int,
        elapsedMs: Long
    ) -> Unit)? = null
    var onVideoDecoderReleased: ((
        eventTime: AnalyticsListener.EventTime,
        decoderName: String
    ) -> Unit)? = null
    var onVideoDisabled: ((
        eventTime: AnalyticsListener.EventTime,
        decoderCounters: DecoderCounters
    ) -> Unit)? = null
    var onVideoFrameProcessingOffset: ((
        eventTime: AnalyticsListener.EventTime,
        totalProcessingOffsetUs: Long,
        frameCount: Int
    ) -> Unit)? = null
    var onPlayerReleased: ((eventTime: AnalyticsListener.EventTime) -> Unit)? = null
    var onProgressChange: ((currentPosition: Long, duration: Long, isPlayingAd: Boolean?) -> Unit)? =
        null

    var onVideoSizeChanged: ((videoSize: VideoSize) -> Unit)? = null
    var onSurfaceSizeChanged: ((width: Int, height: Int) -> Unit)? = null
    var onRenderedFirstFrame: (() -> Unit)? = null
    var onAudioSessionIdChanged: ((audioSessionId: Int) -> Unit)? = null
    var onAudioAttributesChanged: ((audioAttributes: AudioAttributes) -> Unit)? = null
    var onVolumeChanged: ((volume: Float) -> Unit)? = null
    var onSkipSilenceEnabledChanged: ((skipSilenceEnabled: Boolean) -> Unit)? = null
    var onCues: ((cues: MutableList<Cue>) -> Unit)? = null
    var onMetadata: ((metadata: com.google.android.exoplayer2.metadata.Metadata) -> Unit)? = null
    var onDeviceInfoChanged: ((deviceInfo: DeviceInfo) -> Unit)? = null
    var onDeviceVolumeChanged: ((volume: Int, muted: Boolean) -> Unit)? = null
    var onTimelineChanged: ((timeline: Timeline, reason: Int) -> Unit)? = null
    var onMediaItemTransition: ((mediaItem: MediaItem?, reason: Int) -> Unit)? = null
    var onTracksChanged: ((trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) -> Unit)? =
        null
    var onIsLoadingChanged: ((isLoading: Boolean) -> Unit)? = null
    var onAvailableCommandsChanged: ((availableCommands: Player.Commands) -> Unit)? = null
    var onPlayWhenReadyChanged: ((playWhenReady: Boolean, reason: Int) -> Unit)? = null
    var onIsPlayingChanged: ((isPlaying: Boolean) -> Unit)? = null
    var onRepeatModeChanged: ((repeatMode: Int) -> Unit)? = null
    var onShuffleModeEnabledChanged: ((shuffleModeEnabled: Boolean) -> Unit)? = null
    var onPlayerError: ((error: PlaybackException) -> Unit)? = null
    var onPlayerErrorChanged: ((error: PlaybackException?) -> Unit)? = null
    var onPositionDiscontinuity: ((oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) -> Unit)? =
        null
    var onPlaybackParametersChanged: ((playbackParameters: PlaybackParameters) -> Unit)? = null
    var onSeekBackIncrementChanged: ((seekBackIncrementMs: Long) -> Unit)? = null
    var onSeekForwardIncrementChanged: ((seekForwardIncrementMs: Long) -> Unit)? = null
    var onMaxSeekToPreviousPositionChanged: ((maxSeekToPreviousPositionMs: Int) -> Unit)? = null

    private var orb: Orb? = null
    private val compositeDisposable = CompositeDisposable()

    var player: SimpleExoPlayer? = null
    private var dataSourceFactory: DataSource.Factory? = null
    private var mediaItems: List<MediaItem>? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
    private var lastSeenTrackGroupArray: TrackGroupArray? = null
    private var startAutoPlay = false
    private var startWindow = 0
    private var startPosition: Long = 0
    private var adsLoader: AdsLoader? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isViewCreated) {
            onCreateView()
        }
    }

    private fun onCreateView() {
        dataSourceFactory = DemoUtil.getDataSourceFactory(context)
        inflate(context, R.layout.layout_uz_ima_video_core, this)

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        if (inflater == null) {
            throw NullPointerException("Cannot inflater view")
        } else {
            uzPlayerView = inflater.inflate(skinId, null) as UZPlayerView?
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT)
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            layoutParams.addRule(CENTER_IN_PARENT, TRUE)

            uzPlayerView?.let {
                it.layoutParams = layoutParams
                it.visibility = GONE

                if (it.videoSurfaceView is SurfaceView) {
                    (it.videoSurfaceView as SurfaceView).holder.addCallback(object :
                        SurfaceHolder.Callback2 {
                        override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
                            onSurfaceRedrawNeeded?.invoke(holder)
                        }

                        override fun surfaceCreated(holder: SurfaceHolder) {
                            onSurfaceCreated?.invoke(holder)
                        }

                        override fun surfaceChanged(
                            holder: SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int
                        ) {
                            onSurfaceChanged?.invoke(holder, format, width, height)
                        }

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            if (isInPipMode) {
                                if (isPIPEnable) {
                                    if (context is Activity) {
                                        (context as Activity).finishAndRemoveTask()
                                    }
                                }
                            }
                            onSurfaceDestroyed?.invoke(holder)
                        }
                    })
                }

                it.setErrorMessageProvider(PlayerErrorMessageProvider(context))
                it.requestFocus()
                layoutRootView.addView(it)

                val builder = ParametersBuilder(context)
                trackSelectorParameters = builder.build()
                clearStartPosition()
            }

            findViews()
            resizeContainerView()
        }
        updateUIEachSkin()
        setMarginPreviewTimeBar()
        updateUISizeThumbnailTimeBar()
        isViewCreated = true

        uzPlayerView?.let {
            onPlayerViewCreated?.invoke(it)
        }

        createOrb()
    }

    private fun createOrb() {
        val observer = OrbHelper.orbObserver {
            handleNetworkChange(it.connected)
        }
        orb = Orb.with(context).setListener(object : OrbListener {
            override fun onOrbActive() {
//                log("onOrbActive")
            }

            override fun onOrbInactive() {
//                log("onOrbInactive")
            }

            override fun onOrbObserve() {
//                log("onOrbObserve")
            }

            override fun onOrbStop() {
//                log("onOrbStop")
            }

        }).observe(observer)
        orb?.observe(observer)
    }

    private fun findViews() {
        UZViewUtils.setColorProgressBar(progressBar = pb, color = Color.WHITE)

        uzPlayerView?.let { pv ->
            uzPlayerView?.useController =
                false//khong cho dung controller cho den khi isFirstStateReady == true
            pv.setOnDoubleTap(object : OnDoubleTap {
                override fun onDoubleTapFinished() {
//                    log("onDoubleTapFinished")
                    onDoubleTapFinished?.invoke()
                }

                override fun onDoubleTapProgressDown(posX: Float, posY: Float) {
//                    log("onDoubleTapProgressDown")
                    onDoubleTapProgressDown?.invoke(posX, posY)
                }

                override fun onDoubleTapStarted(posX: Float, posY: Float) {
//                    log("onDoubleTapStarted")
                    onDoubleTapStarted?.invoke(posX, posY)
                }

                override fun onDoubleTapProgressUp(posX: Float, posY: Float) {
//                    log("onDoubleTapProgressUp")
                    if (isEnableDoubleTapToSeek) {
                        val halfScreen = UZViewUtils.screenWidth / 2.0f
                        if (posX - 60.0f > halfScreen) {
                            seekToForward()
                        } else if (posX + 60.0f < halfScreen) {
                            seekToBackward()
                        }
                    }
                    onDoubleTapProgressUp?.invoke(posX, posY)
                }
            })
            timeBarUZ = pv.findViewById(R.id.exo_progress)
            layoutPreviewUZ = pv.findViewById(R.id.layoutPreviewUZ)

            if (timeBarUZ == null) {
                pv.visibility = VISIBLE
            } else {
                timeBarUZ?.let { tb ->
                    pv.visibility = VISIBLE
                    tb.addOnPreviewChangeListener(object : OnPreviewChangeListener {
                        override fun onStartPreview(previewView: PreviewView?, progress: Int) {
                            timestampOnStartPreviewTimeBar = System.currentTimeMillis()
                            onStartPreviewTimeBar?.invoke(previewView, progress)
                        }

                        override fun onStopPreview(previewView: PreviewView?, progress: Int) {
                            val seekLastDuration =
                                System.currentTimeMillis() - timestampOnStartPreviewTimeBar
                            if (maxSeekLastDurationTimeBar < seekLastDuration) {
                                maxSeekLastDurationTimeBar = seekLastDuration
                            }
                            isOnPreviewTimeBar = false
                            onStopPreview(progress)
                            onStopPreviewTimeBar?.invoke(previewView, progress)
                        }

                        override fun onPreview(
                            previewView: PreviewView?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            isOnPreviewTimeBar = true
                            updateUIIbRewIconDependOnProgress(
                                currentMls = progress.toLong(),
                                isCalledFromUZTimeBarEvent = true
                            )
                            onPreviewTimeBar?.invoke(previewView, progress, fromUser)
                        }

                    })
                }
            }

            llTopUZ = pv.findViewById(R.id.llTopUZ)
            ivThumbnailUZ = pv.findViewById(R.id.ivThumbnailUZ)
            tvPositionUZ = pv.findViewById(R.id.tvPositionUZ)
            tvDurationUZ = pv.findViewById(R.id.tvDurationUZ)
            btFullscreenUZ = pv.findViewById(R.id.btFullscreenUZ)
            tvTitleUZ = pv.findViewById(R.id.tvTitleUZ)
            btPauseUZ = pv.findViewById(R.id.btPauseUZ)
            btPlayUZ = pv.findViewById(R.id.btPlayUZ)
            btReplayUZ = pv.findViewById(R.id.btReplayUZ)
            btRewUZ = pv.findViewById(R.id.btRewUZ)
            btFfwdUZ = pv.findViewById(R.id.btFfwdUZ)
            btBackScreenUZ = pv.findViewById(R.id.btBackScreenUZ)
            btVolumeUZ = pv.findViewById(R.id.btVolumeUZ)
            btSettingUZ = pv.findViewById(R.id.btSettingUZ)
            btPipUZ = pv.findViewById(R.id.btPipUZ)
            btSpeedUZ = pv.findViewById(R.id.btSpeedUZ)

            tvPositionUZ?.text = StringUtils.convertMlsecondsToHMmSs(0)
            tvDurationUZ?.text = "-:-"

            btRewUZ?.setSrcDrawableDisabled()

            if (!isPIPEnable) {
                UZViewUtils.goneViews(btPipUZ)
            }

            setEventForViews()
        }
    }

    private fun resizeContainerView() {
        if (isFreeSize) {
            setSize(width = this.width, height = this.height)
        } else {
            setSize(width = getVideoWidth(), height = getVideoHeight())
        }
    }

    var controllerAutoShow: Boolean
        get() = uzPlayerView?.controllerAutoShow ?: false
        set(isAutoShowController) {
            uzPlayerView?.controllerAutoShow = isAutoShowController
        }

    //return pixel
    val heightTimeBar: Int
        get() {
            timeBarUZ?.let {
                return UZViewUtils.heightOfView(it)
            }
            return 0
        }

    private val duration: Long
        get() = player?.duration ?: -1

    private val currentPosition: Long
        get() = player?.currentPosition ?: -1

    val videoFormat: Format?
        get() = player?.videoFormat

    val audioFormat: Format?
        get() = player?.audioFormat

    fun getVideoProfileW(): Int {
        return videoFormat?.width ?: 0
    }

    fun getVideoProfileH(): Int {
        return videoFormat?.height ?: 0
    }

    fun setResizeMode(resizeMode: Int) {
        try {
            uzPlayerView?.resizeMode = resizeMode
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSize(width: Int, height: Int) {
        UZViewUtils.resizeLayout(
            viewGroup = layoutRootView,
            videoW = width,
            videoH = height,
            isFreeSize = isFreeSize
        )
    }

    fun setFreeSize(isFreeSize: Boolean) {
        this.isFreeSize = isFreeSize
        resizeContainerView()
    }

    fun setPlayerControllerAlwaysVisible() {
        controllerAutoShow = true
        setControllerHideOnTouch(false)
        controllerShowTimeoutMs = 0
        isPlayerControllerAlwayVisible = true
    }

    private fun handleError(uzException: UZException?) {
        if (uzException == null) {
            return
        }
        uzException.printStackTrace()
        notifyError(uzException)
    }

    private fun notifyError(exception: UZException) {
        onError?.invoke(exception)
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun play(uzPlayback: UZPlayback): Boolean {
        if (!ConnectivityUtils.isConnected(context)) {
            notifyError(ErrorUtils.exceptionNoConnection())
            return false
        }
        val linkPlay = uzPlayback.linkPlay
        if (linkPlay.isNullOrEmpty()) {
            handleError(ErrorUtils.exceptionNoLinkPlay())
            return false
        }
        releaseAdsLoader()
        this.uzPlayback = uzPlayback

        isCalledFromChangeSkin = false
        isOnPlayerEnded = false
        //khong cho dung controller cho den khi isFirstStateReady == true
        uzPlayerView?.useController = false
        updateUIEndScreen()
        releasePlayerManager()
        showProgress()
        initDataSource()
        initPlayerManager()
        initializePlayer()
        onIsInitResult?.invoke(linkPlay)
        return true
    }

    fun resume() {
        player?.playWhenReady = true
        keepScreenOn = true
    }

    fun pause() {
        player?.playWhenReady = false
        keepScreenOn = false
    }

    fun getVideoWidth(): Int {
        return uzPlayerView?.player?.videoSize?.width ?: 0
    }

    fun getVideoHeight(): Int {
        return uzPlayerView?.player?.videoSize?.height ?: 0
    }

    private fun initPlayerManager() {
        if (isRefreshFromChangeSkin) {
            seekTo(currentPositionBeforeChangeSkin)
            isRefreshFromChangeSkin = false
            currentPositionBeforeChangeSkin = 0
        }
    }

    fun onBackPressed(): Boolean {
        if (isLandscape) {
            toggleFullscreen()
            return true
        }
        return false
    }

    fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        positionPIPPlayer = currentPosition
        isInPipMode = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.
            setUseController(useController = false)
        } else {
            // Restore the full-screen UI.
            setUseController(useController = isUSeControllerRestorePip)
        }
        if (newConfig == null) {
            log("newConfig == null")
        } else {
            log("newConfig != null")
        }
    }

    fun onDestroyView() {
        compositeDisposable.clear()
        releasePlayerManager()
        if (isPIPEnable) {
            if (context is Activity) {
                (context as Activity).finishAndRemoveTask()
            }
        }
        releaseAdsLoader()
        orb?.stop()
    }

    private fun releasePlayerManager() {
        try {
            uzPlayerView?.overlayFrameLayout?.removeAllViews()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onResumeView() {
        SensorOrientationChangeNotifier.getInstance(context)?.addListener(this)
        player?.playWhenReady = true
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
            if (uzPlayerView != null) {
                uzPlayerView?.onResume()
            }
        }

        if (positionPIPPlayer > 0L && isInPipMode) {
            seekTo(positionPIPPlayer)
        } else if (autoMoveToLiveEdge && isLIVE) {
            // try to move to the edge of livestream video
            seekToLiveEdge()
        }

    }

    val isPlaying: Boolean
        get() = player?.isPlaying ?: false

    fun isPlayingAd(): Boolean? {
        return player?.isPlayingAd
    }

    fun setAutoMoveToLiveEdge(autoMoveToLiveEdge: Boolean) {
        this.autoMoveToLiveEdge = autoMoveToLiveEdge
    }

    fun seekToLiveEdge() {
        if (isLIVE) {
            player?.seekToDefaultPosition()
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(ARG_VIDEO_POSITION, currentPosition)
        updateTrackSelectorParameters()
        updateStartPosition()
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        positionPIPPlayer = savedInstanceState.getLong(ARG_VIDEO_POSITION)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return uzPlayerView?.dispatchKeyEvent(event) == true || super.dispatchKeyEvent(event)
    }

    fun onStartView() {
        if (Util.SDK_INT > 23) {
            initializePlayer()
            uzPlayerView?.onResume()
        }
    }

    fun onStopView() {
        if (Util.SDK_INT > 23) {
            uzPlayerView?.onPause()
            releasePlayer()
        }
    }

    fun onPauseView() {
        positionPIPPlayer = currentPosition
        SensorOrientationChangeNotifier.getInstance(context)?.remove(this)

        // in PIP to continue
        if (!isInPipMode) {
            player?.playWhenReady = false
            if (Util.SDK_INT <= 23) {
                uzPlayerView?.onPause()
                releasePlayer()
            }
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            updateTrackSelectorParameters()
            updateStartPosition()
            player?.release()
            player = null
            mediaItems = emptyList()
            trackSelector = null
        }
        adsLoader?.setPlayer(null)
    }

    private fun updateStartPosition() {
        player?.let {
            startAutoPlay = it.playWhenReady
            startWindow = it.currentWindowIndex
            startPosition = max(0, it.contentPosition)
        }
    }

    private fun updateTrackSelectorParameters() {
        trackSelector?.parameters?.let {
            trackSelectorParameters = it
        }
    }

    val isPIPEnable: Boolean
        get() = (btPipUZ != null && UZAppUtils.hasSupportPIP(context = context) && uzPlayerView?.isUseUZDragView() == false && isPIPModeEnabled)

    private fun onStopPreview(progress: Int) {
        seekTo(progress.toLong())
        player?.playWhenReady = true
        isOnPlayerEnded = false
        updateUIEndScreen()
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        uzPlayerView?.let { pv ->
            resizeContainerView()
            val currentOrientation = resources.configuration.orientation
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!isInPipMode) {
                    UZViewUtils.hideSystemUiFullScreen(pv)
                }
                isLandscape = true
                btFullscreenUZ?.let {
                    UZViewUtils.setUIFullScreenIcon(imageButton = it, isFullScreen = true)
                }
                UZViewUtils.goneViews(btPipUZ)
            } else {//portrait screen
                if (!isInPipMode) {
                    UZViewUtils.hideSystemUi(pv)
                }
                isLandscape = false
                btFullscreenUZ?.let {
                    UZViewUtils.setUIFullScreenIcon(imageButton = it, isFullScreen = false)
                }
                if (isPIPEnable) {
                    UZViewUtils.visibleViews(btPipUZ)
                }
            }
            setMarginPreviewTimeBar()
            updateUISizeThumbnailTimeBar()
            onScreenRotate?.invoke(isLandscape)
        }
    }

    override fun onClick(v: View) {
        when {
            v === btFullscreenUZ -> {
                toggleFullscreen()
            }
            v === btBackScreenUZ -> {
                clickBackScreen()
            }
            v === btVolumeUZ -> {
                toggleVolumeMute()
            }
            v === btSettingUZ -> {
                showSettingsDialog()
            }
            v === btPipUZ -> {
                enterPIPMode()
            }
            v.parent === layoutControls -> {
                showTrackSelectionDialog(v, true)
            }
            v === btFfwdUZ -> {
                player?.let {
                    it.seekTo(min(it.currentPosition + defaultSeekValue, it.duration))
                }
            }
            v === btRewUZ -> {
                player?.let {
                    if (it.currentPosition - defaultSeekValue > 0) {
                        it.seekTo(it.currentPosition - defaultSeekValue)
                    } else {
                        it.seekTo(0)
                    }
                }

                if (isPlaying) {
                    isOnPlayerEnded = false
                    updateUIEndScreen()
                }
            }
            v === btPauseUZ -> {
                pause()
            }
            v === btPlayUZ -> {
                resume()
            }
            v === btReplayUZ -> {
                replay()
            }
            v === btSpeedUZ -> {
                showSpeed()
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun enterPIPMode() {
        if (isPIPEnable) {
            if (isLandscape) {
                throw IllegalArgumentException("Cannot enter PIP Mode if screen is landscape")
            }
            isInPipMode = true
            positionPIPPlayer = currentPosition
            isUSeControllerRestorePip = isUseController()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                try {
                    val aspectRatio = Rational(getVideoWidth(), getVideoHeight())
                    params.setAspectRatio(aspectRatio)
                    params.setActions(listRemoteAction)
                    if (context is Activity) {
                        (context as Activity).enterPictureInPictureMode(params.build())
                    }
                } catch (e: Exception) {
                    log("enterPIPMode e $e")
                    val w: Int
                    val h: Int
                    if (getVideoWidth() == 0 || getVideoHeight() == 0) {
                        w = this.width
                        h = this.height
                    } else {
                        if (getVideoWidth() > getVideoHeight()) {
                            w = 1280
                            h = 720
                        } else {
                            w = 720
                            h = 1280
                        }
                    }
                    val aspectRatio = Rational(w, h)
                    params.setAspectRatio(aspectRatio)
                    params.setActions(listRemoteAction)
                    if (context is Activity) {
                        (context as Activity).enterPictureInPictureMode(params.build())
                    }
                }
            } else {
                if (context is Activity) {
                    (context as Activity).enterPictureInPictureMode()
                }
            }
        }
    }

    var controllerShowTimeoutMs: Int
        get() = uzPlayerView?.controllerShowTimeoutMs ?: -1
        set(controllerShowTimeoutMs) {
            uzPlayerView?.controllerShowTimeoutMs = controllerShowTimeoutMs
        }

    val isPlayerControllerShowing: Boolean
        get() = uzPlayerView?.isControllerVisible() ?: false

    fun showController() {
        uzPlayerView?.showController()
    }

    fun hideController() {
        if (isPlayerControllerAlwayVisible) {
            return
        }
        uzPlayerView?.hideController()
    }

    fun setControllerHideOnTouch(controllerHideOnTouch: Boolean) {
        this.isControllerHideOnTouch = controllerHideOnTouch
        uzPlayerView?.controllerHideOnTouch = controllerHideOnTouch
    }

    val controllerHideOnTouch: Boolean
        get() = uzPlayerView?.controllerHideOnTouch ?: false

    fun isUseController(): Boolean {
        return uzPlayerView?.useController ?: false
    }

    fun setUseController(useController: Boolean): Boolean {
        if (!isFirstStateReady) {
            log("setUseController() can be applied if the player state is Player.STATE_READY")
            return false
        }
        uzPlayerView?.useController = useController
        return true
    }

    private fun onPlayerEnded() {
        if (isPlaying) {
            keepScreenOn = false
            isOnPlayerEnded = true
            if (isAutoReplay) {
                replay()
            } else {
                updateUIEndScreen()
            }
        }
        hideProgress()
    }

    fun replay() {
        seekTo(0)
        isOnPlayerEnded = false
        updateUIEndScreen()
    }

    fun clickBackScreen() {
        if (isLandscape) {
            toggleFullscreen()
        } else {
            if (context is Activity) {
                (context as Activity).onBackPressed()
            }
        }
    }

    fun clickAudio() {
        val view = DebugUtils.getAudioButton(layoutControls)
        view?.performClick()
    }

    fun clickQuality() {
        val view = DebugUtils.getVideoButton(layoutControls)
        view?.performClick()
    }

    fun clickCaptions() {
        val view = DebugUtils.getCaptionsButton(layoutControls)
        view?.performClick()
    }

    fun setDefaultSeekValue(mls: Long) {
        if (!isFirstStateReady) {
            throw IllegalArgumentException("setDefaultSeekValue(...) can be applied if the player state is Player.STATE_READY")
        }
        defaultSeekValue = mls
    }

    fun seekToForward(mls: Long) {
        setDefaultSeekValue(mls)
        btFfwdUZ?.performClick()
    }

    fun seekToForward() {
        if (!isLIVE) {
            btFfwdUZ?.performClick()
        }
    }

    fun seekToBackward(mls: Long) {
        setDefaultSeekValue(mls)
        btRewUZ?.performClick()
    }

    fun seekToBackward() {
        btRewUZ?.performClick()
    }

    fun toggleShowHideController() {
        uzPlayerView?.toggleShowHideController()
    }

    fun togglePlayPause() {
        if (player == null) {
            return
        }
        if (player?.playWhenReady == true) {
            pause()
        } else {
            resume()
        }
    }

    fun toggleFullscreen() {
        if (context is Activity) {
            UZViewUtils.toggleScreenOrientation(activity = (context as Activity))
        }
    }

    fun showSpeed() {
        player?.let { p ->
            val uzDlgSpeed = UZSpeedDialog(
                context = context,
                currentSpeed = p.playbackParameters.speed,
                callback = object : Callback {
                    override fun onSelectItem(speed: Speed) {
                        setSpeed(speed = speed.value)
                    }
                })
            UZViewUtils.showDialog(uzDlgSpeed)
        }
    }

    val isLIVE: Boolean
        get() {
            return player?.isCurrentWindowLive ?: false
        }

    val isVOD: Boolean
        get() {
            return !isLIVE
        }

    var volume: Float
        get() {
            return player?.volume ?: -1f
        }
        set(volume) {
            player?.volume = volume
        }
    private var volumeToggle = 0f

    fun toggleVolumeMute() {
        if (volume == 0f) {
            volume = volumeToggle
        } else {
            volumeToggle = volume
            volume = 0f
        }
    }

    fun setSpeed(speed: Float) {
        require(!isLIVE) {
            resources.getString(R.string.error_speed_live_content)
        }
        require(!(speed > 3 || speed < -3)) {
            resources.getString(R.string.error_speed_illegal)
        }
        val playbackParameters = PlaybackParameters(speed)
        player?.playbackParameters = playbackParameters
    }

    private fun setEventForViews() {
        fun setClickAndFocusEventForViews(vararg views: View?) {
            for (v in views) {
                v?.setOnClickListener(this)
            }
        }

        setClickAndFocusEventForViews(
            btFullscreenUZ,
            btBackScreenUZ,
            btVolumeUZ,
            btSettingUZ,
            btPipUZ,
            btFfwdUZ,
            btRewUZ,
            btPlayUZ,
            btPauseUZ,
            btReplayUZ,
            btSpeedUZ,
        )
    }

    private fun updateUIEachSkin() {
        if (skinId == R.layout.uzplayer_skin_2 || skinId == R.layout.uzplayer_skin_3) {

            btPlayUZ?.setRatioLand(7)
            btPlayUZ?.setRatioPort(5)

            btPauseUZ?.setRatioLand(7)
            btPauseUZ?.setRatioPort(5)

            btReplayUZ?.setRatioLand(7)
            btReplayUZ?.setRatioPort(5)
        }
    }

    /*
     ** change skin of player (realtime)
     * return true if success
     */
    fun changeSkin(@LayoutRes skinId: Int): Boolean {
        if (uzPlayerView?.isUseUZDragView() == true) {
            throw IllegalArgumentException(resources.getString(R.string.error_change_skin_with_uzdragview))
        }
        if (player == null || !isFirstStateReady || isOnPlayerEnded) {
            return false
        }
        if (isPlayingAd() == true) {
            notifyError(ErrorUtils.exceptionChangeSkin())
            return false
        }
        this.skinId = skinId
        UZPlayer.skinDefault = skinId
        isRefreshFromChangeSkin = true
        isCalledFromChangeSkin = true

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        if (inflater != null) {
            uzPlayerView = inflater.inflate(skinId, null) as UZPlayerView?
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            layoutParams.addRule(CENTER_IN_PARENT, TRUE)
            uzPlayerView?.let {
                it.layoutParams = layoutParams
                it.requestFocus()
                layoutRootView.addView(it)

                val builder = ParametersBuilder(context)
                trackSelectorParameters = builder.build()
                clearStartPosition()
            }
            layoutRootView.requestLayout()
            findViews()

            resizeContainerView()
            updateUIEachSkin()
            setMarginPreviewTimeBar()

            currentPositionBeforeChangeSkin = currentPosition
            releasePlayerManager()
            checkToSetUpResource()
            updateUISizeThumbnailTimeBar()
            setVisibilityOfPlayPauseReplay(false)
            onSkinChange?.invoke(getSkinId())

            return true
        }

        return false
    }

    private fun updateTvDuration() {
        if (isLIVE) {
            tvDurationUZ?.text = StringUtils.convertMlsecondsToHMmSs(mls = 0)
        } else {
            tvDurationUZ?.text = StringUtils.convertMlsecondsToHMmSs(mls = duration)
        }
    }

    private fun setTextPosition(currentMls: Long) {
        if (isLIVE) {
            val duration = duration
            val past = duration - currentMls
            tvPositionUZ?.text = String.format(
                "%s%s",
                HYPHEN,
                StringUtils.convertMlsecondsToHMmSs(past)
            )
        } else {
            tvPositionUZ?.text = StringUtils.convertMlsecondsToHMmSs(currentMls)
        }
    }

    private fun updateUIIbRewIconDependOnProgress(
        currentMls: Long,
        isCalledFromUZTimeBarEvent: Boolean
    ) {
        if (isCalledFromUZTimeBarEvent) {
            setTextPosition(currentMls)
        } else {
            if (!isOnPreviewTimeBar) {
                //uzTimeBar is displaying
                setTextPosition(currentMls)
            }
        }
        if (isLIVE) {
            return
        }
        btRewUZ?.let { r ->
            btFfwdUZ?.let { f ->
                if (currentMls <= 0L) {
                    if (r.isSetSrcDrawableEnabled) {
                        r.setSrcDrawableDisabled()
                    }
                    if (!f.isSetSrcDrawableEnabled) {
                        f.setSrcDrawableEnabled()
                    }
                } else if (currentMls == duration) {
                    if (!r.isSetSrcDrawableEnabled) {
                        r.setSrcDrawableEnabled()
                    }
                    if (f.isSetSrcDrawableEnabled) {
                        f.setSrcDrawableDisabled()
                    }
                } else {
                    if (!r.isSetSrcDrawableEnabled) {
                        r.setSrcDrawableEnabled()
                    }
                    if (!f.isSetSrcDrawableEnabled) {
                        f.setSrcDrawableEnabled()
                    }
                }
            }
        }
    }

    private fun updateUISizeThumbnailTimeBar() {
        val screenWidth = UZViewUtils.screenWidth
        val widthIv = if (isLandscape) {
            screenWidth / 5
        } else {
            screenWidth / 5
        }
        layoutPreviewUZ?.let { fl ->
            val layoutParams = LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.width = widthIv
            layoutParams.height = (widthIv * Constants.RATIO_9_16).toInt()
            fl.layoutParams = layoutParams
            fl.requestLayout()
        }
    }

    private fun setMarginPreviewTimeBar() {
        timeBarUZ?.let { tb ->
            if (isLandscape) {
                UZViewUtils.setMarginDimen(view = tb, dpL = 5, dpT = 0, dpR = 5, dpB = 0)
            } else {
                UZViewUtils.setMarginDimen(view = tb, dpL = 0, dpT = 0, dpR = 0, dpB = 0)
            }
        }
    }

    private fun updateUIDependOnLiveStream() {
        if (UZAppUtils.isTablet(context) && UZAppUtils.isTV(context)) {
            //only hide button pip if device is TV
            UZViewUtils.goneViews(btPipUZ)
        }
        onCurrentWindowDynamic?.invoke(isLIVE)
//        log("updateUIDependOnLiveStream isLIVE $isLIVE")
        if (isLIVE) {
            UZViewUtils.goneViews(btSpeedUZ, tvDurationUZ, tvPositionUZ, btRewUZ, btFfwdUZ)
        } else {
            UZViewUtils.visibleViews(btSpeedUZ, tvDurationUZ, tvPositionUZ, btRewUZ, btFfwdUZ)
        }
        tvTitleUZ?.text = uzPlayback?.name ?: ""
        if (UZAppUtils.isTV(context)) {
            UZViewUtils.goneViews(btFullscreenUZ)
        }
    }

    private fun updateUIEndScreen() {
        uzPlayerView?.let { pv ->
            if (isOnPlayerEnded) {
                setVisibilityOfPlayPauseReplay(true)
                showController()
                pv.controllerShowTimeoutMs = 0
                pv.controllerHideOnTouch = false
            } else {
                setVisibilityOfPlayPauseReplay(false)
                pv.controllerShowTimeoutMs = DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS
                setControllerHideOnTouch(isControllerHideOnTouch)
            }
        }
    }

    private fun setVisibilityOfPlayPauseReplay(isShowReplay: Boolean) {
        if (isShowReplay) {
            UZViewUtils.goneViews(btPlayUZ, btPauseUZ)
            UZViewUtils.visibleViews(btReplayUZ)
            btReplayUZ?.requestFocus()
        } else {
            UZViewUtils.goneViews(btReplayUZ)
        }
    }

    private var isShowingTrackSelectionDialog = false

    @SuppressLint("InflateParams")
    fun showSettingsDialog() {
        if (!isShowingTrackSelectionDialog && TrackSelectionDialog.willHaveContent(trackSelector)) {
            isShowingTrackSelectionDialog = true
            val trackSelectionDialog = TrackSelectionDialog.createForTrackSelector(trackSelector) {
                isShowingTrackSelectionDialog = false
            }
            if (context is AppCompatActivity) {
                val supportFragmentManager = (context as AppCompatActivity).supportFragmentManager
                trackSelectionDialog.show(supportFragmentManager, null)
            }
        }
    }

    fun getListTrack(
        showDialog: Boolean = false,
        title: String = "Video",
        rendererIndex: Int
    ): List<UZItem>? {
        //TODO
//        val mappedTrackInfo = playerManager?.trackSelector?.currentMappedTrackInfo
//        mappedTrackInfo?.let {
//            val dialogPair: Pair<AlertDialog, UZTrackSelectionView> =
//                UZTrackSelectionView.getDialog(
//                    context = context,
//                    title = title,
//                    trackSelector = playerManager?.trackSelector,
//                    rendererIndex = rendererIndex
//                )
//            dialogPair.second.setShowDisableOption(false)
//            dialogPair.second.setAllowAdaptiveSelections(false)
//            dialogPair.second.setCallback(object : com.uiza.sdk.dialog.hq.Callback {
//                override fun onClick() {
//                    dialogPair.first?.cancel()
//                }
//            })
//            if (showDialog) {
//                UZViewUtils.showDialog(dialogPair.first)
//            }
//            return dialogPair.second.uZItemList
//        }

        return null
    }

    private fun showTrackSelectionDialog(view: View, showDialog: Boolean): List<UZItem>? {
        //TODO
//        val mappedTrackInfo = playerManager?.trackSelector?.currentMappedTrackInfo
//        mappedTrackInfo?.let {
//            if (view is Button) {
//                val title = view.text
//                val rendererIndex = view.getTag() as Int
//                val dialogPair: Pair<AlertDialog, UZTrackSelectionView> =
//                    UZTrackSelectionView.getDialog(
//                        context = context,
//                        title = title,
//                        trackSelector = playerManager?.trackSelector,
//                        rendererIndex = rendererIndex
//                    )
//                dialogPair.second.setShowDisableOption(false)
//                dialogPair.second.setAllowAdaptiveSelections(false)
//                dialogPair.second.setCallback(object : com.uiza.sdk.dialog.hq.Callback {
//                    override fun onClick() {
//                        dialogPair.first?.cancel()
//                    }
//                })
//                if (showDialog) {
//                    UZViewUtils.showDialog(dialogPair.first)
//                }
//                return dialogPair.second.uZItemList
//            }
//        }

        return null
    }

    override fun setBackgroundColor(color: Int) {
        rootViewUZVideo.setBackgroundColor(color)
    }

    private fun hideProgress() {
        pb.visibility = View.GONE
    }

    private fun showProgress() {
        pb.visibility = View.VISIBLE
    }

    private fun checkToSetUpResource() {
        if (uzPlayback == null) {
            handleError(ErrorUtils.exceptionSetup())
        } else {
            val linkPlay = uzPlayback?.linkPlay
            if (linkPlay.isNullOrEmpty()) {
                handleError(uzException = ErrorUtils.exceptionNoLinkPlay())
                return
            }
            initDataSource()
            onIsInitResult?.invoke(linkPlay)
            initPlayerManager()
        }
    }

    private fun setFirstStateReady(isFirstStateReady: Boolean) {
        this.isFirstStateReady = isFirstStateReady
        if (this.isFirstStateReady) {
            onFirstStateReady?.invoke()
        }
    }

    private fun initDataSource() {
        setFirstStateReady(false)

        val poster = uzPlayback?.poster
        timeBarUZ?.let {
            it.setEnabledPreview(!poster.isNullOrEmpty())
            it.setPreviewLoader(object : PreviewLoader {
                override fun loadPreview(currentPosition: Long, max: Long) {
                    player?.playWhenReady = false
                    ivThumbnailUZ?.let { iv ->
                        ImageUtils.loadThumbnail(
                            imageView = iv,
                            imageUrl = poster,
                            currentPosition = currentPosition,
                        )
                    }
                }
            })
        }
    }

    private fun addListener() {
        player?.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
//                log("onVideoSizeChanged ${videoSize.width} ${videoSize.height}")
                onVideoSizeChanged?.invoke(videoSize)
            }

            override fun onSurfaceSizeChanged(width: Int, height: Int) {
                super.onSurfaceSizeChanged(width, height)
//                log("onSurfaceSizeChanged $width $height")
                onSurfaceSizeChanged?.invoke(width, height)
            }

            override fun onRenderedFirstFrame() {
                super.onRenderedFirstFrame()
//                log("onRenderedFirstFrame")
                onRenderedFirstFrame?.invoke()
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                super.onAudioSessionIdChanged(audioSessionId)
//                log("onAudioSessionIdChanged audioSessionId $audioSessionId")
                onAudioSessionIdChanged?.invoke(audioSessionId)
            }

            override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
                super.onAudioAttributesChanged(audioAttributes)
//                log("onAudioAttributesChanged audioAttributes ${audioAttributes.allowedCapturePolicy}")
                onAudioAttributesChanged?.invoke(audioAttributes)
            }

            override fun onVolumeChanged(volume: Float) {
                super.onVolumeChanged(volume)
//                log("onVolumeChanged volume $volume")
                if (volume == 0f) {
                    btVolumeUZ?.setSrcDrawableDisabledCanTouch()
                } else {
                    btVolumeUZ?.setSrcDrawableEnabled()
                }
                onVolumeChanged?.invoke(volume)
            }

            override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
                super.onSkipSilenceEnabledChanged(skipSilenceEnabled)
//                log("onSkipSilenceEnabledChanged $skipSilenceEnabled")
                onSkipSilenceEnabledChanged?.invoke(skipSilenceEnabled)
            }

            override fun onCues(cues: MutableList<Cue>) {
                super.onCues(cues)
                onCues?.invoke(cues)
            }

            override fun onMetadata(metadata: com.google.android.exoplayer2.metadata.Metadata) {
                super.onMetadata(metadata)
//                log("onMetadata ${metadata.length()}")
                onMetadata?.invoke(metadata)
            }

            override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
                super.onDeviceInfoChanged(deviceInfo)
//                log("onDeviceInfoChanged ${deviceInfo.minVolume} ${deviceInfo.maxVolume}")
                onDeviceInfoChanged?.invoke(deviceInfo)
            }

            override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
                super.onDeviceVolumeChanged(volume, muted)
//                log("onDeviceVolumeChanged $volume, $muted")
                onDeviceVolumeChanged?.invoke(volume, muted)
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                super.onTimelineChanged(timeline, reason)
//                log("onTimelineChanged ${timeline.periodCount}")
                onTimelineChanged?.invoke(timeline, reason)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
//                log("onMediaItemTransition ${mediaItem?.mediaId}")
                onMediaItemTransition?.invoke(mediaItem, reason)
            }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray,
                trackSelections: TrackSelectionArray
            ) {
                super.onTracksChanged(trackGroups, trackSelections)
//                log("onTracksChanged")
                if (trackGroups !== lastSeenTrackGroupArray) {
                    val mappedTrackInfo = trackSelector?.currentMappedTrackInfo
                    if (mappedTrackInfo != null) {
                        if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO) == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS
                        ) {
                            throw Exception("Media includes video tracks, but none are playable by this device")
                        }
                        if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO) == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS
                        ) {
                            throw Exception("Media includes audio tracks, but none are playable by this device")
                        }
                    }
                    lastSeenTrackGroupArray = trackGroups
                }

                onTracksChanged?.invoke(trackGroups, trackSelections)
            }

//            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
//                super.onMediaMetadataChanged(mediaMetadata)
//                log("onMediaMetadataChanged")
//            }

//            override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
//                super.onPlaylistMetadataChanged(mediaMetadata)
//                log("onPlaylistMetadataChanged")
//            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                super.onIsLoadingChanged(isLoading)
//                log("onIsLoadingChanged isLoading $isLoading")
                onIsLoadingChanged?.invoke(isLoading)
            }

            override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
                super.onAvailableCommandsChanged(availableCommands)
//                log("onAvailableCommandsChanged")
                onAvailableCommandsChanged?.invoke(availableCommands)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                when (playbackState) {
                    Player.STATE_BUFFERING -> {
//                        log("onPlaybackStateChanged STATE_BUFFERING")
                        showProgress()
                    }
                    Player.STATE_IDLE -> {
//                        log("onPlaybackStateChanged STATE_IDLE")
                        showProgress()
                    }
                    Player.STATE_ENDED -> {
//                        log("onPlaybackStateChanged STATE_ENDED")
                        onPlayerEnded()
                    }
                    Player.STATE_READY -> {
//                        log("onPlaybackStateChanged STATE_READY")
                        hideProgress()
                        updateTvDuration()
                        if (player?.playWhenReady == true) {
                            timeBarUZ?.hidePreview()
                        }
                        if (context is Activity) {
                            (context as Activity).setResult(Activity.RESULT_OK)
                        }

                        if (!isFirstStateReady) {
                            setFirstStateReady(true)
                            updateUIDependOnLiveStream()
                        }
                    }
                }
                onPlayerStateChanged?.invoke(playbackState)
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
//                log("onPlayWhenReadyChanged playWhenReady $playWhenReady")
                onPlayWhenReadyChanged?.invoke(playWhenReady, reason)
            }

//            override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
//                super.onPlaybackSuppressionReasonChanged(playbackSuppressionReason)
//                log("onPlaybackSuppressionReasonChanged playbackSuppressionReason $playbackSuppressionReason")
//            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    UZViewUtils.goneViews(btPlayUZ)
                    UZViewUtils.visibleViews(btPauseUZ)
                } else {
                    UZViewUtils.visibleViews(btPlayUZ)
                    UZViewUtils.goneViews(btPauseUZ)
                }
                onIsPlayingChanged?.invoke(isPlaying)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                super.onRepeatModeChanged(repeatMode)
//                log("onRepeatModeChanged repeatMode $repeatMode")
                onRepeatModeChanged?.invoke(repeatMode)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                super.onShuffleModeEnabledChanged(shuffleModeEnabled)
//                log("onShuffleModeEnabledChanged shuffleModeEnabled $shuffleModeEnabled")
                onShuffleModeEnabledChanged?.invoke(shuffleModeEnabled)
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
//                log("onPlayerError error $error")
                hideProgress()
                handleError(ErrorUtils.exceptionPlayback())

                if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                    player?.seekToDefaultPosition()
                    player?.prepare()
                }

                onPlayerError?.invoke(error)
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
//                log("onPlayerErrorChanged error $error")
                onPlayerErrorChanged?.invoke(error)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
//                log("onPositionDiscontinuity oldPosition ${oldPosition.positionMs}, newPosition ${newPosition.positionMs}, reason $reason")
                onPositionDiscontinuity?.invoke(oldPosition, newPosition, reason)
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                super.onPlaybackParametersChanged(playbackParameters)
//                log("onPlaybackParametersChanged playbackParameters ${playbackParameters.speed}")
                onPlaybackParametersChanged?.invoke(playbackParameters)
            }

            override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
                super.onSeekBackIncrementChanged(seekBackIncrementMs)
//                log("onSeekBackIncrementChanged seekBackIncrementMs $seekBackIncrementMs")
                onSeekBackIncrementChanged?.invoke(seekBackIncrementMs)
            }

            override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
                super.onSeekForwardIncrementChanged(seekForwardIncrementMs)
//                log("onSeekForwardIncrementChanged seekForwardIncrementMs $seekForwardIncrementMs")
                onSeekForwardIncrementChanged?.invoke(seekForwardIncrementMs)
            }

            override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Int) {
                super.onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs)
//                log("onMaxSeekToPreviousPositionChanged maxSeekToPreviousPositionMs $maxSeekToPreviousPositionMs")
                onMaxSeekToPreviousPositionChanged?.invoke(maxSeekToPreviousPositionMs)
            }

//            override fun onEvents(player: Player, events: Player.Events) {
//                super.onEvents(player, events)
//                log("onEvents")
//            }

        })
        player?.addAnalyticsListener(object : AnalyticsListener {
            override fun onShuffleModeChanged(
                eventTime: AnalyticsListener.EventTime,
                shuffleModeEnabled: Boolean
            ) {
                super.onShuffleModeChanged(eventTime, shuffleModeEnabled)
                onShuffleModeChanged?.invoke(eventTime, shuffleModeEnabled)
            }

            override fun onLoadStarted(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)
                onLoadStarted?.invoke(eventTime, loadEventInfo, mediaLoadData)
            }

            override fun onLoadCompleted(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                super.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData)
                onLoadCompleted?.invoke(eventTime, loadEventInfo, mediaLoadData)
            }

            override fun onLoadCanceled(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                super.onLoadCanceled(eventTime, loadEventInfo, mediaLoadData)
                onLoadCanceled?.invoke(eventTime, loadEventInfo, mediaLoadData)
            }

            override fun onLoadError(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData,
                error: IOException,
                wasCanceled: Boolean
            ) {
                super.onLoadError(eventTime, loadEventInfo, mediaLoadData, error, wasCanceled)
                onLoadError?.invoke(eventTime, loadEventInfo, mediaLoadData, error, wasCanceled)
            }

            override fun onDownstreamFormatChanged(
                eventTime: AnalyticsListener.EventTime,
                mediaLoadData: MediaLoadData
            ) {
                super.onDownstreamFormatChanged(eventTime, mediaLoadData)
                onDownstreamFormatChanged?.invoke(eventTime, mediaLoadData)
            }

            override fun onUpstreamDiscarded(
                eventTime: AnalyticsListener.EventTime,
                mediaLoadData: MediaLoadData
            ) {
                super.onUpstreamDiscarded(eventTime, mediaLoadData)
                onUpstreamDiscarded?.invoke(eventTime, mediaLoadData)
            }

            override fun onBandwidthEstimate(
                eventTime: AnalyticsListener.EventTime,
                totalLoadTimeMs: Int,
                totalBytesLoaded: Long,
                bitrateEstimate: Long
            ) {
                super.onBandwidthEstimate(
                    eventTime,
                    totalLoadTimeMs,
                    totalBytesLoaded,
                    bitrateEstimate
                )
                onBandwidthEstimate?.invoke(
                    eventTime,
                    totalLoadTimeMs,
                    totalBytesLoaded,
                    bitrateEstimate
                )
            }

            override fun onAudioEnabled(
                eventTime: AnalyticsListener.EventTime,
                decoderCounters: DecoderCounters
            ) {
                super.onAudioEnabled(eventTime, decoderCounters)
                onAudioEnabled?.invoke(eventTime, decoderCounters)
            }

            override fun onAudioDecoderInitialized(
                eventTime: AnalyticsListener.EventTime,
                decoderName: String,
                initializedTimestampMs: Long,
                initializationDurationMs: Long
            ) {
                super.onAudioDecoderInitialized(
                    eventTime,
                    decoderName,
                    initializedTimestampMs,
                    initializationDurationMs
                )
                onAudioDecoderInitialized?.invoke(
                    eventTime,
                    decoderName,
                    initializedTimestampMs,
                    initializationDurationMs
                )
            }

            override fun onAudioInputFormatChanged(
                eventTime: AnalyticsListener.EventTime,
                format: Format,
                decoderReuseEvaluation: DecoderReuseEvaluation?
            ) {
                super.onAudioInputFormatChanged(eventTime, format, decoderReuseEvaluation)
                onAudioInputFormatChanged?.invoke(eventTime, format, decoderReuseEvaluation)
            }

            override fun onAudioPositionAdvancing(
                eventTime: AnalyticsListener.EventTime,
                playoutStartSystemTimeMs: Long
            ) {
                super.onAudioPositionAdvancing(eventTime, playoutStartSystemTimeMs)
                onAudioPositionAdvancing?.invoke(eventTime, playoutStartSystemTimeMs)
            }

            override fun onAudioUnderrun(
                eventTime: AnalyticsListener.EventTime,
                bufferSize: Int,
                bufferSizeMs: Long,
                elapsedSinceLastFeedMs: Long
            ) {
                super.onAudioUnderrun(eventTime, bufferSize, bufferSizeMs, elapsedSinceLastFeedMs)
                onAudioUnderrun?.invoke(eventTime, bufferSize, bufferSizeMs, elapsedSinceLastFeedMs)
            }

            override fun onAudioDecoderReleased(
                eventTime: AnalyticsListener.EventTime,
                decoderName: String
            ) {
                super.onAudioDecoderReleased(eventTime, decoderName)
                onAudioDecoderReleased?.invoke(eventTime, decoderName)
            }

            override fun onAudioDisabled(
                eventTime: AnalyticsListener.EventTime,
                decoderCounters: DecoderCounters
            ) {
                super.onAudioDisabled(eventTime, decoderCounters)
                onAudioDisabled?.invoke(eventTime, decoderCounters)
            }

            override fun onAudioSinkError(
                eventTime: AnalyticsListener.EventTime,
                audioSinkError: java.lang.Exception
            ) {
                super.onAudioSinkError(eventTime, audioSinkError)
                onAudioSinkError?.invoke(eventTime, audioSinkError)
            }

            override fun onAudioCodecError(
                eventTime: AnalyticsListener.EventTime,
                audioCodecError: java.lang.Exception
            ) {
                super.onAudioCodecError(eventTime, audioCodecError)
                onAudioCodecError?.invoke(eventTime, audioCodecError)
            }

            override fun onVideoEnabled(
                eventTime: AnalyticsListener.EventTime,
                decoderCounters: DecoderCounters
            ) {
                super.onVideoEnabled(eventTime, decoderCounters)
                onVideoEnabled?.invoke(eventTime, decoderCounters)
            }

            override fun onVideoDecoderInitialized(
                eventTime: AnalyticsListener.EventTime,
                decoderName: String,
                initializedTimestampMs: Long,
                initializationDurationMs: Long
            ) {
                super.onVideoDecoderInitialized(
                    eventTime,
                    decoderName,
                    initializedTimestampMs,
                    initializationDurationMs
                )
                onVideoDecoderInitialized?.invoke(
                    eventTime,
                    decoderName,
                    initializedTimestampMs,
                    initializationDurationMs
                )
            }

            override fun onVideoInputFormatChanged(
                eventTime: AnalyticsListener.EventTime,
                format: Format,
                decoderReuseEvaluation: DecoderReuseEvaluation?
            ) {
                super.onVideoInputFormatChanged(eventTime, format, decoderReuseEvaluation)
                onVideoInputFormatChanged?.invoke(eventTime, format, decoderReuseEvaluation)
            }

            override fun onDroppedVideoFrames(
                eventTime: AnalyticsListener.EventTime,
                droppedFrames: Int,
                elapsedMs: Long
            ) {
                super.onDroppedVideoFrames(eventTime, droppedFrames, elapsedMs)
                onDroppedVideoFrames?.invoke(eventTime, droppedFrames, elapsedMs)
            }

            override fun onVideoDecoderReleased(
                eventTime: AnalyticsListener.EventTime,
                decoderName: String
            ) {
                super.onVideoDecoderReleased(eventTime, decoderName)
                onVideoDecoderReleased?.invoke(eventTime, decoderName)
            }

            override fun onVideoDisabled(
                eventTime: AnalyticsListener.EventTime,
                decoderCounters: DecoderCounters
            ) {
                super.onVideoDisabled(eventTime, decoderCounters)
                onVideoDisabled?.invoke(eventTime, decoderCounters)
            }

            override fun onVideoFrameProcessingOffset(
                eventTime: AnalyticsListener.EventTime,
                totalProcessingOffsetUs: Long,
                frameCount: Int
            ) {
                super.onVideoFrameProcessingOffset(eventTime, totalProcessingOffsetUs, frameCount)
                onVideoFrameProcessingOffset?.invoke(eventTime, totalProcessingOffsetUs, frameCount)
            }

            override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
                super.onPlayerReleased(eventTime)
                onPlayerReleased?.invoke(eventTime)
            }
        })
        initProgressChange()
    }

    private fun initProgressChange() {
        fun observable(): Observable<out Long> {
            return Observable.interval(0, 1, TimeUnit.SECONDS)
        }

        fun observer(): DisposableObserver<Long> {
            return object : DisposableObserver<Long>() {
                override fun onNext(value: Long) {
                    player?.let { p ->
                        if (p.duration > 0) {
//                            log("initProgressChange ${p.currentPosition} ${p.duration} isPlayingAd: ${isPlayingAd()}")
                            updateUIIbRewIconDependOnProgress(
                                currentMls = p.currentPosition,
                                isCalledFromUZTimeBarEvent = false
                            )
                            onProgressChange?.invoke(p.currentPosition, p.duration, isPlayingAd())
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onComplete() {
                }
            }
        }
        compositeDisposable.add(
            observable()
                .subscribeOn(Schedulers.io()) // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(observer())
        )
    }

    private fun handleNetworkChange(isConnected: Boolean) {
        if (isConnected) {
            hideController()
            retry()
        } else {
            notifyError(ErrorUtils.exceptionNoConnection())
        }
        onNetworkChange?.invoke(isConnected)
    }

    override fun onOrientationChange(orientation: Int) {
        //270 land trai
        //0 portrait duoi
        //90 land phai
        //180 portrait tren
        val isDeviceAutoRotation = UZViewUtils.isRotationPossible(context)
        if (orientation == 90 || orientation == 270) {
            if (isDeviceAutoRotation && !isLandscape) {
                if (!isAlwaysPortraitScreen) {
                    if (context is Activity) {
                        UZViewUtils.changeScreenLandscape(
                            activity = (context as Activity),
                            orientation = orientation
                        )
                    }
                }
            }
        } else {
            if (isDeviceAutoRotation && isLandscape) {
                if (!isAlwaysPortraitScreen) {
                    if (context is Activity) {
                        UZViewUtils.changeScreenPortrait(activity = (context as Activity))
                    }
                }
            }
        }
    }

    fun setPIPModeEnabled(isPIPModeEnabled: Boolean) {
        this.isPIPModeEnabled = isPIPModeEnabled
        if (isPIPEnable) {
            btPipUZ?.visibility = View.VISIBLE
        } else {
            btPipUZ?.visibility = View.GONE
        }
    }

    fun isLandscapeScreen(): Boolean {
        return isLandscape
    }

    fun setAutoReplay(isAutoReplay: Boolean) {
        this.isAutoReplay = isAutoReplay
    }

    fun isAutoReplay(): Boolean {
        return this.isAutoReplay
    }

    fun setAlwaysPortraitScreen(isAlwaysPortraitScreen: Boolean) {
        this.isAlwaysPortraitScreen = isAlwaysPortraitScreen
    }

    fun isAlwaysPortraitScreen(): Boolean {
        return isAlwaysPortraitScreen
    }

    fun isViewCreated(): Boolean {
        return this.isViewCreated
    }

    fun setUseUZDragView(useUZDragView: Boolean) {
        uzPlayerView?.setUseUZDragView(useUZDragView)
    }

    fun isPlayerControllerAlwayVisible(): Boolean {
        return isPlayerControllerAlwayVisible
    }

    fun retry() {
        player?.prepare()
        player?.playWhenReady = true
    }

    fun isEnableDoubleTapToSeek(): Boolean {
        return isEnableDoubleTapToSeek
    }

    fun setEnableDoubleTapToSeek(isEnableDoubleTapToSeek: Boolean) {
        this.isEnableDoubleTapToSeek = isEnableDoubleTapToSeek
    }

    fun setShowLayoutDebug(isShowLayoutDebug: Boolean) {
        this.isShowLayoutDebug = isShowLayoutDebug
        if (isShowLayoutDebug) {
            layoutDebug.visibility = VISIBLE
        } else {
            layoutDebug.visibility = GONE
        }
    }

    fun isShowLayoutDebug(): Boolean {
        return this.isShowLayoutDebug
    }

    fun getSkinId(): Int {
        return this.skinId
    }

    fun isInPipMode(): Boolean {
        return isInPipMode
    }

    private class PlayerErrorMessageProvider(val context: Context) :
        ErrorMessageProvider<PlaybackException> {
        override fun getErrorMessage(e: PlaybackException): Pair<Int, String> {
            var errorString: String = context.getString(R.string.error_generic)
            val cause = e.cause
            if (cause is DecoderInitializationException) {
                // Special case for decoder initialization failures.
                if (cause.codecInfo == null) {
                    when {
                        cause.cause is DecoderQueryException -> {
                            errorString = context.getString(R.string.error_querying_decoders)
                        }
                        cause.secureDecoderRequired -> {
                            errorString = context.getString(
                                R.string.error_no_secure_decoder,
                                cause.mimeType
                            )
                        }
                        else -> {
                            errorString = context.getString(
                                R.string.error_no_decoder,
                                cause.mimeType
                            )
                        }
                    }
                } else {
                    errorString = context.getString(
                        R.string.error_instantiating_decoder,
                        cause.codecInfo?.name
                    )
                }
            }
            return Pair.create(0, errorString)
        }
    }

    private fun clearStartPosition() {
        startAutoPlay = true
        startWindow = C.INDEX_UNSET
        startPosition = C.TIME_UNSET
    }

    private fun initializePlayer(): Boolean {
        dataSourceFactory?.let { dtf ->
            mediaItems = createMediaItems()
            if (mediaItems.isNullOrEmpty()) {
                return false
            }
            if (player == null) {
                val preferExtensionDecoders = false
                val renderersFactory =
                    DemoUtil.buildRenderersFactory(context, preferExtensionDecoders)
                val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dtf)
                    .setAdsLoaderProvider { adsConfiguration: AdsConfiguration? ->
                        adsConfiguration?.let {
                            this.getAdsLoader(it)
                        }
                    }
                    .setAdViewProvider(uzPlayerView)
                trackSelector = DefaultTrackSelector(context)
                trackSelectorParameters?.let {
                    trackSelector?.parameters = it
                }
                lastSeenTrackGroupArray = null
                trackSelector?.let {
                    player = SimpleExoPlayer.Builder(context, renderersFactory)
                        .setMediaSourceFactory(mediaSourceFactory)
                        .setTrackSelector(it)
                        .build()
                }

                player?.let {
                    it.addAnalyticsListener(EventLogger(trackSelector))
                    it.setAudioAttributes(AudioAttributes.DEFAULT, true)
                    it.playWhenReady = startAutoPlay
                }

                uzPlayerView?.player = player
            }

            val haveStartPosition = startWindow != C.INDEX_UNSET
            if (haveStartPosition) {
                player?.seekTo(startWindow, startPosition)
            }
            mediaItems?.let {
//                log("initializePlayer haveStartPosition $haveStartPosition")
                player?.setMediaItems(it, !haveStartPosition)
            }
            player?.prepare()
            addListener()
            return true
        }
        return false
    }

    private fun createMediaItems(): List<MediaItem> {
        val mediaItems = ArrayList<MediaItem>()
        if (uzPlayback == null) {
            return mediaItems
        }
        uzPlayback?.let { uzp ->
            val builder = Builder()
            builder
                .setUri(Uri.parse(uzp.linkPlay))
                .setAdTagUri(uzp.urlIMAAd)
            mediaItems.add(builder.build())

            var hasAds = false
            for (i in mediaItems.indices) {
                val mediaItem = mediaItems[i]
                if (!Util.checkCleartextTrafficPermitted(mediaItem)) {
                    throw Exception("Cleartext HTTP traffic not permitted. See https://exoplayer.dev/issues/cleartext-not-permitted")
                }
                if (context is Activity) {
                    if (Util.maybeRequestReadExternalStoragePermission(
                            context as Activity,
                            mediaItem
                        )
                    ) {
                        return emptyList()
                    }
                }

                val drmConfiguration =
                    Assertions.checkNotNull(mediaItem.playbackProperties).drmConfiguration
                if (drmConfiguration != null) {
                    if (Util.SDK_INT < 18) {
                        throw Exception("DRM content not supported on API levels below 18")
                    } else if (!FrameworkMediaDrm.isCryptoSchemeSupported(drmConfiguration.uuid)) {
                        throw  Exception("This device does not support the required DRM scheme")
                    }
                }
                hasAds = hasAds or (mediaItem.playbackProperties?.adsConfiguration != null)
            }
            if (!hasAds) {
                releaseAdsLoader()
            }
        }
        return mediaItems
    }

    private fun releaseAdsLoader() {
        if (adsLoader != null) {
            adsLoader?.release()
            adsLoader = null
            uzPlayerView?.overlayFrameLayout?.removeAllViews()
        }
    }

    private fun getAdsLoader(adsConfiguration: AdsConfiguration): AdsLoader? {
        // The ads loader is reused for multiple playbacks, so that ad playback can resume.
        log("getAdsLoader adTagUri ${adsConfiguration.adTagUri}, adsId ${adsConfiguration.adsId}")
        if (adsLoader == null) {
            adsLoader = ImaAdsLoader.Builder(context).build()
        }
        adsLoader?.setPlayer(player)
        return adsLoader
    }

}
