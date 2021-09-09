package com.uiza.sampleplayer.ui.playeradvanced

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.decoder.DecoderReuseEvaluation
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.widget.previewseekbar.PreviewView
import kotlinx.android.synthetic.main.activity_player_advanced.*
import java.io.IOException

class PlayerAdvancedActivity : AppCompatActivity() {
    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_advanced)
        setupViews()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        uzVideoView.onPlayerViewCreated = {
            uzVideoView.isAutoStart = false//default is true
            uzVideoView.setAutoReplay(true)//default is false
//            uzVideoView.setPlayerControllerAlwaysVisible()//make the controller always show
            uzVideoView.setControllerHideOnTouch(true)
            uzVideoView.setEnableDoubleTapToSeek(false)//default is false
            uzVideoView.setShowLayoutDebug(false)

            log("heightTimeBar ${uzVideoView.heightTimeBar}")
            log("videoFormat ${uzVideoView.videoFormat?.width}")
            log("audioFormat ${uzVideoView.audioFormat?.bitrate}")
            log("getVideoProfileW ${uzVideoView.getVideoProfileW()}")
            log("getVideoProfileH ${uzVideoView.getVideoProfileH()}")
            log("getVideoWidth ${uzVideoView.getVideoWidth()}")
            log("getVideoHeight ${uzVideoView.getVideoHeight()}")
            log("isPlayingAd ${uzVideoView.isPlayingAd()}")
            log("isPlaying ${uzVideoView.isPlaying}")
            log("isPlayerControllerShowing ${uzVideoView.isPlayerControllerShowing}")
            log("controllerHideOnTouch ${uzVideoView.controllerHideOnTouch}")
            log("isUseController ${uzVideoView.isUseController()}")
            log("isLIVE ${uzVideoView.isLIVE}")
            log("isVOD ${uzVideoView.isVOD}")
            log("isAutoReplay ${uzVideoView.isAutoReplay()}")
            log("isAlwaysPortraitScreen ${uzVideoView.isAlwaysPortraitScreen()}")
            log("isPlayerControllerAlwayVisible ${uzVideoView.isPlayerControllerAlwayVisible()}")
            log("isEnableDoubleTapToSeek ${uzVideoView.isEnableDoubleTapToSeek()}")
            log("isShowLayoutDebug ${uzVideoView.isShowLayoutDebug()}")
            log("getSkinId ${uzVideoView.getSkinId()}")
        }
        uzVideoView.onFirstStateReady = {
            tvOnFirstStateReady.text = "onFirstStateReady"
            uzVideoView.controllerShowTimeoutMs = 15_000 //15s
            uzVideoView.setDefaultSeekValue(15_000)//15s
            uzVideoView.setUseController(true)
        }
        uzVideoView.onIsInitResult = { linkPlay ->
            tvOnIsInitResult.text = "onIsInitResult linkPlay $linkPlay"
        }
        uzVideoView.onStartPreviewTimeBar = { _: PreviewView?, progress: Int ->
            //will be called if you play a video has poster in UZPlayer
            tvOnStartPreviewTimeBar.text = "onStartPreviewTimeBar progress $progress"
        }
        uzVideoView.onStopPreviewTimeBar = { _: PreviewView?, progress: Int ->
            //will be called if you play a video has poster in UZPlayer
            tvOnStopPreviewTimeBar.text = "onStopPreviewTimeBar progress $progress"
        }
        uzVideoView.onPreviewTimeBar = { _: PreviewView?, progress: Int, fromUser: Boolean ->
            //will be called if you play a video has poster in UZPlayer
            tvOnPreviewTimeBar.text = "onPreviewTimeBar progress $progress, fromUser $fromUser"
        }
        uzVideoView.onNetworkChange = { isConnected ->
            tvOnNetworkChange.text = "onNetworkChange isConnected $isConnected"
        }
        uzVideoView.onSkinChange = {
            tvOnSkinChange.text = "onSkinChange $it"
        }
        uzVideoView.onScreenRotate = { isLandscape: Boolean ->
            tvOnScreenRotate.text = "onScreenRotate isLandscape $isLandscape"
        }
        uzVideoView.onError = {
            tvOnError.text = "$it"
        }
        uzVideoView.onDoubleTapFinished = {
            tvOnDoubleTapFinished.text = "onDoubleTapFinished"
        }
        uzVideoView.onDoubleTapProgressDown = { posX: Float, posY: Float ->
            tvOnDoubleTapProgressDown.text = "onDoubleTapProgressDown $posX $posY"
        }
        uzVideoView.onDoubleTapStarted = { posX: Float, posY: Float ->
            tvOnDoubleTapStarted.text = "onDoubleTapStarted $posX $posY"
        }
        uzVideoView.onDoubleTapProgressUp = { posX: Float, posY: Float ->
            tvOnDoubleTapProgressUp.text = "onDoubleTapProgressUp $posX $posY"
        }
        uzVideoView.onPlayerStateChanged = { playbackState: Int ->
            when (playbackState) {
                Player.STATE_IDLE -> {
                    tvOnPlayerStateChanged.text = "onPlayerStateChanged playbackState STATE_IDLE"
                }
                Player.STATE_BUFFERING -> {
                    tvOnPlayerStateChanged.text =
                        "onPlayerStateChanged playbackState STATE_BUFFERING"
                }
                Player.STATE_READY -> {
                    tvOnPlayerStateChanged.text = "onPlayerStateChanged playbackState STATE_READY"
                }
                Player.STATE_ENDED -> {
                    tvOnPlayerStateChanged.text = "onPlayerStateChanged playbackState STATE_ENDED"
                }
            }
        }
        uzVideoView.onCurrentWindowDynamic = { isLIVE ->
            tvOnCurrentWindowDynamic.text = "onCurrentWindowDynamic isLIVE $isLIVE"
        }
        uzVideoView.onSurfaceRedrawNeeded = {
            tvOnSurfaceRedrawNeeded.text = "onSurfaceRedrawNeeded"
        }
        uzVideoView.onSurfaceCreated = {
            tvOnSurfaceCreated.text = "onSurfaceCreated"
        }
        uzVideoView.onSurfaceChanged = { _: SurfaceHolder, format: Int, width: Int, height: Int ->
            tvOnSurfaceChanged.text =
                "onSurfaceChanged format $format, width $width, height $height"
        }
        uzVideoView.onSurfaceDestroyed = {
            tvOnSurfaceDestroyed.text = "onSurfaceDestroyed"
        }
        uzVideoView.onShuffleModeChanged =
            { eventTime: AnalyticsListener.EventTime, shuffleModeEnabled: Boolean ->
                tvOnShuffleModeChanged.text =
                    "onShuffleModeChanged ${eventTime.currentPlaybackPositionMs}, shuffleModeEnabled $shuffleModeEnabled"
            }
        uzVideoView.onLoadStarted = { eventTime: AnalyticsListener.EventTime,
                                      loadEventInfo: LoadEventInfo,
                                      mediaLoadData: MediaLoadData ->
            tvOnLoadStarted.text =
                "onLoadStarted ${eventTime.currentPlaybackPositionMs}, loadEventInfo ${loadEventInfo.bytesLoaded}, mediaLoadData ${mediaLoadData.dataType}"
        }
        uzVideoView.onLoadCompleted = { eventTime: AnalyticsListener.EventTime,
                                        loadEventInfo: LoadEventInfo,
                                        mediaLoadData: MediaLoadData ->
            tvOnLoadCompleted.text =
                "onLoadCompleted eventTime ${eventTime.currentPlaybackPositionMs}, loadEventInfo ${loadEventInfo.bytesLoaded}, mediaLoadData ${mediaLoadData.dataType}"
        }
        uzVideoView.onLoadCanceled = { eventTime: AnalyticsListener.EventTime,
                                       loadEventInfo: LoadEventInfo,
                                       mediaLoadData: MediaLoadData ->
            tvOnLoadCanceled.text =
                "onLoadCanceled eventTime ${eventTime.currentPlaybackPositionMs}, loadEventInfo ${loadEventInfo.bytesLoaded}, mediaLoadData ${mediaLoadData.dataType}"
        }
        uzVideoView.onLoadError = { eventTime: AnalyticsListener.EventTime,
                                    loadEventInfo: LoadEventInfo,
                                    mediaLoadData: MediaLoadData,
                                    error: IOException,
                                    wasCanceled: Boolean ->
            tvOnLoadError.text =
                "onLoadError eventTime ${eventTime.currentPlaybackPositionMs}, loadEventInfo ${loadEventInfo.bytesLoaded}, mediaLoadData ${mediaLoadData.dataType}, error $error, wasCanceled $wasCanceled"
        }
        uzVideoView.onDownstreamFormatChanged = { eventTime: AnalyticsListener.EventTime,
                                                  mediaLoadData: MediaLoadData ->
            tvOnDownstreamFormatChanged.text =
                "onDownstreamFormatChanged eventTime ${eventTime.currentPlaybackPositionMs}, mediaLoadData ${mediaLoadData.dataType}"
        }
        uzVideoView.onUpstreamDiscarded = { eventTime: AnalyticsListener.EventTime,
                                            mediaLoadData: MediaLoadData ->
            tvOnUpstreamDiscarded.text =
                "onUpstreamDiscarded eventTime ${eventTime.currentPlaybackPositionMs}, mediaLoadData ${mediaLoadData.dataType}"
        }
        uzVideoView.onBandwidthEstimate = { eventTime: AnalyticsListener.EventTime,
                                            totalLoadTimeMs: Int,
                                            totalBytesLoaded: Long,
                                            bitrateEstimate: Long ->
            tvOnBandwidthEstimate.text =
                "onBandwidthEstimate eventTime ${eventTime.currentPlaybackPositionMs}, totalLoadTimeMs $totalLoadTimeMs, totalBytesLoaded $totalBytesLoaded, bitrateEstimate $bitrateEstimate"
        }
        uzVideoView.onAudioEnabled =
            { eventTime: AnalyticsListener.EventTime, decoderCounters: DecoderCounters ->
                tvOnAudioEnabled.text =
                    "onAudioEnabled eventTime ${eventTime.currentPlaybackPositionMs}, decoderCounters ${decoderCounters.decoderInitCount}"
            }
        uzVideoView.onAudioDecoderInitialized = { eventTime: AnalyticsListener.EventTime,
                                                  decoderName: String,
                                                  initializedTimestampMs: Long,
                                                  initializationDurationMs: Long ->
            tvOnAudioDecoderInitialized.text =
                "onAudioDecoderInitialized eventTime ${eventTime.currentPlaybackPositionMs}, decoderName $decoderName, initializedTimestampMs $initializedTimestampMs, initializationDurationMs $initializationDurationMs"
        }
        uzVideoView.onAudioInputFormatChanged = { eventTime: AnalyticsListener.EventTime,
                                                  format: Format,
                                                  decoderReuseEvaluation: DecoderReuseEvaluation? ->
            tvOnAudioInputFormatChanged.text =
                "onAudioInputFormatChanged eventTime ${eventTime.currentPlaybackPositionMs}, format ${format.bitrate}, decoderReuseEvaluation $decoderReuseEvaluation"
        }
        uzVideoView.onAudioPositionAdvancing = { eventTime: AnalyticsListener.EventTime,
                                                 playoutStartSystemTimeMs: Long ->
            tvOnAudioPositionAdvancing.text =
                "onAudioPositionAdvancing eventTime ${eventTime.currentPlaybackPositionMs}, playoutStartSystemTimeMs $playoutStartSystemTimeMs"
        }
        uzVideoView.onAudioUnderrun = { eventTime: AnalyticsListener.EventTime,
                                        bufferSize: Int,
                                        bufferSizeMs: Long,
                                        elapsedSinceLastFeedMs: Long ->
            tvOnAudioUnderrun.text =
                "onAudioUnderrun eventTime ${eventTime.currentPlaybackPositionMs}, bufferSize $bufferSize, bufferSizeMs $bufferSizeMs, elapsedSinceLastFeedMs $elapsedSinceLastFeedMs"
        }
        uzVideoView.onAudioDecoderReleased = { eventTime: AnalyticsListener.EventTime,
                                               decoderName: String ->
            tvOnAudioDecoderReleased.text =
                "onAudioDecoderReleased eventTime ${eventTime.currentPlaybackPositionMs}, decoderName $decoderName"
        }
        uzVideoView.onAudioDisabled = { eventTime: AnalyticsListener.EventTime,
                                        decoderCounters: DecoderCounters ->
            tvOnAudioDisabled.text =
                "onAudioDisabled eventTime ${eventTime.currentPlaybackPositionMs}, decoderCounters ${decoderCounters.decoderInitCount}"
        }
        uzVideoView.onAudioSinkError =
            { eventTime: AnalyticsListener.EventTime, audioSinkError: java.lang.Exception ->
                tvOnAudioSinkError.text =
                    "onAudioSinkError eventTime ${eventTime.currentPlaybackPositionMs}, audioSinkError $audioSinkError"
            }
        uzVideoView.onAudioCodecError = { eventTime: AnalyticsListener.EventTime,
                                          audioCodecError: java.lang.Exception ->
            tvOnAudioCodecError.text =
                "onAudioCodecError eventTime ${eventTime.currentPlaybackPositionMs}, audioCodecError $audioCodecError"
        }
        uzVideoView.onVideoEnabled = { eventTime: AnalyticsListener.EventTime,
                                       decoderCounters: DecoderCounters ->
            tvOnVideoEnabled.text =
                "onVideoEnabled eventTime ${eventTime.currentPlaybackPositionMs}, decoderCounters ${decoderCounters.decoderInitCount}"
        }
        uzVideoView.onVideoDecoderInitialized = { eventTime: AnalyticsListener.EventTime,
                                                  decoderName: String,
                                                  initializedTimestampMs: Long,
                                                  initializationDurationMs: Long ->
            tvOnVideoDecoderInitialized.text =
                "onVideoDecoderInitialized eventTime ${eventTime.currentPlaybackPositionMs}, decoderName $decoderName, initializedTimestampMs $initializedTimestampMs, initializationDurationMs $initializationDurationMs"
        }
        uzVideoView.onVideoInputFormatChanged = { eventTime: AnalyticsListener.EventTime,
                                                  format: Format,
                                                  decoderReuseEvaluation: DecoderReuseEvaluation? ->
            tvOnVideoInputFormatChanged.text =
                "onVideoInputFormatChanged eventTime ${eventTime.currentPlaybackPositionMs}, format ${format.bitrate}, decoderReuseEvaluation $decoderReuseEvaluation"
        }
        uzVideoView.onDroppedVideoFrames = { eventTime: AnalyticsListener.EventTime,
                                             droppedFrames: Int,
                                             elapsedMs: Long ->
            tvOnDroppedVideoFrames.text =
                "onDroppedVideoFrames eventTime ${eventTime.currentPlaybackPositionMs}, droppedFrames $droppedFrames, elapsedMs $elapsedMs"
        }
        uzVideoView.onVideoDecoderReleased = { eventTime: AnalyticsListener.EventTime,
                                               decoderName: String ->
            tvOnVideoDecoderReleased.text =
                "onVideoDecoderReleased eventTime ${eventTime.currentPlaybackPositionMs}, decoderName $decoderName"
        }
        uzVideoView.onVideoDisabled = { eventTime: AnalyticsListener.EventTime,
                                        decoderCounters: DecoderCounters ->
            tvOnVideoDisabled.text =
                "onVideoDisabled eventTime ${eventTime.currentPlaybackPositionMs}, decoderCounters ${decoderCounters.decoderInitCount}"
        }
        uzVideoView.onVideoFrameProcessingOffset = { eventTime: AnalyticsListener.EventTime,
                                                     totalProcessingOffsetUs: Long,
                                                     frameCount: Int ->
            tvOnVideoFrameProcessingOffset.text =
                "onVideoFrameProcessingOffset eventTime ${eventTime.currentPlaybackPositionMs}, totalProcessingOffsetUs $totalProcessingOffsetUs, frameCount $frameCount"
        }
        uzVideoView.onPlayerReleased = { eventTime: AnalyticsListener.EventTime ->
            tvOnPlayerReleased.text =
                "onPlayerReleased eventTime ${eventTime.currentPlaybackPositionMs}"
        }
        uzVideoView.onProgressChange =
            { currentPosition: Long, duration: Long, isPlayingAd: Boolean? ->
                tvOnProgressChange.text =
                    "onProgressChange currentPosition $currentPosition, duration $duration, isPlayingAd $isPlayingAd"
            }
        btPlayVOD.setOnClickListener {
            etLinkPlay.setText(Constant.LINK_PLAY_VOD)
            btPlayLink.performClick()
        }
        btPlayLive.setOnClickListener {
            etLinkPlay.setText(Constant.LINK_PLAY_LIVE)
            btPlayLink.performClick()
        }
        btPlayLink.setOnClickListener {
            onPlay(etLinkPlay.text.toString().trim())
        }
        btSeekTo.setOnClickListener {
            uzVideoView.seekTo(16_000)
        }
        btSeekToLiveEdge.setOnClickListener {
            uzVideoView.seekToLiveEdge()
        }
        btResume.setOnClickListener {
            uzVideoView.resume()
        }
        btPause.setOnClickListener {
            uzVideoView.pause()
        }
        btTogglePlayPause.setOnClickListener {
            uzVideoView.togglePlayPause()
        }
        btReplay.setOnClickListener {
            uzVideoView.replay()
        }
        btBackScreen.setOnClickListener {
            uzVideoView.clickBackScreen()
        }
        btToggleVolumeMute.setOnClickListener {
            uzVideoView.toggleVolumeMute()
        }
        btShowSettingsDialog.setOnClickListener {
            uzVideoView.showSettingsDialog()
        }
        btShowSpeed.setOnClickListener {
            uzVideoView.showSpeed()
        }
        btSetSpeed.setOnClickListener {
            uzVideoView.setSpeed(2f)
        }
        btShowController.setOnClickListener {
            uzVideoView.showController()
        }
        btHideController.setOnClickListener {
            uzVideoView.hideController()
        }
        btClickAudio.setOnClickListener {
            uzVideoView.clickAudio()
        }
        btClickQuality.setOnClickListener {
            uzVideoView.clickQuality()
        }
        btClickCaptions.setOnClickListener {
            uzVideoView.clickCaptions()
        }
        btSeekToForward.setOnClickListener {
            uzVideoView.seekToForward()
        }
        btSeekToForward7.setOnClickListener {
            uzVideoView.seekToForward(7_000)
        }
        btSeekToBackward.setOnClickListener {
            uzVideoView.seekToBackward()
        }
        btSeekToBackward7.setOnClickListener {
            uzVideoView.seekToBackward(7_000)
        }
        btToggleFullscreen.setOnClickListener {
            uzVideoView.toggleFullscreen()
        }
        btRetry.setOnClickListener {
            uzVideoView.retry()
        }
        btGetListTrackVideo.setOnClickListener {
            val list = uzVideoView.getListTrack(showDialog = false, title = "", rendererIndex = 0)
            var msg = ""
            list?.forEach {
                msg += "${it.description} ~ ${it.format.toString()}\n"
            }
            log(msg)
            toast(msg)
        }
        btGetListTrackAudio.setOnClickListener {
            val list = uzVideoView.getListTrack(showDialog = false, title = "", rendererIndex = 1)
            var msg = ""
            list?.forEach {
                msg += "${it.description} ~ ${it.format.toString()}\n"
            }
            log(msg)
            toast(msg)
        }
        btGetListTrackCaptions.setOnClickListener {
            val list = uzVideoView.getListTrack(showDialog = false, title = "", rendererIndex = 2)
            var msg = ""
            list?.forEach {
                msg += "${it.description} ~ ${it.format.toString()}\n"
            }
            log(msg)
            toast(msg)
        }
    }

    private fun onPlay(link: String) {
        if (link.isEmpty()) {
            Toast.makeText(this, "Link play is empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (uzVideoView.isViewCreated()) {
            val uzPlayback = UZPlayback(
                linkPlay = link,
                urlIMAAd = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator="
            )
            uzVideoView.play(uzPlayback)
        }
    }

    public override fun onDestroy() {
        uzVideoView.onDestroyView()
        super.onDestroy()
    }

    public override fun onResume() {
        super.onResume()
        uzVideoView.onResumeView()
    }

    public override fun onPause() {
        super.onPause()
        uzVideoView.onPauseView()
    }

    override fun onStart() {
        super.onStart()
        uzVideoView.onStartView()
    }

    override fun onStop() {
        super.onStop()
        uzVideoView.onStopView()
    }

    override fun onBackPressed() {
        if (!uzVideoView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
