package com.uiza.sdk.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.PictureInPictureParams
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.os.*
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.Pair
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.hls.HlsManifest
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaTrack
import com.uiza.sdk.BuildConfig
import com.uiza.sdk.R
import com.uiza.sdk.UZPlayer.Companion.elapsedTime
import com.uiza.sdk.analytics.UZAnalytic.Companion.pushEvent
import com.uiza.sdk.dialog.hq.UZItem
import com.uiza.sdk.dialog.hq.UZTrackSelectionView
import com.uiza.sdk.dialog.playlistfolder.CallbackPlaylistFolder
import com.uiza.sdk.dialog.playlistfolder.UZPlaylistFolderDialog
import com.uiza.sdk.dialog.setting.OnToggleChangeListener
import com.uiza.sdk.dialog.setting.SettingAdapter
import com.uiza.sdk.dialog.setting.SettingItem
import com.uiza.sdk.dialog.speed.Callback
import com.uiza.sdk.dialog.speed.Speed
import com.uiza.sdk.dialog.speed.UZSpeedDialog
import com.uiza.sdk.exceptions.ErrorConstant
import com.uiza.sdk.exceptions.ErrorUtils
import com.uiza.sdk.exceptions.UZException
import com.uiza.sdk.interfaces.DebugCallback
import com.uiza.sdk.interfaces.UZAdPlayerCallback
import com.uiza.sdk.interfaces.UZManagerObserver
import com.uiza.sdk.interfaces.UZPlayerCallback
import com.uiza.sdk.listerner.UZBufferListener
import com.uiza.sdk.listerner.UZChromeCastListener
import com.uiza.sdk.listerner.UZProgressListener
import com.uiza.sdk.listerner.UZTVFocusChangeListener
import com.uiza.sdk.models.UZEventType
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.models.UZTrackingData
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
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.layout_uz_ima_video_core.view.*
import okhttp3.ResponseBody
import java.util.*

class UZVideoView : RelativeLayout,
    UZManagerObserver,
    SensorOrientationChangeNotifier.Listener,
    View.OnClickListener,
    OnFocusChangeListener {

    companion object {
        private const val HYPHEN = "-"
        private const val FAST_FORWARD_REWIND_INTERVAL = 10000L // 10s
        private const val DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS = 8000 // 8s
        private const val DEFAULT_VALUE_TRACKING_LOOP = 5000L // 5s
        const val DEFAULT_TARGET_DURATION_MLS = 2000L // 2s
        private const val ARG_VIDEO_POSITION = "ARG_VIDEO_POSITION"
    }

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    private var targetDurationMls = DEFAULT_TARGET_DURATION_MLS
    private var mHandler = Handler(Looper.getMainLooper())
    private var llTop: LinearLayout? = null
    private var rlChromeCast: RelativeLayout? = null
    private var playerManager: UZPlayerManager? = null
    private var rlLiveInfo: RelativeLayout? = null
    private var previewFrameLayout: FrameLayout? = null
    private var timeBar: UZPreviewTimeBar? = null
    private var ivThumbnail: ImageView? = null
    private var tvPosition: UZTextView? = null
    private var tvDuration: UZTextView? = null
    private var tvTitle: TextView? = null
    private var tvLiveStatus: TextView? = null
    private var tvLiveView: TextView? = null
    private var tvLiveTime: TextView? = null
    private var ibFullscreen: UZImageButton? = null
    private var ibPauseIcon: UZImageButton? = null
    private var ibPlayIcon: UZImageButton? = null
    private var ibReplay: UZImageButton? = null
    private var ibRew: UZImageButton? = null
    private var ibFfwd: UZImageButton? = null
    private var ibBackScreen: UZImageButton? = null
    private var ibVolume: UZImageButton? = null
    private var ibSetting: UZImageButton? = null
    private var ibPlaylistFolder: UZImageButton? = null
    private var ibHearing: UZImageButton? = null
    private var ibPip: UZImageButton? = null
    private var ibSkipPrevious: UZImageButton? = null
    private var ibSkipNext: UZImageButton? = null
    private var ibSpeed: UZImageButton? = null
    private var ivLiveTime: UZImageButton? = null
    private var ivLiveView: UZImageButton? = null
    private var tvEndScreenMsg: TextView? = null
    override var playerView: UZPlayerView? = null
    private var defaultSeekValue = FAST_FORWARD_REWIND_INTERVAL
    private var timeBarAtBottom = false
    private var uzChromeCast: UZChromeCast? = null
    override var isCastingChromecast = false

    override var isAutoStart: Boolean = Constants.DF_PLAYER_IS_AUTO_START
        set(isAutoStart) {
            field = isAutoStart
            updateUIButtonPlayPauseDependOnIsAutoStart()
        }

    private var autoMoveToLiveEdge = false
    private var isInPipMode = false
    private var isPIPModeEnabled = true //Has the user disabled PIP mode in AppOpps?
    private var positionPIPPlayer = 0L
    var isAutoSwitchItemPlaylistFolder = true
    private var isAutoShowController = false
    private var isFreeSize = false
    private var isPlayerControllerAlwayVisible = false
    private var isSetFirstRequestFocusDone = false
    private var countTryLinkPlayError = 0
    private var activityIsPausing = false
    private var timestampOnStartPreview = 0L
    private var isOnPreview = false
    private var maxSeekLastDuration = 0L
    var isLandscape = false
    var isAlwaysPortraitScreen = false
    private var isHideOnTouch = true
    private var useController = true
    private var isOnPlayerEnded = false
    private var alwaysHideLiveViewers = false

    /*
     **Change skin via skin id resources
     * changeSkin(R.layout.uzplayer_skin_1);
     */
    //TODO improve this func
    private var isRefreshFromChangeSkin = false
    private var currentPositionBeforeChangeSkin = 0L
    private var isCalledFromChangeSkin = false
    private var firstViewHasFocus: View? = null


    private var onPreviewChangeListener: OnPreviewChangeListener? = null
    private var playerCallback: UZPlayerCallback? = null
    private var uzTVFocusChangeListener: UZTVFocusChangeListener? = null
    override var adPlayerCallback: UZAdPlayerCallback? = null
        set(callback) {
            field = callback
            if (UZAppUtils.isAdsDependencyAvailable) {
                playerManager?.setAdPlayerCallback(callback)
            } else {
                throw NoClassDefFoundError(ErrorConstant.ERR_506)
            }
        }
    var isFirstStateReady = false

    private var isCalledFromConnectionEventBus = false

    //last current position lúc từ exoplayer switch sang cast player
    private var lastCurrentPosition = 0L
    private var isCastPlayerPlayingFirst = false
    private var viewerSessionId: String? = null
    private var disposables = CompositeDisposable()
    var isViewCreated = false

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
        if (UZAppUtils.checkChromeCastAvailable()) {
            setupChromeCast()
        }
        inflate(context, R.layout.layout_uz_ima_video_core, this)

        val skinId = UZData.uzPlayerSkinLayoutId
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

        if (inflater == null) {
            throw NullPointerException("Can not inflater view")
        } else {
            playerView = inflater.inflate(skinId, null) as UZPlayerView
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT)
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            layoutParams.addRule(CENTER_IN_PARENT, TRUE)

            playerView?.let {
                it.layoutParams = layoutParams
                it.visibility = GONE
                layoutRootView.addView(it)
            }

            controllerAutoShow = isAutoShowController
            findViews()
            resizeContainerView()
        }
        updateUIEachSkin()
        setMarginPreviewTimeBar()
        setMarginRlLiveInfo()
        updateUISizeThumbnail()
        isViewCreated = true

        playerView?.let {
            playerCallback?.playerViewCreated(it)
        }
    }

    private fun findViews() {
        rlMsg.setOnClickListener(this)
        UZViewUtils.setTextShadow(textView = tvMsg, color = Color.BLACK)
        UZViewUtils.setColorProgressBar(progressBar = pb, color = Color.WHITE)
        updateUIPositionOfProgressBar()

        playerView?.let { pv ->
            pv.setOnDoubleTap(object : OnDoubleTap {
                override fun onDoubleTapFinished() {}
                override fun onDoubleTapProgressDown(posX: Float, posY: Float) {}
                override fun onDoubleTapStarted(posX: Float, posY: Float) {}
                override fun onDoubleTapProgressUp(posX: Float, posY: Float) {
                    val halfScreen = UZViewUtils.screenWidth / 2.0f
                    if (posX - 60.0f > halfScreen) {
                        seekToForward()
                    } else if (posX + 60.0f < halfScreen) {
                        seekToBackward()
                    }
                }
            })
            timeBar = pv.findViewById(R.id.exo_progress)
            previewFrameLayout = pv.findViewById(R.id.previewFrameLayout)

            if (timeBar == null) {
                pv.visibility = VISIBLE
            } else {
                timeBar?.let { tb ->
                    if (tb.tag == null) {
                        timeBarAtBottom = false
                        pv.visibility = VISIBLE
                    } else {
                        if (tb.tag.toString() == resources.getString(R.string.use_bottom_uz_timebar)) {
                            timeBarAtBottom = true
                            setMarginDependOnUZTimeBar(pv.videoSurfaceView)
                        } else {
                            timeBarAtBottom = false
                            pv.visibility = VISIBLE
                        }
                    }
                    tb.addOnPreviewChangeListener(object : OnPreviewChangeListener {
                        override fun onStartPreview(previewView: PreviewView?, progress: Int) {
                            timestampOnStartPreview = System.currentTimeMillis()
                            onPreviewChangeListener?.onStartPreview(previewView, progress)
                        }

                        override fun onStopPreview(previewView: PreviewView?, progress: Int) {
                            if (isCastingChromecast) {
                                val casty = UZData.casty
                                casty?.player?.seek(progress.toLong())
                            }
                            val seekLastDuration =
                                System.currentTimeMillis() - timestampOnStartPreview
                            if (maxSeekLastDuration < seekLastDuration) {
                                maxSeekLastDuration = seekLastDuration
                            }
                            isOnPreview = false
                            onStopPreview(progress)
                            onPreviewChangeListener?.onStopPreview(previewView, progress)
                        }

                        override fun onPreview(
                            previewView: PreviewView?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            isOnPreview = true
                            updateUIIbRewIconDependOnProgress(
                                currentMls = progress.toLong(),
                                isCalledFromUZTimeBarEvent = true
                            )
                            onPreviewChangeListener?.onPreview(previewView, progress, fromUser)
                        }

                    })
                    tb.onFocusChangeListener = this
                }
            }

            llTop = pv.findViewById(R.id.llTop)
            ivThumbnail = pv.findViewById(R.id.ivThumbnail)
            tvPosition = pv.findViewById(R.id.tvPosition)
            tvDuration = pv.findViewById(R.id.tvDuration)
            ibFullscreen = pv.findViewById(R.id.ibFullscreen)
            tvTitle = pv.findViewById(R.id.tvTitle)
            ibPauseIcon = pv.findViewById(R.id.exo_pause)
            ibPlayIcon = pv.findViewById(R.id.exo_play)
            ibReplay = pv.findViewById(R.id.exo_replay)
            ibRew = pv.findViewById(R.id.exo_rew)
            ibFfwd = pv.findViewById(R.id.exo_ffwd)
            ibBackScreen = pv.findViewById(R.id.btBackScreen)
            ibVolume = pv.findViewById(R.id.exo_volume)
            ibSetting = pv.findViewById(R.id.exo_setting)
            ibPlaylistFolder = pv.findViewById(R.id.ibPlaylistFolder)
            ibHearing = pv.findViewById(R.id.ibHearing)
            ibPip = pv.findViewById(R.id.ibPip)
            ibSkipNext = pv.findViewById(R.id.ibSkipNext)
            ibSkipPrevious = pv.findViewById(R.id.ibSkipPrevious)
            ibSpeed = pv.findViewById(R.id.ibSpeed)
            rlLiveInfo = pv.findViewById(R.id.rlLiveInfo)
            tvLiveStatus = pv.findViewById(R.id.tvLiveStatus)
            tvLiveView = pv.findViewById(R.id.tvLiveView)
            tvLiveTime = pv.findViewById(R.id.tvLiveTime)
            ivLiveView = pv.findViewById(R.id.ivLiveView)
            ivLiveTime = pv.findViewById(R.id.ivLiveTime)

            tvPosition?.text = StringUtils.convertMlsecondsToHMmSs(0)
            tvDuration?.text = "-:-"

            //If auto start true, show button play and gone button pause
            UZViewUtils.goneViews(ibPlayIcon)

            ibRew?.setSrcDrawableDisabled()

            if (!UZAppUtils.hasSupportPIP(context) || UZData.useUZDragView) {
                UZViewUtils.goneViews(ibPip)
            }

            if (BuildConfig.DEBUG) {
                layoutDebug.visibility = VISIBLE
            } else {
                layoutDebug.visibility = GONE
            }

            UZViewUtils.setFocusableViews(focusable = false, ivLiveView, ivLiveTime)

            val rlEndScreen = pv.findViewById<RelativeLayout>(R.id.rlEndScreen)
            UZViewUtils.goneViews(rlEndScreen)

            tvEndScreenMsg = pv.findViewById(R.id.tvEndScreenMsg)
            tvEndScreenMsg?.let {
                UZViewUtils.setTextShadow(textView = it, color = Color.WHITE)
                it.setOnClickListener(this)
            }
            setEventForViews()
            setVisibilityOfPlaylistFolderController(GONE)
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
            this.isAutoShowController = isAutoShowController
            playerView?.controllerAutoShow = isAutoShowController
        }

    // Lay pixel dung cho custom UI like youtube, timeBar bottom of player controller
    private val pixelAdded: Int
        get() = if (timeBarAtBottom) {
            heightTimeBar / 2
        } else {
            0
        }

    //return pixel
    private val heightTimeBar: Int
        get() {
            timeBar?.let {
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
            ivVideoCover = ivCover,
            pixelAdded = pixelAdded,
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
        setHideControllerOnTouch(false)
        controllerShowTimeoutMs = 0
        isPlayerControllerAlwayVisible = true
    }

    private fun handleError(uzException: UZException?) {
        if (uzException == null) {
            return
        }
        uzException.printStackTrace()
        notifyError(uzException)
        UZData.isSettingPlayer = false
    }

    private fun notifyError(exception: UZException) {
        playerCallback?.onError(exception)
    }

    private fun handlePlayPlayListFolderUI() {
        setVisibilityOfPlaylistFolderController(
            if (isPlayPlaylistFolder) {
                View.VISIBLE
            } else {
                View.GONE
            }
        )
    }

    val player: SimpleExoPlayer?
        get() = playerManager?.getPlayer()

    fun seekTo(positionMs: Long) {
        playerManager?.seekTo(positionMs)
    }


    fun play(): Boolean {
        val playback = UZData.getPlayback()
        if (playback == null) {
            log("ErrorConstant.ERR_14")
            return false
        }
        if (!ConnectivityUtils.isConnected(context)) {
            log("ErrorConstant.ERR_0")
            return false
        }
        initPlayback(playback = playback, isClearDataPlaylistFolder = true)
        return true
    }

    fun play(playback: UZPlayback): Boolean {
        if (!ConnectivityUtils.isConnected(context)) {
            notifyError(ErrorUtils.exceptionNoConnection())
            return false
        }
        UZData.setPlayback(playback = playback)
        initPlayback(playback = playback, isClearDataPlaylistFolder = true)
        return true
    }

    fun play(playlist: ArrayList<UZPlayback>): Boolean {
        // TODO: Check how to get subtitle of a custom link play, because we have no idea about entityId or appId
        if (!ConnectivityUtils.isConnected(context)) {
            handleError(uzException = ErrorUtils.exceptionNoConnection())
            return false
        }
        if (playlist.isEmpty()) {
            handleError(uzException = ErrorUtils.exceptionPlaylistFolderItemFirst())
            return false
        } else {
            UZData.clearDataForPlaylistFolder()
            UZData.setPlayList(playlist = playlist)
            playPlaylistPosition(position = UZData.getCurrentPositionOfPlayList())
        }
        return true
    }

    fun resume() {
        if (isCastingChromecast) {
            val casty = UZData.casty
            casty?.player?.play()
        } else {
            playerManager?.resume()
        }

        UZViewUtils.goneViews(ibPlayIcon)
        ibPauseIcon?.let {
            UZViewUtils.visibleViews(it)
            it.requestFocus()
        }
        keepScreenOn = true
    }

    fun pause() {
        if (isCastingChromecast) {
            val casty = UZData.casty
            casty?.player?.pause()
        } else {
            playerManager?.pause()
        }
        UZViewUtils.goneViews(ibPauseIcon)
        keepScreenOn = false
        ibPlayIcon?.let {
            UZViewUtils.visibleViews(it)
            it.requestFocus()
        }
    }

    val videoWidth: Int
        get() = playerManager?.videoWidth ?: 0

    val videoHeight: Int
        get() = playerManager?.videoHeight ?: 0

    private fun initPlayback(playback: UZPlayback, isClearDataPlaylistFolder: Boolean) {
        if (isClearDataPlaylistFolder) {
            UZData.clearDataForPlaylistFolder()
        }
        isCalledFromChangeSkin = false
        handlePlayPlayListFolderUI()
        hideLayoutMsg()
        controllerShowTimeoutMs = DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS
        isOnPlayerEnded = false
        updateUIEndScreen()
        viewerSessionId = UUID.randomUUID().toString()
        releasePlayerManager()
        resetCountTryLinkPlayError()
        showProgress()
        updateUIDependOnLiveStream()
        val linkPlay = playback.firstLinkPlay
        if (linkPlay.isNullOrEmpty()) {
            handleError(ErrorUtils.exceptionNoLinkPlay())
            return
        }
        initDataSource(
            linkPlay = linkPlay,
            urlIMAAd = UZData.urlIMAAd,
            urlThumbnailsPreviewSeekBar = playback.poster
        )
        playerCallback?.isInitResult(linkPlay)
        trackWatchingTimer(firstRun = true)
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
            if (isCalledFromConnectionEventBus) {
                pm.setRunnable()
                isCalledFromConnectionEventBus = false
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

    protected fun tryNextLinkPlay() {
        if (isLIVE) {
            // try to play 5 times
            UZData.getPlayback()?.let { playBack ->
                if (countTryLinkPlayError >= playBack.size) {
                    return
                }
            }
            // if entity is livestreaming, dont try to next link play
            playerManager?.let {
                it.initWithoutReset()
                it.setRunnable()
            }
            countTryLinkPlayError++
            isFirstStateReady = false
            return
        }
        countTryLinkPlayError++
        isFirstStateReady = false
        releasePlayerManager()
        checkToSetUpResource()
    }

    //khi call api callAPIGetLinkPlay nhung json tra ve ko co data
    //se co gang choi video da play gan nhat
    //neu co thi se play
    //khong co thi bao loi
    private fun handleErrorNoData() {
        removeVideoCover(true)
        if (playerCallback != null) {
            UZData.isSettingPlayer = false
            handleError(uzException = ErrorUtils.exceptionNoLinkPlay())
        }
    }

    protected fun resetCountTryLinkPlayError() {
        countTryLinkPlayError = 0
    }

    fun onBackPressed(): Boolean {
        if (isLandscape) {
            toggleFullscreen()
            return true
        }
        return false
    }

    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        positionPIPPlayer = currentPosition
        isInPipMode = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.
            setUseController(useController = false)
        } else {
            // Restore the full-screen UI.
            setUseController(useController = true)
        }
    }

    fun onDestroyView() {
        releasePlayerStats()
        releasePlayerManager()
        UZData.isSettingPlayer = false
        isCastingChromecast = false
        isCastPlayerPlayingFirst = false
        if (UZAppUtils.hasSupportPIP(context)) {
            if (context is Activity) {
                (context as Activity).finishAndRemoveTask()
            }
        }
        playerManager?.unregister()
        disposables.dispose()
    }

    private fun releasePlayerStats() {
        player?.removeAnalyticsListener(statsForNerdsView)
    }

    private fun releasePlayerManager() {
        playerManager?.release()
    }

    fun onResumeView() {
        SensorOrientationChangeNotifier.getInstance(context)?.addListener(this)
        if (isCastingChromecast) {
            return
        }
        activityIsPausing = false
//        if (ibPlayIcon == null || ibPlayIcon?.visibility != VISIBLE) {
//            playerManager?.resume()
//        }
        playerManager?.resume()
        if (positionPIPPlayer > 0L && isInPipMode) {
            seekTo(positionPIPPlayer)
        } else if (autoMoveToLiveEdge && isLIVE) {
            // try to move to the edge of livestream video
            seekToLiveEdge()
        }
        //Makes sure that the media controls pop up on resuming and when going between PIP and non-PIP states.
        setUseController(true)
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
        activityIsPausing = true
        positionPIPPlayer = currentPosition
        SensorOrientationChangeNotifier.getInstance(context)?.remove(this)

        // in PIP to continue
        if (!isInPipMode) {
            playerManager?.pause()
        }
    }

    override val isPIPEnable: Boolean
        get() = (ibPip != null && !isCastingChromecast && UZAppUtils.hasSupportPIP(context = context) && !UZData.useUZDragView)

    fun onStopPreview(progress: Int) {
        if (!isCastingChromecast) {
            playerManager?.seekTo(progress.toLong())
            playerManager?.resume()
            isOnPlayerEnded = false
            updateUIEndScreen()
        }
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
                ibFullscreen?.let {
                    UZViewUtils.setUIFullScreenIcon(imageButton = it, isFullScreen = true)
                }
                UZViewUtils.goneViews(ibPip)
            } else {
                if (!isInPipMode) {
                    UZViewUtils.hideSystemUi(pv)
                }
                isLandscape = false
                ibFullscreen?.let {
                    UZViewUtils.setUIFullScreenIcon(imageButton = it, isFullScreen = false)
                }
                if (isPIPEnable) {
                    UZViewUtils.visibleViews(ibPip)
                }
            }
            setMarginPreviewTimeBar()
            setMarginRlLiveInfo()
            updateUISizeThumbnail()
            updateUIPositionOfProgressBar()
            if (timeBarAtBottom) {
                setMarginDependOnUZTimeBar(pv.videoSurfaceView)
            }
            playerCallback?.onScreenRotate(isLandscape)
        }
    }

    override fun onClick(v: View) {
        if (v === ibFullscreen) {
            toggleFullscreen()
        } else if (v === ibBackScreen) {
            handleClickBackScreen()
        } else if (v === ibVolume) {
            handleClickBtVolume()
        } else if (v === ibSetting) {
            showSettingsDialog()
        } else if (v === ibPlaylistFolder) {
            handleClickPlaylistFolder()
        } else if (v === ibHearing) {
            handleClickHearing()
        } else if (v === ibPip) {
            enterPIPMode()
        } else if (v.parent === layoutControls) {
            showTrackSelectionDialog(v, true)
        } else if (v === tvLiveStatus) {
            seekToEndLive()
        } else if (v === ibFfwd) {
            if (isCastingChromecast) {
                val casty = UZData.casty
                casty?.player?.seekToForward(defaultSeekValue)
            }
            playerManager?.seekToForward(defaultSeekValue)
        } else if (v === ibRew) {
            if (isCastingChromecast) {
                val casty = UZData.casty
                casty?.player?.seekToRewind(defaultSeekValue)
            } else if (playerManager != null) {
                playerManager?.seekToBackward(defaultSeekValue)
                if (isPlaying) {
                    isOnPlayerEnded = false
                    updateUIEndScreen()
                }
            }
        } else if (v === ibPauseIcon) {
            pause()
        } else if (v === ibPlayIcon) {
            resume()
        } else if (v === ibReplay) {
            replay()
        } else if (v === ibSkipNext) {
            handleClickSkipNext()
        } else if (v === ibSkipPrevious) {
            handleClickSkipPrevious()
        } else if (v === ibSpeed) {
            showSpeed()
        }
        /*có trường hợp đang click vào các control thì bị ẩn control ngay lập tức,
        trường hợp này ta có thể xử lý khi click vào control thì reset count down để ẩn control ko
        default controller timeout là 8s, vd tới s thứ 7 bạn tương tác thì tới s thứ 8 controller sẽ bị ẩn*/
        if (useController && (rlMsg == null || rlMsg?.visibility != VISIBLE) && isPlayerControllerShowing
        ) {
            showController()
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun enterPIPMode() {
        if (isPIPEnable) {
            if (isLandscape) {
                throw  IllegalArgumentException("Cannot enter PIP Mode if screen is landscape")
            }
            isInPipMode = true
            positionPIPPlayer = currentPosition
            setUseController(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                val aspectRatio = Rational(videoWidth, videoHeight)
                params.setAspectRatio(aspectRatio)
                if (context is Activity) {
                    (context as Activity).enterPictureInPictureMode(params.build())
                }
            } else {
                if (context is Activity) {
                    (context as Activity).enterPictureInPictureMode()
                }
            }
        }
//        postDelayed({
//            if (context is Activity) {
//                isPIPModeEnabled = (context as Activity).isInPictureInPictureMode
//            }
//            if (!isPIPModeEnabled) {
//                enterPIPMode()
//            }
//        }, 50)
    }

    var controllerShowTimeoutMs: Int
        get() = playerView?.controllerShowTimeoutMs ?: -1
        set(controllerShowTimeoutMs) {
            post {
                playerView?.controllerShowTimeoutMs = controllerShowTimeoutMs
            }
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
        //do not hide if is casting chromecast
        if (!isCastingChromecast) {
            playerView?.hideController()
        }
    }

    fun setHideControllerOnTouch(isHide: Boolean) {
        isHideOnTouch = isHide
        playerView?.controllerHideOnTouch = isHide
    }

    val controllerHideOnTouch: Boolean
        get() = playerView?.controllerHideOnTouch ?: false

    fun isUseController(): Boolean {
        return useController
    }

    fun setUseController(useController: Boolean) {
        this.useController = useController
        playerView?.useController = useController
    }

    protected val isPlayPlaylistFolder: Boolean
        get() = !(UZData.getPlayList().isNullOrEmpty())

    private fun playPlaylistPosition(position: Int) {
        if (!isPlayPlaylistFolder) {
            log("playPlaylistPosition error: incorrect position")
            return
        }
        log("playPlaylistPosition position: $position")
        if (position < 0) {
            log("This is the first item")
            notifyError(ErrorUtils.exceptionPlaylistFolderItemFirst())
            return
        }
        UZData.getPlayList()?.let {
            if (position > it.size - 1) {
                log("This is the last item")
                notifyError(ErrorUtils.exceptionPlaylistFolderItemLast())
                return
            }
        }
        pause()
        hideController()
        UZViewUtils.setSrcDrawableEnabledForViews(ibSkipPrevious, ibSkipNext)
        //set disabled prevent double click, will enable onStateReadyFirst()
        UZViewUtils.setClickableForViews(able = false, ibSkipPrevious, ibSkipNext)
        //end update UI for skip next and skip previous button
        UZData.setCurrentPositionOfPlayList(position)
        val playback = UZData.getPlayback()
        if (playback == null || !playback.canPlay()) {
            notifyError(ErrorUtils.exceptionNoLinkPlay())
            return
        }
        initPlayback(playback = playback, isClearDataPlaylistFolder = false)
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
            if (isPlayPlaylistFolder && isAutoSwitchItemPlaylistFolder) {
                hideController()
                autoSwitchNextVideo()
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
                    hideLayoutMsg()
                    resetCountTryLinkPlayError()
                    timeBar?.hidePreview()
                }
                if (context is Activity) {
                    (context as Activity).setResult(Activity.RESULT_OK)
                }

                if (!isFirstStateReady) {
                    removeVideoCover(isFromHandleError = false)
                    isFirstStateReady = true
                }
            }
        }
    }

    private fun autoSwitchNextVideo() {
        playPlaylistPosition(position = UZData.getCurrentPositionOfPlayList() + 1)
    }

    private fun autoSwitchPreviousLinkVideo() {
        playPlaylistPosition(position = UZData.getCurrentPositionOfPlayList() - 1)
    }

    private fun handleClickPlaylistFolder() {
        UZData.getPlayList()?.let { playList ->
            val uzPlaylistFolderDlg = UZPlaylistFolderDialog(
                mContext = context,
                playList = playList,
                currentPositionOfDataList = UZData.getCurrentPositionOfPlayList(),
                callbackPlaylistFolder = object : CallbackPlaylistFolder {
                    override fun onDismiss() {}
                    override fun onFocusChange(playback: UZPlayback, position: Int) {}
                    override fun onClickItem(playback: UZPlayback, position: Int) {
                        playPlaylistPosition(position)
                    }
                })
            UZViewUtils.showDialog(uzPlaylistFolderDlg)
        }
    }

    private fun handleClickSkipNext() {
        isOnPlayerEnded = false
        updateUIEndScreen()
        autoSwitchNextVideo()
    }

    private fun handleClickSkipPrevious() {
        isOnPlayerEnded = false
        updateUIEndScreen()
        autoSwitchPreviousLinkVideo()
    }

    fun replay() {
        if (playerManager == null) {
            return
        }
        //TODO Chỗ này đáng lẽ chỉ clear value của tracking khi đảm bảo rằng seekTo(0) true
        val result = playerManager?.seekTo(0)
        if (result == true) {
            isSetFirstRequestFocusDone = false
            isOnPlayerEnded = false
            updateUIEndScreen()
            handlePlayPlayListFolderUI()
        }
        if (isCastingChromecast) {
            replayChromeCast()
        }
    }

    private fun replayChromeCast() {
        lastCurrentPosition = 0
        handleConnectedChromecast()
        showController()
    }

    /*Nếu đang casting thì button này sẽ handle volume on/off ở cast player
     * Ngược lại, sẽ handle volume on/off ở exo player*/
    private fun handleClickBtVolume() {
        if (isCastingChromecast) {
            val casty = UZData.casty
            if (casty != null) {
                val isMute = casty.toggleMuteVolume()
                ibVolume?.setImageResource(if (isMute) R.drawable.ic_volume_off_white_24 else R.drawable.ic_volume_up_white_24)
            }
        }
        toggleVolumeMute()
    }

    private fun handleClickBackScreen() {
        if (isLandscape) {
            toggleFullscreen()
        } else {
            if (context is Activity) {
                (context as Activity).onBackPressed()
            }
        }
    }

    private fun handleClickHearing() {
        val view = DebugUtils.getAudioButton(layoutControls)
        view?.performClick()
    }

    fun setDefaultSeekValue(mls: Int) {
        defaultSeekValue = mls.toLong()
    }

    fun seekToForward(mls: Int) {
        setDefaultSeekValue(mls)
        ibFfwd?.performClick()
    }

    fun seekToForward() {
        if (!isLIVE) {
            ibFfwd?.performClick()
        }
    }

    fun seekToBackward(mls: Int) {
        setDefaultSeekValue(mls)
        ibRew?.performClick()
    }

    fun seekToBackward() {
        ibRew?.performClick()
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

    fun toggleVolume() {
        ibVolume?.performClick()
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

    /**
     * Bo video hien tai va choi tiep theo mot video trong playlist/folder
     */
    fun skipNextVideo() {
        handleClickSkipNext()
    }

    /**
     * Bo video hien tai va choi lui lai mot video trong playlist/folder
     */
    fun skipPreviousVideo() {
        handleClickSkipPrevious()
    }

    val isLIVE: Boolean
        get() = playerManager != null && playerManager?.isLIVE == true

    fun setLiveViewers(viewers: Int) {
        if (!alwaysHideLiveViewers) {
            if (viewers == 1) {
                tvLiveView?.text = resources.getString(R.string.oneViewer)
            } else {
                tvLiveView?.text = resources.getString(R.string.numberOfViewers, viewers)
            }
        }
    }

    var volume: Float
        get() = playerManager?.volume ?: -1F
        set(volume) {
            playerManager?.let { pm ->
                pm.volume = volume
                if (pm.volume != 0f) {
                    ibVolume?.setSrcDrawableEnabled()
                } else {
                    ibVolume?.setSrcDrawableDisabledCanTouch()
                }
            }
        }
    private var volumeToggle = 0f

    fun toggleVolumeMute() {
        playerManager?.let { pm ->
            if (pm.volume == 0f) {
                volume = volumeToggle
                ibVolume?.setSrcDrawableEnabled()
            } else {
                volumeToggle = volume
                volume = 0f
                ibVolume?.setSrcDrawableDisabledCanTouch()
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
        player?.playbackParameters = playbackParameters
    }

    private fun setEventForViews() {
        setClickAndFocusEventForViews(
            ibFullscreen,
            ibBackScreen,
            ibVolume,
            ibSetting,
            ibPlaylistFolder,
            ibHearing,
            ibPip,
            ibFfwd,
            ibRew,
            ibPlayIcon,
            ibPauseIcon,
            ibReplay,
            ibSkipNext,
            ibSkipPrevious,
            ibSpeed,
            tvLiveStatus
        )
    }

    private fun setClickAndFocusEventForViews(vararg views: View?) {
        for (v in views) {
            v?.let {
                it.setOnClickListener(this)
                it.onFocusChangeListener = this
            }
        }
    }

    //If auto start true, show button play and gone button pause
    //if not, gone button play and show button pause
    private fun updateUIButtonPlayPauseDependOnIsAutoStart() {
        if (isAutoStart) {
            UZViewUtils.goneViews(ibPlayIcon)
            ibPauseIcon?.let { ib ->
                UZViewUtils.visibleViews(ib)
                if (!isSetFirstRequestFocusDone) {
                    ib.requestFocus() //set first request focus if using player for TV
                    isSetFirstRequestFocusDone = true
                }
            }
        } else {
            if (isPlaying) {
                UZViewUtils.goneViews(ibPlayIcon)
                ibPauseIcon?.let { ib ->
                    UZViewUtils.visibleViews(ib)
                    if (!isSetFirstRequestFocusDone) {
                        ib.requestFocus() //set first request focus if using player for TV
                        isSetFirstRequestFocusDone = true
                    }
                }
            } else {
                ibPlayIcon?.let { ib ->
                    UZViewUtils.visibleViews(ib)
                    if (!isSetFirstRequestFocusDone) {
                        ib.requestFocus() //set first request focus if using player for TV
                        isSetFirstRequestFocusDone = true
                    }
                }
                UZViewUtils.goneViews(ibPauseIcon)
            }
        }
    }

    private fun removeVideoCover(isFromHandleError: Boolean) {
        if (!isFromHandleError) {
            onStateReadyFirst()
        }
        if (ivCover.visibility != View.GONE) {
            ivCover.visibility = View.GONE
            ivCover.invalidate()
            if (isLIVE) {
                tvLiveTime?.text = HYPHEN
            }
        } else {
            //goi change skin realtime thi no ko vao if nen ko update tvDuration dc
            updateTvDuration()
        }
    }

    private fun updateUIEachSkin() {
        val curSkinLayoutId = UZData.uzPlayerSkinLayoutId
        if (curSkinLayoutId == R.layout.uzplayer_skin_2 || curSkinLayoutId == R.layout.uzplayer_skin_3) {

            ibPlayIcon?.setRatioLand(7)
            ibPlayIcon?.setRatioPort(5)

            ibPauseIcon?.setRatioLand(7)
            ibPauseIcon?.setRatioPort(5)

            ibReplay?.setRatioLand(7)
            ibReplay?.setRatioPort(5)
        }
    }

    private fun updateUIPositionOfProgressBar() {
        playerView?.let { pv ->
            postDelayed({
                val marginL = pv.measuredWidth / 2 - pb.measuredWidth / 2
                val marginT = pv.measuredHeight / 2 - pb.measuredHeight / 2
                UZViewUtils.setMarginPx(view = pb, l = marginL, t = marginT, r = 0, b = 0)
            }, 10)
        }
    }

    /*
     ** change skin of player (realtime)
     * return true if success
     */
    fun changeSkin(@LayoutRes skinId: Int): Boolean {
        if (playerManager == null) {
            return false
        }
        require(!UZData.useUZDragView) {
            { resources.getString(R.string.error_change_skin_with_uzdragview) }
        }
        if (playerManager?.isPlayingAd == true) {
            notifyError(ErrorUtils.exceptionChangeSkin())
            return false
        }
        UZData.uzPlayerSkinLayoutId = skinId
        isRefreshFromChangeSkin = true
        isCalledFromChangeSkin = true

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        if (inflater != null) {
            layoutRootView.removeView(playerView)
            layoutRootView.requestLayout()
            playerView = inflater.inflate(skinId, null) as UZPlayerView

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
            updateUIDependOnLiveStream()
            setMarginPreviewTimeBar()
            setMarginRlLiveInfo()

            //setup chromecast
            if (UZAppUtils.checkChromeCastAvailable()) {
                setupChromeCast()
            }
            currentPositionBeforeChangeSkin = currentPosition
            releasePlayerManager()
            setTitle()
            checkToSetUpResource()
            updateUISizeThumbnail()
            playerCallback?.onSkinChange()

            return true
        }
        return false
    }

    private fun setupChromeCast() {
        uzChromeCast = UZChromeCast()
        uzChromeCast?.setUZChromeCastListener(object : UZChromeCastListener {
            override fun onConnected() {
                lastCurrentPosition = currentPosition
                handleConnectedChromecast()
            }

            override fun onDisconnected() {
                handleDisconnectedChromecast()
            }

            override fun addUIChromeCast() {
                uzChromeCast?.let {
                    llTop?.addView(it.mediaRouteButton)
                }
                addUIChromecastLayer()
            }
        })
        uzChromeCast?.setupChromeCast(context)
    }

    private fun updateTvDuration() {
        if (isLIVE) {
            tvDuration?.text = StringUtils.convertMlsecondsToHMmSs(mls = 0)
        } else {
            tvDuration?.text = StringUtils.convertMlsecondsToHMmSs(mls = duration)
        }
    }

    private fun setTextPosition(currentMls: Long) {
        if (isLIVE) {
            val duration = duration
            val past = duration - currentMls
            tvPosition?.text = String.format(
                "%s%s",
                HYPHEN,
                StringUtils.convertMlsecondsToHMmSs(past)
            )
        } else {
            tvPosition?.text = StringUtils.convertMlsecondsToHMmSs(currentMls)
        }
    }

    private fun updateUIIbRewIconDependOnProgress(
        currentMls: Long,
        isCalledFromUZTimeBarEvent: Boolean
    ) {
        if (isCalledFromUZTimeBarEvent) {
            setTextPosition(currentMls)
        } else {
            if (!isOnPreview) {
                //uzTimeBar is displaying
                setTextPosition(currentMls)
            }
            return
        }
        if (isLIVE) {
            return
        }
        ibRew?.let { r ->
            ibFfwd?.let { f ->
                if (currentMls == 0L) {
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
                    resHasFocus = R.drawable.bkg_tv_has_focus,
                    resNoFocus = R.drawable.bkg_tv_no_focus
                )
                view.clearColorFilter()
            }
            is Button -> {
                UZViewUtils.updateUIFocusChange(
                    view = view,
                    isFocus = isFocus,
                    resHasFocus = R.drawable.bkg_tv_has_focus,
                    resNoFocus = R.drawable.bkg_tv_no_focus
                )
            }
            is UZPreviewTimeBar -> {
                UZViewUtils.updateUIFocusChange(
                    view = view,
                    isFocus = isFocus,
                    resHasFocus = R.drawable.bkg_tv_has_focus_uz_timebar,
                    resNoFocus = R.drawable.bkg_tv_no_focus_uz_timebar
                )
            }
        }
    }

    private fun handleFirstViewHasFocus() {
        if (firstViewHasFocus != null) {
            uzTVFocusChangeListener?.onFocusChange(view = firstViewHasFocus, isFocus = true)
            firstViewHasFocus = null
        }
    }

    private fun updateUISizeThumbnail() {
        val screenWidth = UZViewUtils.screenWidth
        val widthIv = if (isLandscape) {
            screenWidth / 4
        } else {
            screenWidth / 5
        }
        previewFrameLayout?.let { fl ->
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
        timeBar?.let { tb ->
            if (isLandscape) {
                UZViewUtils.setMarginDimen(view = tb, dpL = 5, dpT = 0, dpR = 5, dpB = 0)
            } else {
                UZViewUtils.setMarginDimen(view = tb, dpL = 0, dpT = 0, dpR = 0, dpB = 0)
            }
        }
    }

    private fun setMarginRlLiveInfo() {
        rlLiveInfo?.let { rl ->
            if (isLandscape) {
                UZViewUtils.setMarginDimen(view = rl, dpL = 50, dpT = 0, dpR = 50, dpB = 0)
            } else {
                UZViewUtils.setMarginDimen(view = rl, dpL = 5, dpT = 0, dpR = 5, dpB = 0)
            }
        }
    }

    private fun setTitle() {
        tvTitle?.text = UZData.getEntityName()
    }

    fun setAlwaysHideLiveViewers(hide: Boolean) {
        alwaysHideLiveViewers = hide
    }

    private fun updateUIDependOnLiveStream() {
        if (isCastingChromecast) {
            UZViewUtils.goneViews(ibPip)
        } else if (UZAppUtils.isTablet(context) && UZAppUtils.isTV(context)) {
            //only hide ibPictureInPictureIcon if device is TV
            UZViewUtils.goneViews(ibPip)
        }
        if (isLIVE) {
            if (alwaysHideLiveViewers) {
                UZViewUtils.visibleViews(rlLiveInfo, tvLiveStatus, tvLiveTime, ivLiveTime)
                UZViewUtils.goneViews(ivLiveTime, ivLiveView)
            } else {
                UZViewUtils.visibleViews(
                    rlLiveInfo,
                    tvLiveStatus,
                    tvLiveTime,
                    ivLiveTime,
                    tvLiveView,
                    ivLiveView
                )
            }
            UZViewUtils.goneViews(ibSpeed, tvDuration, ibRew, ibFfwd)
            setUIVisible(visible = false, ibRew, ibFfwd)
        } else {
            UZViewUtils.goneViews(
                rlLiveInfo,
                tvLiveStatus,
                tvLiveTime,
                tvLiveView,
                ivLiveTime,
                ivLiveView
            )
            UZViewUtils.visibleViews(ibSpeed, tvDuration, ibFfwd, ibRew)
            setUIVisible(visible = true, ibRew, ibFfwd)
            //TODO why set visible not work?
        }
        if (UZAppUtils.isTV(context)) {
            UZViewUtils.goneViews(ibFullscreen)
        }
    }

    private fun setUIVisible(visible: Boolean, vararg views: UZImageButton?) {
        for (v in views) {
            v?.setUIVisible(visible)
        }
    }

    protected fun updateUIButtonVisibilities() {
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

    fun showLayoutMsg() {
        hideController()
        UZViewUtils.visibleViews(rlMsg)
    }

    fun hideLayoutMsg() {
        UZViewUtils.goneViews(rlMsg)
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
                setHideControllerOnTouch(isHideOnTouch)
            }
        }
    }

    private fun setVisibilityOfPlayPauseReplay(isShowReplay: Boolean) {
        if (isShowReplay) {
            UZViewUtils.goneViews(ibPlayIcon, ibPauseIcon)
            UZViewUtils.visibleViews(ibReplay)
            ibReplay?.requestFocus()
        } else {
            updateUIButtonPlayPauseDependOnIsAutoStart()
            UZViewUtils.goneViews(ibReplay)
        }
    }

    private fun setVisibilityOfPlaylistFolderController(visibilityOfPlaylistFolderController: Int) {
        UZViewUtils.setVisibilityViews(
            visibility = visibilityOfPlaylistFolderController,
            ibPlaylistFolder,
            ibSkipNext,
            ibSkipPrevious
        )
        setVisibilityOfPlayPauseReplay(false)
    }

    var dlg: Dialog? = null


    @SuppressLint("InflateParams")
    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        if (inflater != null) {
            builder.setCustomTitle(inflater.inflate(R.layout.custom_header_dragview, null))
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
                                    playerCallback?.onTimeShiftChange(pm.isTimeShiftOn)
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
                        mHandler.postDelayed(
                            {
                                if (dialogPair.first == null) {
                                    return@postDelayed
                                }
                                dialogPair.first?.cancel()
                            }, 300
                        )
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

    fun setMarginDependOnUZTimeBar(view: View?) {
        if (view == null || timeBar == null) {
            return
        }
        val tmpHeightTimeBar: Int
        if (isLandscape) {
            UZViewUtils.setMarginPx(view = view, l = 0, t = 0, r = 0, b = 0)
        } else {
            tmpHeightTimeBar = heightTimeBar
            UZViewUtils.setMarginPx(view = view, l = 0, t = 0, r = 0, b = tmpHeightTimeBar / 2)
        }
    }

    fun hideProgress() {
        pb.visibility = View.GONE
    }

    fun showProgress() {
        pb.visibility = View.VISIBLE
    }

    fun setPlayerCallback(callback: UZPlayerCallback?) {
        playerCallback = callback
    }

    fun setTVFocusChangeListener(uzTVFocusChangeListener: UZTVFocusChangeListener?) {
        this.uzTVFocusChangeListener = uzTVFocusChangeListener
        handleFirstViewHasFocus()
    }

    fun setOnPreviewChangeListener(onPreviewChangeListener: OnPreviewChangeListener?) {
        this.onPreviewChangeListener = onPreviewChangeListener
    }

    private fun checkToSetUpResource() {
        val playback = UZData.getPlayback()
        if (playback == null) {
            handleError(ErrorUtils.exceptionSetup())
        } else {
            val listLinkPlay = playback.getLinkPlays()
            if (listLinkPlay.isEmpty()) {
                handleErrorNoData()
                return
            }
            if (countTryLinkPlayError >= listLinkPlay.size) {
                if (ConnectivityUtils.isConnected(context)) {
                    handleError(ErrorUtils.exceptionTryAllLinkPlay())
                } else {
                    handleError(ErrorUtils.exceptionNoConnection())
                }
                return
            }
            val linkPlay = listLinkPlay[countTryLinkPlayError]
            if (linkPlay.isEmpty()) {
                handleError(ErrorUtils.exceptionNoLinkPlay())
                return
            }
            initDataSource(
                linkPlay = linkPlay,
                urlIMAAd = if (isCalledFromChangeSkin) null else UZData.urlIMAAd,
                urlThumbnailsPreviewSeekBar = playback.poster
            )
            playerCallback?.isInitResult(linkPlay)
            initPlayerManager()
        }
    }

    private fun initDataSource(
        linkPlay: String,
        urlIMAAd: String?,
        urlThumbnailsPreviewSeekBar: String?
    ) {
        playerManager = UZPlayerManager.Builder(context)
            .withPlayUrl(linkPlay)
            .withIMAAdUrl(urlIMAAd)
            .build()

        isFirstStateReady = false

        timeBar?.let {
            val disable = TextUtils.isEmpty(urlThumbnailsPreviewSeekBar)
            it.isEnabled = !disable
            it.setPreviewLoader(object : PreviewLoader {
                override fun loadPreview(currentPosition: Long, max: Long) {
                    playerManager?.let { pm ->
                        pm.setPlayWhenReady(false)
                        val posterUrl = UZData.getPosterUrl()
                        if (!TextUtils.isEmpty(posterUrl))
                            ivThumbnail?.let {
                                ImageUtils.loadThumbnail(imageView = it, imageUrl = posterUrl)
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
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}
            override fun onAdEnded() {}
            override fun onAdProgress(s: Int, duration: Int, percent: Int) {}
            override fun onVideoProgress(currentMls: Long, s: Int, duration: Long, percent: Int) {
                post {
                    updateUIIbRewIconDependOnProgress(
                        currentMls = currentMls,
                        isCalledFromUZTimeBarEvent = false
                    )
                }
                if (isLIVE) {
                    post {
                        updateLiveStatus(currentMls = currentMls, duration = duration)
                    }
                }
            }
        })
        playerManager?.setDebugCallback(object : DebugCallback {
            override fun onUpdateButtonVisibilities() {
                updateUIButtonVisibilities()
            }
        })
        playerManager?.setBufferCallback(object : UZBufferListener {
            override fun onBufferChanged(bufferedDurationUs: Long, playbackSpeed: Float) {
                statsForNerdsView.setBufferedDurationUs(bufferedDurationUs)
            }
        })
    }

    private fun onStateReadyFirst() {
        updateTvDuration()
        updateUIButtonPlayPauseDependOnIsAutoStart()
        updateUIDependOnLiveStream()
        if (timeBarAtBottom) {
            UZViewUtils.visibleViews(playerView)
        }
        resizeContainerView()

        //enable from playPlaylistPosition() prevent double click
        UZViewUtils.setClickableForViews(able = true, ibSkipPrevious, ibSkipNext)

        UZData.getPlayback()?.getLinkPlay(countTryLinkPlayError)?.let {
            playerCallback?.isInitResult(it)
        }

        if (isCastingChromecast) {
            replayChromeCast()
        }
        timeBar?.hidePreview()
        UZData.isSettingPlayer = false
    }

    /**
     * When isLive = true, if not time shift then hide timber
     */
    private fun updateTimeBarWithTimeShiftStatus() {
        playerManager?.let { pm ->
            if (pm.isTimeShiftSupport) {
                if (pm.isTimeShiftOn) {
                    UZViewUtils.visibleViews(timeBar)
                } else {
                    UZViewUtils.goneViews(timeBar)
                }
            }
        }
    }

    //TODO
    //    @Subscribe(threadMode = ThreadMode.MAIN)
    //    public void onNetworkEvent(ConnectEvent event) {
    //        if (event == null || playerManager == null) return;
    //        if (!event.isConnected()) {
    //            notifyError(ErrorUtils.exceptionNoConnection());
    //        } else {
    //            if (playerManager.getExoPlaybackException() == null) {
    //                hideController();
    //                hideLayoutMsg();
    //            } else {
    //                isCalledFromConnectionEventBus = true;
    //                playerManager.setResumeIfConnectionError();
    //                if (!activityIsPausing) {
    //                    playerManager.register(this);
    //                    if (isCalledFromConnectionEventBus) {
    //                        playerManager.setRunnable();
    //                        isCalledFromConnectionEventBus = false;
    //                    }
    //                }
    //            }
    //            resume();
    //        }
    //    }

    private fun handleConnectedChromecast() {
        isCastingChromecast = true
        isCastPlayerPlayingFirst = false
        playChromecast()
        updateUIChromecast()
    }

    private fun handleDisconnectedChromecast() {
        isCastingChromecast = false
        isCastPlayerPlayingFirst = false
        updateUIChromecast()
    }

    private fun playChromecast() {
        if (UZData.getPlayback() == null || playerManager == null || playerManager?.getPlayer() == null) {
            return
        }
        playerManager?.let { pm ->
            showProgress()
            val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)

            //        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, UizaData.getInstance().getPlayback().getDescription());
//        movieMetadata.putString(MediaMetadata.KEY_TITLE, UizaData.getInstance().getPlayback().getEntityName());
//        movieMetadata.addImage(new WebImage(Uri.parse(UizaData.getInstance().getPlayback().getThumbnail())));
            // NOTE: The receiver app (on TV) should Satisfy CORS requirements
            // https://developers.google.com/cast/docs/android_sender/media_tracks#satisfy_cors_requirements

            val mediaTrackList = ArrayList<MediaTrack>()
            val duration = duration
            if (duration < 0) {
                log("invalid duration -> cannot play chromecast")
                return
            }
            val mediaInfo = MediaInfo.Builder(pm.linkPlay)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(movieMetadata)
                .setMediaTracks(mediaTrackList)
                .setStreamDuration(duration)
                .build()

            //play chromecast without screen control
            val casty = UZData.casty
            if (casty != null) {
                casty.player.loadMediaAndPlayInBackground(mediaInfo, true, lastCurrentPosition)
                casty.player.remoteMediaClient.addProgressListener({ currentPosition: Long, _: Long ->
                    if (currentPosition >= lastCurrentPosition && !isCastPlayerPlayingFirst) {
                        hideProgress()
                        isCastPlayerPlayingFirst = true
                    }
                    if (currentPosition > 0) {
                        pm.seekTo(currentPosition)
                    }
                }, 1000)
            }
        }
    }

    /* khi click vào biểu tượng casting
     * thì sẽ pause local player và bắt đầu loading lên cast player
     * khi disconnect thì local player sẽ resume*/
    private fun updateUIChromecast() {
        if (playerManager == null || rlChromeCast == null || UZAppUtils.isTV(context)) {
            return
        }
        playerManager?.let { pm ->
            if (isCastingChromecast) {
                pm.pause()
                volume = 0f
                UZViewUtils.visibleViews(rlChromeCast, ibPlayIcon)
                UZViewUtils.goneViews(ibPauseIcon)
                //casting player luôn play first với volume not mute
                //UizaData.getInstance().getCasty().setVolume(0.99);
                playerView?.controllerShowTimeoutMs = 0
            } else {
                pm.resume()
                volume = 0.99f
                UZViewUtils.goneViews(rlChromeCast, ibPlayIcon)
                UZViewUtils.visibleViews(ibPauseIcon)
                //TODO iplm volume mute on/off o cast player
                //khi quay lại exoplayer từ cast player thì mặc định sẽ bật lại âm thanh (dù cast player đang mute hay !mute)
                playerView?.controllerShowTimeoutMs = DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS
            }
        }
    }

    // ===== Stats For Nerds =====
    private fun initStatsForNerds() {
        player?.addAnalyticsListener(statsForNerdsView)
    }

    private fun addUIChromecastLayer() {
        rlChromeCast = RelativeLayout(context)
        val rlChromeCastParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        rlChromeCast?.let { rl ->
            rl.layoutParams = rlChromeCastParams
            rl.visibility = View.GONE
            rl.setBackgroundColor(Color.BLACK)

            val ibsCast = UZImageButton(context)
            ibsCast.setBackgroundColor(Color.TRANSPARENT)
            ibsCast.setImageResource(R.drawable.cast)
            val ibsCastParams =
                LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            ibsCastParams.addRule(CENTER_IN_PARENT, TRUE)
            ibsCast.layoutParams = ibsCastParams
            ibsCast.setRatioPort(5)
            ibsCast.setRatioLand(5)
            ibsCast.scaleType = ImageView.ScaleType.FIT_CENTER
            ibsCast.setColorFilter(Color.WHITE)
            rl.addView(ibsCast)

            rl.setOnClickListener(this)

            llTop?.let { ll ->
                if (ll.parent is RelativeLayout) {
                    (ll.parent as RelativeLayout).addView(rl, 0)
                }
            }
            rlLiveInfo?.let { rl ->
                if (rl.parent is RelativeLayout) {
                    (rl.parent as RelativeLayout).addView(rl, 0)
                }
            }
        }
    }

    private fun updateLiveStatus(currentMls: Long, duration: Long) {
        tvLiveStatus?.let { tv ->
            val timeToEndChunk = duration - currentMls
            if (timeToEndChunk <= targetDurationMls * 10) {
                tv.setTextColor(ContextCompat.getColor(context, R.color.text_live_color_focus))
                UZViewUtils.goneViews(tvPosition)
            } else {
                tv.setTextColor(ContextCompat.getColor(context, R.color.text_live_color))
                UZViewUtils.visibleViews(tvPosition)
            }
        }
    }

    private fun seekToEndLive() {
        val timeToEndChunk = duration - currentPosition
        if (timeToEndChunk > targetDurationMls * 10) {
            seekToLiveEdge()
        }
    }

    fun updateLiveStreamLatency(latency: Long) {
        statsForNerdsView.showTextLiveStreamLatency()
        statsForNerdsView.setTextLiveStreamLatency(StringUtils.groupingSeparatorLong(latency))
    }

    fun hideTextLiveStreamLatency() {
        statsForNerdsView.hideTextLiveStreamLatency()
    }

    private fun trackWatchingTimer(firstRun: Boolean) {
        val pi = UZData.getPlaybackInfo()
        if (pi == null) {
            log("Do not track watching")
        } else {
            viewerSessionId?.let { vsi ->
                val data = UZTrackingData(
                    info = pi,
                    viewerSessionId = vsi,
                    type = UZEventType.WATCHING
                )
                mHandler.postDelayed(
                    {
                        if (isPlaying) {
                            disposables.add(
                                pushEvent(
                                    data = data,
                                    onNext = { res: ResponseBody? ->
                                        log("send track watching: " + viewerSessionId + ", response " + res?.string())
                                    }
                                ) { error: Throwable? ->
                                    error?.printStackTrace()
                                    log("send track watching error: ${error?.toString()}")
                                })
                        }
                        trackWatchingTimer(false)

                    }, if (firstRun) 0 else DEFAULT_VALUE_TRACKING_LOOP
                ) // 5s
            }
        }
    }

    override val title: String?
        get() = null

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

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (uzTVFocusChangeListener != null) {
            uzTVFocusChangeListener?.onFocusChange(view = v, isFocus = hasFocus)
        } else if (firstViewHasFocus == null) {
            firstViewHasFocus = v
        }
    }

}
