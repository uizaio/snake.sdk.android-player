package com.uiza.sdk.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.util.Pair
import android.util.Rational
import android.view.*
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import com.ezralazuardy.orb.Orb
import com.ezralazuardy.orb.OrbHelper
import com.ezralazuardy.orb.OrbListener
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.uiza.sdk.R
import com.uiza.sdk.UZPlayer
import com.uiza.sdk.UZPlayer.Companion.elapsedTime
import com.uiza.sdk.dialog.hq.UZItem
import com.uiza.sdk.dialog.hq.UZTrackSelectionView
import com.uiza.sdk.dialog.setting.OnToggleChangeListener
import com.uiza.sdk.dialog.setting.SettingAdapter
import com.uiza.sdk.dialog.setting.SettingItem
import com.uiza.sdk.dialog.speed.Callback
import com.uiza.sdk.dialog.speed.Speed
import com.uiza.sdk.dialog.speed.UZSpeedDialog
import com.uiza.sdk.exceptions.ErrorUtils
import com.uiza.sdk.exceptions.UZException
import com.uiza.sdk.interfaces.DebugCallback
import com.uiza.sdk.interfaces.UZManagerObserver
import com.uiza.sdk.interfaces.UZProgressListener
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.observers.SensorOrientationChangeNotifier
import com.uiza.sdk.utils.*
import com.uiza.sdk.utils.ConvertUtils.getProgramDateTime
import com.uiza.sdk.view.UZPlayerView.OnDoubleTap
import com.uiza.sdk.widget.UZImageButton
import com.uiza.sdk.widget.UZPreviewTimeBar
import com.uiza.sdk.widget.UZTextView
import com.uiza.sdk.widget.previewseekbar.PreviewLoader
import com.uiza.sdk.widget.previewseekbar.PreviewView
import com.uiza.sdk.widget.previewseekbar.PreviewView.OnPreviewChangeListener
import kotlinx.android.synthetic.main.layout_uz_ima_video_core.view.*
import java.util.*


class UZVideoView : RelativeLayout,
    UZManagerObserver,
    SensorOrientationChangeNotifier.Listener,
    View.OnClickListener {

    companion object {
        private const val HYPHEN = "-"
        private const val FAST_FORWARD_REWIND_INTERVAL = 10000L // 10s
        private const val DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS = 5000 // 5s
        const val DEFAULT_TARGET_DURATION_MLS = 2000L // 2s
        private const val ARG_VIDEO_POSITION = "ARG_VIDEO_POSITION"
    }

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    private var targetDurationMls = DEFAULT_TARGET_DURATION_MLS
    private var defaultSeekValue = FAST_FORWARD_REWIND_INTERVAL
    private var playerManager: UZPlayerManager? = null

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
    override var playerView: UZPlayerView? = null

    override var isAutoStart: Boolean = Constants.DF_PLAYER_IS_AUTO_START
        set(isAutoStart) {
            field = isAutoStart
            updateUIButtonPlayPauseDependOnIsAutoStart()
        }

    private var autoMoveToLiveEdge = false
    private var isInPipMode = false
    private var isPIPModeEnabled = false
    private var isUSeControllerRestorePip = false
    private var positionPIPPlayer = 0L
    private var isAutoReplay = false
    private var isFreeSize = false
    private var isPlayerControllerAlwayVisible = false
    private var isControllerHideOnTouch = true
    private var isSetFirstRequestFocusDoneForTV = false
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
    var onTimeShiftChange: ((timeShiftOn: Boolean) -> Unit)? = null
    var onScreenRotate: ((isLandscape: Boolean) -> Unit)? = null
    var onError: ((e: UZException) -> Unit)? = null
    var onPlayerStateChanged: ((playWhenReady: Boolean, playbackState: Int) -> Unit)? = null
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

    var onBufferProgress: ((bufferedPosition: Long, bufferedPercentage: Int, duration: Long) -> Unit)? =
        null
    var onVideoProgress: ((currentMls: Long, s: Int, duration: Long, percent: Int) -> Unit)? =
        null

    private var orb: Orb? = null

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

//        log("onAttachedToWindow isViewCreated $isViewCreated")
        if (!isViewCreated) {
            onCreateView()
        }
    }

    private fun onCreateView() {
        inflate(context, R.layout.layout_uz_ima_video_core, this)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        if (inflater == null) {
//            log("onCreateView cannot inflater view")
            throw NullPointerException("Cannot inflater view")
        } else {
            playerView = inflater.inflate(skinId, null) as UZPlayerView?
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT)
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            layoutParams.addRule(CENTER_IN_PARENT, TRUE)

            playerView?.let {
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

                layoutRootView.addView(it)
            }

            findViews()
            resizeContainerView()
        }
        updateUIEachSkin()
        setMarginPreviewTimeBar()
        updateUISizeThumbnailTimeBar()
        isViewCreated = true

//        log("onCreateView isViewCreated $isViewCreated")
        playerView?.let {
//            log("onCreateView invoke")
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
//        updateUIPositionOfProgressBar()

        playerView?.let { pv ->
            playerView?.useController =
                false//khong cho dung controller cho den khi isFirstStateReady == true
            pv.setOnDoubleTap(object : OnDoubleTap {
                override fun onDoubleTapFinished() {
                    onDoubleTapFinished?.invoke()
                }

                override fun onDoubleTapProgressDown(posX: Float, posY: Float) {
                    onDoubleTapProgressDown?.invoke(posX, posY)
                }

                override fun onDoubleTapStarted(posX: Float, posY: Float) {
                    onDoubleTapStarted?.invoke(posX, posY)
                }

                override fun onDoubleTapProgressUp(posX: Float, posY: Float) {
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

            //If auto start true, show button play and gone button pause
            UZViewUtils.goneViews(btPlayUZ)

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
            setSize(width = videoWidth, height = videoHeight)
        }
    }

    var controllerAutoShow: Boolean
        get() = playerView?.controllerAutoShow ?: false
        set(isAutoShowController) {
            playerView?.controllerAutoShow = isAutoShowController
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

    val videoProfileW: Int
        get() = playerManager?.videoProfileW ?: 0

    val videoProfileH: Int
        get() = playerManager?.videoProfileH ?: 0

    fun setResizeMode(resizeMode: Int) {
        try {
            playerView?.resizeMode = resizeMode
        } catch (e: IllegalStateException) {
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

    val player: SimpleExoPlayer?
        get() = playerManager?.getPlayer()

    fun seekTo(positionMs: Long) {
        playerManager?.seekTo(positionMs)
    }

    fun play(uzPlayback: UZPlayback): Boolean {
        if (!ConnectivityUtils.isConnected(context)) {
            notifyError(ErrorUtils.exceptionNoConnection())
            return false
        }
        this.uzPlayback = uzPlayback
        initPlayback()
        return true
    }

    fun resume() {
        playerManager?.resume()
        UZViewUtils.goneViews(btPlayUZ)
        btPauseUZ?.let {
            UZViewUtils.visibleViews(it)
            it.requestFocus()
        }
        keepScreenOn = true
    }

    fun pause() {
        playerManager?.pause()
        UZViewUtils.goneViews(btPauseUZ)
        keepScreenOn = false
        btPlayUZ?.let {
            UZViewUtils.visibleViews(it)
            it.requestFocus()
        }
    }

    val videoWidth: Int
        get() = playerManager?.videoWidth ?: 0

    val videoHeight: Int
        get() = playerManager?.videoHeight ?: 0

    private fun initPlayback() {
        if (uzPlayback == null) {
            handleError(ErrorUtils.exceptionNoLinkPlay())
            return
        }
        val linkPlay = uzPlayback?.linkPlay
        if (linkPlay.isNullOrEmpty()) {
            handleError(ErrorUtils.exceptionNoLinkPlay())
            return
        }
        isCalledFromChangeSkin = false
        controllerShowTimeoutMs = DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS
        isOnPlayerEnded = false
        playerView?.useController =
            false//khong cho dung controller cho den khi isFirstStateReady == true
        updateUIEndScreen()
        releasePlayerManager()
        showProgress()

        initDataSource(
            linkPlay = linkPlay,
            urlIMAAd = uzPlayback?.urlIMAAd,
            poster = uzPlayback?.poster
        )
        onIsInitResult?.invoke(linkPlay)
        initPlayerManager()
    }

    private fun initPlayerManager() {
        playerManager?.let { pm ->
            pm.register(this)
            if (isRefreshFromChangeSkin) {
                pm.seekTo(currentPositionBeforeChangeSkin)
                isRefreshFromChangeSkin = false
                currentPositionBeforeChangeSkin = 0
            }
            initStatsForNerds()
        }
    }

    fun toggleStatsForNerds() {
        if (player == null) return
        val isEnableStatsForNerds =
            statsForNerdsView == null || statsForNerdsView.visibility != VISIBLE
        if (isEnableStatsForNerds) {
            UZViewUtils.visibleViews(statsForNerdsView)
        } else {
            UZViewUtils.goneViews(statsForNerdsView)
        }
    }

    private fun tryNextLinkPlay() {
        if (isLIVE) {
//            playerManager?.let {
//                it.initWithoutReset()
//                it.setRunnable()
//            }
            retry()
            setFirstStateReady(false)
            return
        }
        setFirstStateReady(false)
        releasePlayerManager()
        checkToSetUpResource()
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
        releasePlayerStats()
        releasePlayerManager()
        if (isPIPEnable) {
            if (context is Activity) {
                (context as Activity).finishAndRemoveTask()
            }
        }
        playerManager?.unregister()
        orb?.stop()
    }

    private fun releasePlayerStats() {
        player?.removeAnalyticsListener(statsForNerdsView)
    }

    private fun releasePlayerManager() {
        playerManager?.release()
    }

    fun onResumeView() {
        SensorOrientationChangeNotifier.getInstance(context)?.addListener(this)
        playerManager?.resume()
        if (positionPIPPlayer > 0L && isInPipMode) {
            seekTo(positionPIPPlayer)
        } else if (autoMoveToLiveEdge && isLIVE) {
            // try to move to the edge of livestream video
            seekToLiveEdge()
        }
    }

    val isPlaying: Boolean
        get() = player?.playWhenReady ?: false

    /**
     * Set auto move the the last window of livestream, default is false
     *
     * @param autoMoveToLiveEdge true if always seek to last livestream video, otherwise false
     */
    fun setAutoMoveToLiveEdge(autoMoveToLiveEdge: Boolean) {
        this.autoMoveToLiveEdge = autoMoveToLiveEdge
    }

    /**
     * Seek to live edge of a streaming video
     */
    fun seekToLiveEdge() {
        if (isLIVE) {
            player?.seekToDefaultPosition()
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(ARG_VIDEO_POSITION, currentPosition)
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        positionPIPPlayer = savedInstanceState.getLong(ARG_VIDEO_POSITION)
    }

    fun onPauseView() {
        positionPIPPlayer = currentPosition
        SensorOrientationChangeNotifier.getInstance(context)?.remove(this)

        // in PIP to continue
        if (!isInPipMode) {
            playerManager?.pause()

        }
    }

    fun isPlayingAd(): Boolean? {
        return playerManager?.isPlayingAd
    }

    override val isPIPEnable: Boolean
        get() = (btPipUZ != null && UZAppUtils.hasSupportPIP(context = context) && playerView?.isUseUZDragView() == false && isPIPModeEnabled)

    private fun onStopPreview(progress: Int) {
        playerManager?.seekTo(progress.toLong())
        playerManager?.resume()
        isOnPlayerEnded = false
        updateUIEndScreen()
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        playerView?.let { pv ->
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
//            updateUIPositionOfProgressBar()
            onScreenRotate?.invoke(isLandscape)
        }
    }

    override fun onClick(v: View) {
        if (v === btFullscreenUZ) {
            toggleFullscreen()
        } else if (v === btBackScreenUZ) {
            clickBackScreen()
        } else if (v === btVolumeUZ) {
            toggleVolumeMute()
        } else if (v === btSettingUZ) {
            showSettingsDialog()
        } else if (v === btPipUZ) {
            enterPIPMode()
        } else if (v.parent === layoutControls) {
            showTrackSelectionDialog(v, true)
        } else if (v === btFfwdUZ) {
            playerManager?.seekToForward(defaultSeekValue)
        } else if (v === btRewUZ) {
            playerManager?.seekToBackward(defaultSeekValue)
            if (isPlaying) {
                isOnPlayerEnded = false
                updateUIEndScreen()
            }
        } else if (v === btPauseUZ) {
            pause()
        } else if (v === btPlayUZ) {
            resume()
        } else if (v === btReplayUZ) {
            replay()
        } else if (v === btSpeedUZ) {
            showSpeed()
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
                    val aspectRatio = Rational(videoWidth, videoHeight)
                    params.setAspectRatio(aspectRatio)
                    params.setActions(listRemoteAction)
                    if (context is Activity) {
                        (context as Activity).enterPictureInPictureMode(params.build())
                    }
                } catch (e: Exception) {
                    log("enterPIPMode e $e")
                    val w: Int
                    val h: Int
                    if (videoWidth == 0 || videoHeight == 0) {
                        w = this.width
                        h = this.height
                    } else {
                        if (videoWidth > videoHeight) {
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
        get() = playerView?.controllerShowTimeoutMs ?: -1
        set(controllerShowTimeoutMs) {
            playerView?.controllerShowTimeoutMs = controllerShowTimeoutMs
        }
    val isPlayerControllerShowing: Boolean
        get() = playerView?.isControllerVisible ?: false

    fun showController() {
        playerView?.showController()
    }

    fun hideController() {
        if (isPlayerControllerAlwayVisible) {
            return
        }
        playerView?.hideController()
    }

    fun setControllerHideOnTouch(controllerHideOnTouch: Boolean) {
        this.isControllerHideOnTouch = controllerHideOnTouch
        playerView?.controllerHideOnTouch = controllerHideOnTouch
    }

    val controllerHideOnTouch: Boolean
        get() = playerView?.controllerHideOnTouch ?: false

    fun isUseController(): Boolean {
        return playerView?.useController ?: false
    }

    fun setUseController(useController: Boolean): Boolean {
        if (!isFirstStateReady) {
            log("setUseController() can be applied if the player state is Player.STATE_READY")
            return false
        }
        playerView?.useController = useController
        return true
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        if (manifest is HlsManifest) {
            val playlist = manifest.mediaPlaylist
            targetDurationMls = C.usToMs(playlist.targetDurationUs)
            // From the current playing frame to end time of chunk
            val timeToEndChunk = duration - currentPosition
            val extProgramDateTime = getProgramDateTime(
                playlist = playlist,
                timeToEndChunk = timeToEndChunk
            )
            if (extProgramDateTime == C.INDEX_UNSET.toLong()) {
                hideTextLiveStreamLatency()
                return
            }
            val elapsedTime = SystemClock.elapsedRealtime() - elapsedTime
            val currentTime = System.currentTimeMillis() + elapsedTime
            val latency = currentTime - extProgramDateTime
            updateLiveStreamLatency(latency)
        } else {
            hideTextLiveStreamLatency()
        }
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        hideProgress()
        handleError(ErrorUtils.exceptionPlayback())
        if (ConnectivityUtils.isConnected(context)) {
            tryNextLinkPlay()
        } else {
            pause()
        }
    }

    override fun onPlayerEnded() {
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

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING, Player.STATE_IDLE -> {
                showProgress()
            }
            Player.STATE_ENDED -> {
                onPlayerEnded()
            }
            Player.STATE_READY -> {
                hideProgress()
                updateTvDuration()
                updateTimeBarWithTimeShiftStatus()
                if (playWhenReady) {
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
        onPlayerStateChanged?.invoke(playWhenReady, playbackState)
    }

    fun replay() {
        if (playerManager == null) {
            return
        }
        val result = playerManager?.seekTo(0)
        if (result == true) {
            isSetFirstRequestFocusDoneForTV = false
            isOnPlayerEnded = false
            updateUIEndScreen()
        }
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
        playerView?.toggleShowHideController()
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
            return playerManager != null && playerManager?.isLIVE == true
        }

    val isVOD: Boolean
        get() {
            return playerManager != null && playerManager?.isVOD == true
        }

    fun getDebugString(): String? {
        return playerManager?.debugString
    }

    var volume: Float
        get() = playerManager?.volume ?: -1F
        set(volume) {
            playerManager?.let { pm ->
                pm.volume = volume
                if (pm.volume != 0f) {
                    btVolumeUZ?.setSrcDrawableEnabled()
                } else {
                    btVolumeUZ?.setSrcDrawableDisabledCanTouch()
                }
            }
        }
    private var volumeToggle = 0f

    fun toggleVolumeMute() {
        playerManager?.let { pm ->
            if (pm.volume == 0f) {
                volume = volumeToggle
                btVolumeUZ?.setSrcDrawableEnabled()
            } else {
                volumeToggle = volume
                volume = 0f
                btVolumeUZ?.setSrcDrawableDisabledCanTouch()
            }
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
        player?.setPlaybackParameters(playbackParameters)
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

    //If auto start true, show button play and gone button pause
    //if not, gone button play and show button pause
    private fun updateUIButtonPlayPauseDependOnIsAutoStart() {
        if (isAutoStart) {
            UZViewUtils.goneViews(btPlayUZ)
            btPauseUZ?.let { ib ->
                UZViewUtils.visibleViews(ib)
                if (!isSetFirstRequestFocusDoneForTV) {
                    ib.requestFocus() //set first request focus if using player for TV
                    isSetFirstRequestFocusDoneForTV = true
                }
            }
        } else {
            if (isPlaying) {
                UZViewUtils.goneViews(btPlayUZ)
                btPauseUZ?.let { ib ->
                    UZViewUtils.visibleViews(ib)
                    if (!isSetFirstRequestFocusDoneForTV) {
                        ib.requestFocus() //set first request focus if using player for TV
                        isSetFirstRequestFocusDoneForTV = true
                    }
                }
            } else {
                btPlayUZ?.let { ib ->
                    UZViewUtils.visibleViews(ib)
                    if (!isSetFirstRequestFocusDoneForTV) {
                        ib.requestFocus() //set first request focus if using player for TV
                        isSetFirstRequestFocusDoneForTV = true
                    }
                }
                UZViewUtils.goneViews(btPauseUZ)
            }
        }
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

//    private fun updateUIPositionOfProgressBar() {
//        playerView?.let { pv ->
//            postDelayed({
//                val marginL = pv.measuredWidth / 2 - pb.measuredWidth / 2
//                val marginT = pv.measuredHeight / 2 - pb.measuredHeight / 2
//                UZViewUtils.setMarginPx(view = pb, l = marginL, t = marginT, r = 0, b = 0)
//            }, 10)
//        }
//    }

    /*
     ** change skin of player (realtime)
     * return true if success
     */
    fun changeSkin(@LayoutRes skinId: Int): Boolean {
        if (playerView?.isUseUZDragView() == true) {
            throw IllegalArgumentException(resources.getString(R.string.error_change_skin_with_uzdragview))
        }
        if (playerManager == null || !isFirstStateReady || isOnPlayerEnded) {
            return false
        }
        if (playerManager?.isPlayingAd == true) {
            notifyError(ErrorUtils.exceptionChangeSkin())
            return false
        }
        this.skinId = skinId
        UZPlayer.skinDefault = skinId
        isRefreshFromChangeSkin = true
        isCalledFromChangeSkin = true

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        if (inflater != null) {
//            layoutRootView.removeView(playerView)
//            layoutRootView.requestLayout()

            playerView = inflater.inflate(skinId, null) as UZPlayerView?
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            layoutParams.addRule(CENTER_IN_PARENT, TRUE)
            playerView?.let {
                it.layoutParams = layoutParams
                layoutRootView.addView(it)
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
//            return
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

    //FOR TV
    fun updateUIFocusChange(view: View, isFocus: Boolean) {
        when (view) {
            is UZImageButton -> {
                UZViewUtils.updateUIFocusChange(
                    view = view,
                    isFocus = isFocus,
                    resHasFocus = R.drawable.background_tv_has_focus_uz,
                    resNoFocus = R.drawable.background_tv_no_focus_uz
                )
                view.clearColorFilter()
            }
            is Button -> {
                UZViewUtils.updateUIFocusChange(
                    view = view,
                    isFocus = isFocus,
                    resHasFocus = R.drawable.background_tv_has_focus_uz,
                    resNoFocus = R.drawable.background_tv_no_focus_uz
                )
            }
            is UZPreviewTimeBar -> {
                UZViewUtils.updateUIFocusChange(
                    view = view,
                    isFocus = isFocus,
                    resHasFocus = R.drawable.background_tv_has_focus_uz_timebar,
                    resNoFocus = R.drawable.background_tv_no_focus_uz_timebar
                )
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
        log("updateUIDependOnLiveStream isLIVE $isLIVE")
        if (isLIVE) {
            UZViewUtils.goneViews(btSpeedUZ, tvDurationUZ, tvPositionUZ, btRewUZ, btFfwdUZ)
        } else {
            UZViewUtils.visibleViews(btSpeedUZ, tvDurationUZ, tvPositionUZ, btFfwdUZ, btRewUZ)
        }
        tvTitleUZ?.text = uzPlayback?.name ?: ""
        if (UZAppUtils.isTV(context)) {
            UZViewUtils.goneViews(btFullscreenUZ)
        }
    }

    private fun updateUIButtonVisibilities() {
        if (context == null) {
            return
        }
        layoutControls.removeAllViews()
        if (player == null) {
            return
        }
        val mappedTrackInfo = playerManager?.trackSelector?.currentMappedTrackInfo ?: return
        for (i in 0 until mappedTrackInfo.rendererCount) {
            val trackGroups = mappedTrackInfo.getTrackGroups(i)
            if (trackGroups.length != 0) {
                val button = Button(context)
                button.isSoundEffectsEnabled = false
                val label: Int = when (playerManager?.getPlayer()?.getRendererType(i)) {
                    C.TRACK_TYPE_AUDIO -> R.string.audio
                    C.TRACK_TYPE_VIDEO -> R.string.video
                    C.TRACK_TYPE_TEXT -> R.string.text
                    else -> continue
                }
                button.setText(label)
                button.tag = i
                button.setOnClickListener(this)
                layoutControls.addView(button)
            }
        }
    }

    private fun updateUIEndScreen() {
        playerView?.let { pv ->
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
            updateUIButtonPlayPauseDependOnIsAutoStart()
            UZViewUtils.goneViews(btReplayUZ)
        }
    }

    var dlg: Dialog? = null

    @SuppressLint("InflateParams")
    fun showSettingsDialog() {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        if (inflater != null) {
            builder.setCustomTitle(inflater.inflate(R.layout.view_header_dragview_uz, null))
        }
        val actionCount = layoutControls.childCount
        if (actionCount < 1) {
            return
        }
        val actions = ArrayList<SettingItem>()
        for (i in 0 until actionCount) {
            val v = layoutControls.getChildAt(i)
            if (v is Button) {
                actions.add(
                    SettingItem(v.text.toString())
                )
            }
        }
        if (statsForNerdsView != null) {
            actions.add(
                SettingItem(
                    resources.getString(R.string.stats),
                    statsForNerdsView.visibility == VISIBLE,
                    object : OnToggleChangeListener {
                        override fun onCheckedChanged(isChecked: Boolean): Boolean {
                            dlg?.dismiss()
                            if (isChecked) {
                                UZViewUtils.visibleViews(statsForNerdsView)
                            } else {
                                UZViewUtils.goneViews(statsForNerdsView)
                            }
                            return true
                        }
                    })
            )
        }
        playerManager?.let { pm ->
            if (pm.isTimeShiftSupport) {
                actions.add(
                    SettingItem(
                        resources.getString(R.string.time_shift),
                        pm.isTimeShiftOn,
                        object : OnToggleChangeListener {
                            override fun onCheckedChanged(isChecked: Boolean): Boolean {
                                dlg?.dismiss()
                                val sw = pm.switchTimeShift(isChecked)
                                if (sw) {
                                    onTimeShiftChange?.invoke(pm.isTimeShiftOn)
                                }
                                return sw
                            }
                        })
                )
            }
        }
        builder.setAdapter(
            SettingAdapter(
                context,
                actions
            )
        ) { _: DialogInterface?, which: Int ->
            if (which < actionCount) {
                layoutControls.getChildAt(which).performClick()
            }
        }
        dlg = builder.create()
        dlg?.let {
            UZViewUtils.showDialog(it)
        }
    }

    fun getListTrack(
        showDialog: Boolean = false,
        title: String = "Video",
        rendererIndex: Int
    ): List<UZItem>? {
        val mappedTrackInfo = playerManager?.trackSelector?.currentMappedTrackInfo
        mappedTrackInfo?.let {
            val dialogPair: Pair<AlertDialog, UZTrackSelectionView> =
                UZTrackSelectionView.getDialog(
                    context = context,
                    title = title,
                    trackSelector = playerManager?.trackSelector,
                    rendererIndex = rendererIndex
                )
            dialogPair.second.setShowDisableOption(false)
            dialogPair.second.setAllowAdaptiveSelections(false)
            dialogPair.second.setCallback(object : com.uiza.sdk.dialog.hq.Callback {
                override fun onClick() {
                    dialogPair.first?.cancel()
                }
            })
            if (showDialog) {
                UZViewUtils.showDialog(dialogPair.first)
            }
            return dialogPair.second.uZItemList
        }

        return null
    }

    private fun showTrackSelectionDialog(view: View, showDialog: Boolean): List<UZItem>? {
        val mappedTrackInfo = playerManager?.trackSelector?.currentMappedTrackInfo
        mappedTrackInfo?.let {
            if (view is Button) {
                val title = view.text
                val rendererIndex = view.getTag() as Int
                val dialogPair: Pair<AlertDialog, UZTrackSelectionView> =
                    UZTrackSelectionView.getDialog(
                        context = context,
                        title = title,
                        trackSelector = playerManager?.trackSelector,
                        rendererIndex = rendererIndex
                    )
                dialogPair.second.setShowDisableOption(false)
                dialogPair.second.setAllowAdaptiveSelections(false)
                dialogPair.second.setCallback(object : com.uiza.sdk.dialog.hq.Callback {
                    override fun onClick() {
                        dialogPair.first?.cancel()
                    }
                })
                if (showDialog) {
                    UZViewUtils.showDialog(dialogPair.first)
                }
                return dialogPair.second.uZItemList
            }
        }

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
            initDataSource(
                linkPlay = linkPlay,
                urlIMAAd = if (isCalledFromChangeSkin) null else uzPlayback?.urlIMAAd,
                poster = uzPlayback?.poster
            )
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

    private fun initDataSource(
        linkPlay: String,
        urlIMAAd: String?,
        poster: String?
    ) {
        playerManager = UZPlayerManager.Builder(context)
            .withPlayUrl(linkPlay)
            .withIMAAdUrl(urlIMAAd)
            .build()

        setFirstStateReady(false)

        timeBarUZ?.let {
            it.setEnabledPreview(!poster.isNullOrEmpty())
            it.setPreviewLoader(object : PreviewLoader {
                override fun loadPreview(currentPosition: Long, max: Long) {
                    playerManager?.let { pm ->
                        pm.setPlayWhenReady(false)
                        ivThumbnailUZ?.let { iv ->
                            ImageUtils.loadThumbnail(
                                imageView = iv,
                                imageUrl = poster,
                                currentPosition = currentPosition,
                            )
                        }
                    }
                }
            })
        }

        playerManager?.setProgressListener(object : UZProgressListener {
            override fun onBufferProgress(
                bufferedPosition: Long,
                bufferedPercentage: Int,
                duration: Long
            ) {
                onBufferProgress?.invoke(bufferedPosition, bufferedPercentage, duration)
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}

            override fun onVideoProgress(currentMls: Long, s: Int, duration: Long, percent: Int) {
                post {
                    updateUIIbRewIconDependOnProgress(
                        currentMls = currentMls,
                        isCalledFromUZTimeBarEvent = false
                    )
                }
                onVideoProgress?.invoke(currentMls, s, duration, percent)
            }
        })
        playerManager?.setDebugCallback(object : DebugCallback {
            override fun onUpdateButtonVisibilities() {
                updateUIButtonVisibilities()
            }
        })
    }

    /**
     * When isLive = true, if not time shift then hide timebar
     */
    private fun updateTimeBarWithTimeShiftStatus() {
        playerManager?.let { pm ->
            if (pm.isTimeShiftSupport) {
                if (pm.isTimeShiftOn) {
                    UZViewUtils.visibleViews(timeBarUZ)
                } else {
                    UZViewUtils.goneViews(timeBarUZ)
                }
            }
        }
    }

    private fun handleNetworkChange(isConnected: Boolean) {
        if (isConnected) {
            if (playerManager?.exoPlaybackException == null) {
                hideController()
            } else {
                retry()
            }
        } else {
            notifyError(ErrorUtils.exceptionNoConnection())
        }
        onNetworkChange?.invoke(isConnected)
    }

    // ===== Stats For Nerds =====
    private fun initStatsForNerds() {
        player?.addAnalyticsListener(statsForNerdsView)
    }

    private fun updateLiveStreamLatency(latency: Long) {
        statsForNerdsView.showTextLiveStreamLatency()
        statsForNerdsView.setTextLiveStreamLatency(StringUtils.groupingSeparatorLong(latency))
    }

    private fun hideTextLiveStreamLatency() {
        statsForNerdsView.hideTextLiveStreamLatency()
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
        playerView?.setUseUZDragView(useUZDragView)
    }

    fun isPlayerControllerAlwayVisible(): Boolean {
        return isPlayerControllerAlwayVisible
    }

    fun retry() {
        player?.retry()
        playerManager?.setPlayWhenReady(true)
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
}
