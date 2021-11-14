package com.uiza.sampleplayer.ui.playeradvanced

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.decoder.DecoderReuseEvaluation
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoSize
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.widget.previewseekbar.PreviewView
import kotlinx.android.synthetic.main.activity_player_advanced.*
import java.io.IOException

class PlayerAdvancedActivity : AppCompatActivity() {
    private var isShowingTrackSelectionDialogCustom = false

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
        // will be called when player is created
        uzVideoView.onPlayerViewCreated = {
            uzVideoView.isAutoStart = false // default is true
            uzVideoView.setAutoReplay(false) // default is false
            uzVideoView.setEnableDoubleTapToSeek(false) // default is false
            uzVideoView.setShowLayoutDebug(false) // hide debug layout

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
            log("isControllerHideOnTouch ${uzVideoView.isControllerHideOnTouch()}")
            log("isUseController ${uzVideoView.isUseController()}")
            log("isLIVE ${uzVideoView.isLIVE}")
            log("isVOD ${uzVideoView.isVOD}")
            log("isAutoReplay ${uzVideoView.isAutoReplay()}")
            log("isAlwaysPortraitScreen ${uzVideoView.isAlwaysPortraitScreen()}")
            log("isPlayerControllerAlwayVisible ${uzVideoView.isPlayerControllerAlwayVisible()}")
            log("isEnableDoubleTapToSeek ${uzVideoView.isEnableDoubleTapToSeek()}")
            log("isShowLayoutDebug ${uzVideoView.isShowLayoutDebug()}")
            log("getSkinId ${uzVideoView.getSkinId()}")
            log("controllerAutoShow ${uzVideoView.controllerAutoShow}")
            log("volume ${uzVideoView.volume}")
        }

        // the first time the player has playbackState == Player.STATE_READY
        uzVideoView.onFirstStateReady = {
            tvOnFirstStateReady.text = "onFirstStateReady"
            uzVideoView.setUseController(true) // use controller
            uzVideoView.setDefaultSeekValue(15_000) // seek value = 15s
            uzVideoView.setControllerHideOnTouch(true) // show/hide controller if touch in the player

            val isAlwaysVisibleController = false
            if (isAlwaysVisibleController) {
                uzVideoView.setPlayerControllerAlwaysVisible() // make the controller always show
            } else {
                // in case you want to set value show timeout of controller
                uzVideoView.controllerShowTimeoutMs = 10_000 // 10s
            }
        }

        // result when init resources
        uzVideoView.onIsInitResult = { linkPlay ->
            tvOnIsInitResult.text = "onIsInitResult linkPlay $linkPlay"
        }
        // will be called if you play a video has poster in player
        uzVideoView.onStartPreviewTimeBar = { _: PreviewView?, progress: Int ->
            tvOnStartPreviewTimeBar.text = "onStartPreviewTimeBar progress $progress"
        }
        // will be called if you play a video has poster in player
        uzVideoView.onStopPreviewTimeBar = { _: PreviewView?, progress: Int ->
            tvOnStopPreviewTimeBar.text = "onStopPreviewTimeBar progress $progress"
        }
        // will be called if you play a video has poster in player
        uzVideoView.onPreviewTimeBar = { _: PreviewView?, progress: Int, fromUser: Boolean ->
            tvOnPreviewTimeBar.text = "onPreviewTimeBar progress $progress, fromUser $fromUser"
        }
        // will be called if your network is changed
        uzVideoView.onNetworkChange = { isConnected ->
            tvOnNetworkChange.text = "onNetworkChange isConnected $isConnected"
        }
        // will be called when you change skin of player
        uzVideoView.onSkinChange = {
            tvOnSkinChange.text = "onSkinChange $it"
        }
        // will be called when screen is rotated
        uzVideoView.onScreenRotate = { isLandscape: Boolean ->
            tvOnScreenRotate.text = "onScreenRotate isLandscape $isLandscape"
        }
        // will be called when the player has any UZException
        uzVideoView.onError = {
            tvOnError.text = "$it"
        }
        // listener for double tap on the player
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
        // will be called when player state is changed
        uzVideoView.onPlayerStateChanged = { playbackState: Int ->
            when (playbackState) {
                // The player does not have any media to play
                Player.STATE_IDLE -> {
                    tvOnPlayerStateChanged.text = "onPlayerStateChanged playbackState STATE_IDLE"
                }
                // The player is not able to immediately play from its current position. This state typically occurs when more data needs to be loaded
                Player.STATE_BUFFERING -> {
                    tvOnPlayerStateChanged.text =
                        "onPlayerStateChanged playbackState STATE_BUFFERING"
                }
                // The player is able to immediately play from its current position. The player will be playing if getPlayWhenReady() is true, and paused otherwise
                Player.STATE_READY -> {
                    tvOnPlayerStateChanged.text = "onPlayerStateChanged playbackState STATE_READY"
                }
                // The player has finished playing the media.
                Player.STATE_ENDED -> {
                    tvOnPlayerStateChanged.text = "onPlayerStateChanged playbackState STATE_ENDED"
                }
            }
        }
        // help you know the current video is Live content or not
        uzVideoView.onCurrentWindowDynamic = { isLIVE ->
            tvOnCurrentWindowDynamic.text = "onCurrentWindowDynamic isLIVE $isLIVE"
        }
        // listener for surface view
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
        // Called when the shuffle mode changed.
        // Params:
        // eventTime – The event time.
        // shuffleModeEnabled – Whether the shuffle mode is enabled.
        uzVideoView.onShuffleModeChanged =
            { eventTime: AnalyticsListener.EventTime, shuffleModeEnabled: Boolean ->
                tvOnShuffleModeChanged.text =
                    "onShuffleModeChanged ${eventTime.currentPlaybackPositionMs}, shuffleModeEnabled $shuffleModeEnabled"
            }
        // Called when a media source started loading data.
        // Params:
        // eventTime – The event time.
        // loadEventInfo – The LoadEventInfo defining the load event.
        // mediaLoadData – The MediaLoadData defining the data being loaded.
        uzVideoView.onLoadStarted = { eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData ->
            tvOnLoadStarted.text =
                "onLoadStarted ${eventTime.currentPlaybackPositionMs}, loadEventInfo ${loadEventInfo.bytesLoaded}, mediaLoadData ${mediaLoadData.dataType}"
        }

        // Called when a media source completed loading data.
        // Params:
        // eventTime – The event time.
        // loadEventInfo – The LoadEventInfo defining the load event.
        // mediaLoadData – The MediaLoadData defining the data being loaded.
        uzVideoView.onLoadCompleted = { eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData ->
            tvOnLoadCompleted.text =
                "onLoadCompleted eventTime ${eventTime.currentPlaybackPositionMs}, loadEventInfo ${loadEventInfo.bytesLoaded}, mediaLoadData ${mediaLoadData.dataType}"
        }

        // Called when a media source canceled loading data.
        // Params:
        // eventTime – The event time.
        // loadEventInfo – The LoadEventInfo defining the load event.
        // mediaLoadData – The MediaLoadData defining the data being loaded.
        uzVideoView.onLoadCanceled = { eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData ->
            tvOnLoadCanceled.text =
                "onLoadCanceled eventTime ${eventTime.currentPlaybackPositionMs}, loadEventInfo ${loadEventInfo.bytesLoaded}, mediaLoadData ${mediaLoadData.dataType}"
        }

        // Called when a media source loading error occurred.
        // This method being called does not indicate that playback has failed, or that it will fail. The player may be able to recover from the error. Hence applications should not implement this method to display a user visible error or initiate an application level retry. Player.Listener.onPlayerError is the appropriate place to implement such behavior. This method is called to provide the application with an opportunity to log the error if it wishes to do so.
        // Params:
        // eventTime – The event time.
        // loadEventInfo – The LoadEventInfo defining the load event.
        // mediaLoadData – The MediaLoadData defining the data being loaded.
        // error – The load error.
        // wasCanceled – Whether the load was canceled as a result of the error
        uzVideoView.onLoadError = { eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
            error: IOException,
            wasCanceled: Boolean ->
            tvOnLoadError.text =
                "onLoadError eventTime ${eventTime.currentPlaybackPositionMs}, loadEventInfo ${loadEventInfo.bytesLoaded}, mediaLoadData ${mediaLoadData.dataType}, error $error, wasCanceled $wasCanceled"
        }

        // Called when the downstream format sent to the renderers changed.
        // Params:
        // eventTime – The event time.
        // mediaLoadData – The MediaLoadData defining the newly selected media data
        uzVideoView.onDownstreamFormatChanged = { eventTime: AnalyticsListener.EventTime,
            mediaLoadData: MediaLoadData ->
            tvOnDownstreamFormatChanged.text =
                "onDownstreamFormatChanged eventTime ${eventTime.currentPlaybackPositionMs}, mediaLoadData ${mediaLoadData.dataType}"
        }

        // Called when data is removed from the back of a media buffer, typically so that it can be re-buffered in a different format.
        // Params:
        // eventTime – The event time.
        // mediaLoadData – The MediaLoadData defining the media being discarded.
        uzVideoView.onUpstreamDiscarded = { eventTime: AnalyticsListener.EventTime,
            mediaLoadData: MediaLoadData ->
            tvOnUpstreamDiscarded.text =
                "onUpstreamDiscarded eventTime ${eventTime.currentPlaybackPositionMs}, mediaLoadData ${mediaLoadData.dataType}"
        }

        // Called when the bandwidth estimate for the current data source has been updated.
        // Params:
        // eventTime – The event time.
        // totalLoadTimeMs – The total time spend loading this update is based on, in milliseconds.
        // totalBytesLoaded – The total bytes loaded this update is based on.
        // bitrateEstimate – The bandwidth estimate, in bits per second
        uzVideoView.onBandwidthEstimate = { eventTime: AnalyticsListener.EventTime,
            totalLoadTimeMs: Int,
            totalBytesLoaded: Long,
            bitrateEstimate: Long ->
            tvOnBandwidthEstimate.text =
                "onBandwidthEstimate eventTime ${eventTime.currentPlaybackPositionMs}, totalLoadTimeMs $totalLoadTimeMs, totalBytesLoaded $totalBytesLoaded, bitrateEstimate $bitrateEstimate"
        }

        // Called when an audio renderer is enabled.
        // Params:
        // eventTime – The event time.
        // decoderCounters – DecoderCounters that will be updated by the renderer for as long as it remains enabled.
        uzVideoView.onAudioEnabled =
            { eventTime: AnalyticsListener.EventTime, decoderCounters: DecoderCounters ->
                tvOnAudioEnabled.text =
                    "onAudioEnabled eventTime ${eventTime.currentPlaybackPositionMs}, decoderCounters ${decoderCounters.decoderInitCount}"
            }

        // Called when an audio renderer creates a decoder.
        // Params:
        // eventTime – The event time.
        // decoderName – The decoder that was created.
        // initializedTimestampMs – SystemClock.elapsedRealtime() when initialization finished.
        // initializationDurationMs – The time taken to initialize the decoder in milliseconds.
        uzVideoView.onAudioDecoderInitialized = { eventTime: AnalyticsListener.EventTime,
            decoderName: String,
            initializedTimestampMs: Long,
            initializationDurationMs: Long ->
            tvOnAudioDecoderInitialized.text =
                "onAudioDecoderInitialized eventTime ${eventTime.currentPlaybackPositionMs}, decoderName $decoderName, initializedTimestampMs $initializedTimestampMs, initializationDurationMs $initializationDurationMs"
        }

        // Called when the format of the media being consumed by an audio renderer changes.
        // Params:
        // eventTime – The event time.
        // format – The new format.
        // decoderReuseEvaluation – The result of the evaluation to determine whether an existing decoder instance can be reused for the new format, or null if the renderer did not have a decoder.
        uzVideoView.onAudioInputFormatChanged = { eventTime: AnalyticsListener.EventTime,
            format: Format,
            decoderReuseEvaluation: DecoderReuseEvaluation? ->
            tvOnAudioInputFormatChanged.text =
                "onAudioInputFormatChanged eventTime ${eventTime.currentPlaybackPositionMs}, format ${format.bitrate}, decoderReuseEvaluation $decoderReuseEvaluation"
        }

        // Called when the audio position has increased for the first time since the last pause or position reset.
        // Params:
        // eventTime – The event time.
        // playoutStartSystemTimeMs – The approximate derived System.currentTimeMillis() at which playout started.
        uzVideoView.onAudioPositionAdvancing = { eventTime: AnalyticsListener.EventTime,
            playoutStartSystemTimeMs: Long ->
            tvOnAudioPositionAdvancing.text =
                "onAudioPositionAdvancing eventTime ${eventTime.currentPlaybackPositionMs}, playoutStartSystemTimeMs $playoutStartSystemTimeMs"
        }

        // Called when an audio underrun occurs.
        // Params:
        // eventTime – The event time.
        // bufferSize – The size of the audio output buffer, in bytes.
        // bufferSizeMs – The size of the audio output buffer, in milliseconds, if it contains PCM encoded audio. C.TIME_UNSET if the output buffer contains non-PCM encoded audio.
        // elapsedSinceLastFeedMs – The time since audio was last written to the output buffer.
        uzVideoView.onAudioUnderrun = { eventTime: AnalyticsListener.EventTime,
            bufferSize: Int,
            bufferSizeMs: Long,
            elapsedSinceLastFeedMs: Long ->
            tvOnAudioUnderrun.text =
                "onAudioUnderrun eventTime ${eventTime.currentPlaybackPositionMs}, bufferSize $bufferSize, bufferSizeMs $bufferSizeMs, elapsedSinceLastFeedMs $elapsedSinceLastFeedMs"
        }

        // Called when an audio renderer releases a decoder.
        // Params:
        // eventTime – The event time.
        // decoderName – The decoder that was released.
        uzVideoView.onAudioDecoderReleased = { eventTime: AnalyticsListener.EventTime,
            decoderName: String ->
            tvOnAudioDecoderReleased.text =
                "onAudioDecoderReleased eventTime ${eventTime.currentPlaybackPositionMs}, decoderName $decoderName"
        }

        // Called when an audio renderer is disabled.
        // Params:
        // eventTime – The event time.
        // decoderCounters – DecoderCounters that were updated by the renderer
        uzVideoView.onAudioDisabled = { eventTime: AnalyticsListener.EventTime,
            decoderCounters: DecoderCounters ->
            tvOnAudioDisabled.text =
                "onAudioDisabled eventTime ${eventTime.currentPlaybackPositionMs}, decoderCounters ${decoderCounters.decoderInitCount}"
        }

        // Called when AudioSink has encountered an error.
        // This method being called does not indicate that playback has failed, or that it will fail. The player may be able to recover from the error. Hence applications should not implement this method to display a user visible error or initiate an application level retry. Player.Listener.onPlayerError is the appropriate place to implement such behavior. This method is called to provide the application with an opportunity to log the error if it wishes to do so.
        // Params:
        // eventTime – The event time.
        // audioSinkError – The error that occurred. Typically an AudioSink.InitializationException, a AudioSink.WriteException, or an AudioSink.UnexpectedDiscontinuityException
        uzVideoView.onAudioSinkError =
            { eventTime: AnalyticsListener.EventTime, audioSinkError: java.lang.Exception ->
                tvOnAudioSinkError.text =
                    "onAudioSinkError eventTime ${eventTime.currentPlaybackPositionMs}, audioSinkError $audioSinkError"
            }

        // Called when an audio decoder encounters an error.
        // This method being called does not indicate that playback has failed, or that it will fail. The player may be able to recover from the error. Hence applications should not implement this method to display a user visible error or initiate an application level retry. Player.Listener.onPlayerError is the appropriate place to implement such behavior. This method is called to provide the application with an opportunity to log the error if it wishes to do so.
        // Params:
        // eventTime – The event time.
        // audioCodecError – The error. Typically a MediaCodec.CodecException if the renderer uses MediaCodec, or a DecoderException if the renderer uses a software decoder
        uzVideoView.onAudioCodecError = { eventTime: AnalyticsListener.EventTime,
            audioCodecError: java.lang.Exception ->
            tvOnAudioCodecError.text =
                "onAudioCodecError eventTime ${eventTime.currentPlaybackPositionMs}, audioCodecError $audioCodecError"
        }

        // Called when a video renderer is enabled.
        // Params:
        // eventTime – The event time.
        // decoderCounters – DecoderCounters that will be updated by the renderer for as long as it remains enabled.
        uzVideoView.onVideoEnabled = { eventTime: AnalyticsListener.EventTime,
            decoderCounters: DecoderCounters ->
            tvOnVideoEnabled.text =
                "onVideoEnabled eventTime ${eventTime.currentPlaybackPositionMs}, decoderCounters ${decoderCounters.decoderInitCount}"
        }

        // Called when a video renderer creates a decoder.
        // Params:
        // eventTime – The event time.
        // decoderName – The decoder that was created.
        // initializedTimestampMs – SystemClock.elapsedRealtime() when initialization finished.
        // initializationDurationMs – The time taken to initialize the decoder in milliseconds.
        uzVideoView.onVideoDecoderInitialized = { eventTime: AnalyticsListener.EventTime,
            decoderName: String,
            initializedTimestampMs: Long,
            initializationDurationMs: Long ->
            tvOnVideoDecoderInitialized.text =
                "onVideoDecoderInitialized eventTime ${eventTime.currentPlaybackPositionMs}, decoderName $decoderName, initializedTimestampMs $initializedTimestampMs, initializationDurationMs $initializationDurationMs"
        }

        // Called when the format of the media being consumed by a video renderer changes.
        // Params:
        // eventTime – The event time.
        // format – The new format.
        // decoderReuseEvaluation – The result of the evaluation to determine whether an existing decoder instance can be reused for the new format, or null if the renderer did not have a decoder.
        uzVideoView.onVideoInputFormatChanged = { eventTime: AnalyticsListener.EventTime,
            format: Format,
            decoderReuseEvaluation: DecoderReuseEvaluation? ->
            tvOnVideoInputFormatChanged.text =
                "onVideoInputFormatChanged eventTime ${eventTime.currentPlaybackPositionMs}, format ${format.bitrate}, decoderReuseEvaluation $decoderReuseEvaluation"
        }

        // Called after video frames have been dropped.
        // Params:
        // eventTime – The event time.
        // droppedFrames – The number of dropped frames since the last call to this method.
        // elapsedMs – The duration in milliseconds over which the frames were dropped. This duration is timed from when the renderer was started or from when dropped frames were last reported (whichever was more recent), and not from when the first of the reported drops occurred.
        uzVideoView.onDroppedVideoFrames = { eventTime: AnalyticsListener.EventTime,
            droppedFrames: Int,
            elapsedMs: Long ->
            tvOnDroppedVideoFrames.text =
                "onDroppedVideoFrames eventTime ${eventTime.currentPlaybackPositionMs}, droppedFrames $droppedFrames, elapsedMs $elapsedMs"
        }

        // Called when a video renderer releases a decoder.
        // Params:
        // eventTime – The event time.
        // decoderName – The decoder that was released.
        uzVideoView.onVideoDecoderReleased = { eventTime: AnalyticsListener.EventTime,
            decoderName: String ->
            tvOnVideoDecoderReleased.text =
                "onVideoDecoderReleased eventTime ${eventTime.currentPlaybackPositionMs}, decoderName $decoderName"
        }

        // Called when a video renderer is disabled.
        // Params:
        // eventTime – The event time.
        // decoderCounters – DecoderCounters that were updated by the renderer.
        uzVideoView.onVideoDisabled = { eventTime: AnalyticsListener.EventTime,
            decoderCounters: DecoderCounters ->
            tvOnVideoDisabled.text =
                "onVideoDisabled eventTime ${eventTime.currentPlaybackPositionMs}, decoderCounters ${decoderCounters.decoderInitCount}"
        }

        // Called when there is an update to the video frame processing offset reported by a video renderer.
        // The processing offset for a video frame is the difference between the time at which the frame became available to render, and the time at which it was scheduled to be rendered. A positive value indicates the frame became available early enough, whereas a negative value indicates that the frame wasn't available until after the time at which it should have been rendered.
        // Params:
        // eventTime – The event time.
        // totalProcessingOffsetUs – The sum of the video frame processing offsets for frames rendered since the last call to this method.
        // frameCount – The number to samples included in totalProcessingOffsetUs.
        uzVideoView.onVideoFrameProcessingOffset = { eventTime: AnalyticsListener.EventTime,
            totalProcessingOffsetUs: Long,
            frameCount: Int ->
            tvOnVideoFrameProcessingOffset.text =
                "onVideoFrameProcessingOffset eventTime ${eventTime.currentPlaybackPositionMs}, totalProcessingOffsetUs $totalProcessingOffsetUs, frameCount $frameCount"
        }

        // Called when the Player is released.
        // Params:
        // eventTime – The event time.
        uzVideoView.onPlayerReleased = { eventTime: AnalyticsListener.EventTime ->
            tvOnPlayerReleased.text =
                "onPlayerReleased eventTime ${eventTime.currentPlaybackPositionMs}"
        }
        uzVideoView.onProgressChange =
            { currentPosition: Long, duration: Long, isPlayingAd: Boolean? ->
                tvOnProgressChange.text =
                    "onProgressChange currentPosition $currentPosition, duration $duration, isPlayingAd $isPlayingAd"
            }

        // Called each time there's a change in the size of the video being rendered.
        // Params:
        // videoSize – The new size of the video.
        uzVideoView.onVideoSizeChanged = { videoSize: VideoSize ->
            tvOnVideoSizeChanged.text =
                "onVideoSizeChanged videoSize width: ${videoSize.width}, height: ${videoSize.height}, pixelWidthHeightRatio: ${videoSize.pixelWidthHeightRatio}, unappliedRotationDegrees: ${videoSize.unappliedRotationDegrees}"
        }

        // Called each time there's a change in the size of the surface onto which the video is being rendered.
        // Params:
        // width – The surface width in pixels. May be C.LENGTH_UNSET if unknown, or 0 if the video is not rendered onto a surface.
        // height – The surface height in pixels. May be C.LENGTH_UNSET if unknown, or 0 if the video is not rendered onto a surface.
        uzVideoView.onSurfaceSizeChanged = { width: Int, height: Int ->
            tvOnSurfaceSizeChanged.text = "onSurfaceSizeChanged width $width, height $height"
        }

        // Called when the audio session ID changes.
        // Params:
        // audioSessionId – The audio session ID.
        uzVideoView.onAudioSessionIdChanged = { audioSessionId ->
            tvOnAudioSessionIdChanged.text =
                "onAudioSessionIdChanged audioSessionId $audioSessionId"
        }

        // Called when the audio attributes change.
        // Params:
        // audioAttributes – The audio attributes
        uzVideoView.onAudioAttributesChanged = {
            tvOnAudioAttributesChanged.text = "onAudioAttributesChanged ${it.allowedCapturePolicy}"
        }

        // Called when the volume changes.
        // Params:
        // volume – The new volume, with 0 being silence and 1 being unity gain
        uzVideoView.onVolumeChanged = { volume: Float ->
            tvOnVolumeChanged.text = "onVolumeChanged volume $volume"
        }

        // Called when skipping silences is enabled or disabled in the audio stream.
        // Params:
        // eventTime – The event time.
        // skipSilenceEnabled – Whether skipping silences in the audio stream is enabled.
        uzVideoView.onSkipSilenceEnabledChanged = {
            log("onSkipSilenceEnabledChanged $it")
        }

        // Called when there is a change in the Cues.
        // cues is in ascending order of priority. If any of the cue boxes overlap when displayed, the Cue nearer the end of the list should be shown on top.
        // Params:
        // cues – The Cues. May be empty.
        uzVideoView.onCues = {
            log("onCues size ${it.size}")
        }

        // Called when there is Metadata associated with the current playback time.
        // Params:
        // eventTime – The event time.
        // metadata – The metadata
        uzVideoView.onMetadata = {
            log("onMetadata length ${it.length()}")
        }

        // Called when the device information changes.
        uzVideoView.onDeviceInfoChanged = {
            tvOnDeviceInfoChanged.text = "onDeviceInfoChanged ${it.playbackType}"
        }

        // Called when the device volume or mute state changes.
        uzVideoView.onDeviceVolumeChanged = { volume: Int, muted: Boolean ->
            tvOnDeviceVolumeChanged.text = "onDeviceVolumeChanged volume $volume, muted $muted"
        }

        // Called when the timeline has been refreshed.
        // Note that the current window or period index may change as a result of a timeline change. If playback can't continue smoothly because of this timeline change, a separate onPositionDiscontinuity(Player.PositionInfo, Player.PositionInfo, int) callback will be triggered.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // timeline – The latest timeline. Never null, but may be empty.
        // reason – The Player.TimelineChangeReason responsible for this timeline change.
        uzVideoView.onTimelineChanged = { timeline: Timeline, reason: Int ->
            tvOnTimelineChanged.text =
                "onTimelineChanged timeline ${timeline.periodCount}, reason $reason"
        }

        // Called when playback transitions to a media item or starts repeating a media item according to the current repeat mode.
        // Note that this callback is also called when the playlist becomes non-empty or empty as a consequence of a playlist change.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // mediaItem – The MediaItem. May be null if the playlist becomes empty.
        // reason – The reason for the transition.
        uzVideoView.onMediaItemTransition = { mediaItem: MediaItem?, reason: Int ->
            log("onMediaItemTransition mediaItem ${mediaItem?.mediaId}, reason $reason")
        }

        // Called when the available or selected tracks change.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // trackGroups – The available tracks. Never null, but may be of length zero.
        // trackSelections – The selected tracks. Never null, but may contain null elements. A concrete implementation may include null elements if it has a fixed number of renderer components, wishes to report a TrackSelection for each of them, and has one or more renderer components that is not assigned any selected tracks.
        uzVideoView.onTracksChanged =
            { trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray ->
                log("onTracksChanged trackGroups ${trackGroups.length}, trackSelections ${trackSelections.length}")
            }

        // Called when the player starts or stops loading the source.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // isLoading – Whether the source is currently being loaded.
        uzVideoView.onIsLoadingChanged = {
            tvOnIsLoadingChanged.text = "onIsLoadingChanged $it"
        }

        // Called when the value returned from isCommandAvailable(int) changes for at least one Player.Command.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // availableCommands – The available Player.Commands.
        uzVideoView.onAvailableCommandsChanged = {
            tvOnAvailableCommandsChanged.text = "onAvailableCommandsChanged ${it.size()}"
        }

        // Called when the value returned from getPlayWhenReady() changes.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // playWhenReady – Whether playback will proceed when ready.
        // reason – The reason for the change.
        uzVideoView.onPlayWhenReadyChanged = { playWhenReady: Boolean, reason: Int ->
            tvOnPlayWhenReadyChanged.text =
                "onPlayWhenReadyChanged playWhenReady $playWhenReady, reason $reason"
        }

        // Called when the value of isPlaying() changes.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // isPlaying – Whether the player is playing
        uzVideoView.onIsPlayingChanged = {
            tvOnIsPlayingChanged.text = "onIsPlayingChanged $it"
        }

        // Called when the value of getRepeatMode() changes.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // repeatMode – The Player.RepeatMode used for playback.
        uzVideoView.onRepeatModeChanged = {
            log("onRepeatModeChanged $it")
        }

        // Called when the value of getShuffleModeEnabled() changes.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // shuffleModeEnabled – Whether shuffling of windows is enabled.
        uzVideoView.onShuffleModeEnabledChanged = {
            log("onShuffleModeEnabledChanged $it")
        }

        // Called when an error occurs. The playback state will transition to STATE_IDLE immediately after this method is called. The player instance can still be used, and release() must still be called on the player should it no longer be required.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Implementations of Player may pass an instance of a subclass of PlaybackException to this method in order to include more information about the error.
        // Params:
        // error – The error.
        uzVideoView.onPlayerError = {
            log("onPlayerError ${it.errorCode}")
        }

        // Called when the PlaybackException returned by getPlayerError() changes.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Implementations of Player may pass an instance of a subclass of PlaybackException to this method in order to include more information about the error.
        // Params:
        // error – The new error, or null if the error is being cleared.
        uzVideoView.onPlayerErrorChanged = {
            log("onPlayerErrorChanged ${it?.errorCode}")
        }

        // Called when a position discontinuity occurs.
        // A position discontinuity occurs when the playing period changes, the playback position jumps within the period currently being played, or when the playing period has been skipped or removed.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // oldPosition – The position before the discontinuity.
        // newPosition – The position after the discontinuity.
        // reason – The Player.DiscontinuityReason responsible for the discontinuity.
        uzVideoView.onPositionDiscontinuity = { oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int ->
            tvOnPositionDiscontinuity.text =
                "onPositionDiscontinuity oldPosition ${oldPosition.periodIndex}, newPosition ${newPosition.periodIndex}, reason $reason"
        }

        // Called when the current playback parameters change. The playback parameters may change due to a call to setPlaybackParameters(PlaybackParameters), or the player itself may change them (for example, if audio playback switches to passthrough or offload mode, where speed adjustment is no longer possible).
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // playbackParameters – The playback parameters.
        uzVideoView.onPlaybackParametersChanged = {
            tvOnPlaybackParametersChanged.text =
                "onPlaybackParametersChanged speed ${it.speed}, pitcho ${it.pitch}"
        }

        // Called when the value of getSeekBackIncrement() changes.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // seekBackIncrementMs – The seekBack() increment, in milliseconds.
        uzVideoView.onSeekBackIncrementChanged = {
            tvOnSeekBackIncrementChanged.text = "onSeekBackIncrementChanged $it"
        }

        // Called when the value of getSeekForwardIncrement() changes.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // seekForwardIncrementMs – The seekForward() increment, in milliseconds.
        uzVideoView.onSeekForwardIncrementChanged = {
            tvOnSeekForwardIncrementChanged.text = "onSeekForwardIncrementChanged $it"
        }

        // Called when the value of getMaxSeekToPreviousPosition() changes.
        // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
        // Params:
        // maxSeekToPreviousPositionMs – The maximum position for which seekToPrevious() seeks to the previous position, in milliseconds.
        uzVideoView.onMaxSeekToPreviousPositionChanged = {
            log("onMaxSeekToPreviousPositionChanged $it")
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
        btShowSettingsDialogCustom.setOnClickListener {
            val trackSelector = uzVideoView.getTrackSelector()
            if (!isShowingTrackSelectionDialogCustom &&
                trackSelector != null &&
                trackSelector.currentMappedTrackInfo != null &&
                TrackSelectionDialogCustom.willHaveContent(trackSelector)
            ) {
                isShowingTrackSelectionDialogCustom = true
                val trackSelectionDialog =
                    TrackSelectionDialogCustom.createForTrackSelector(trackSelector) {
                        isShowingTrackSelectionDialogCustom = false
                    }
                trackSelectionDialog.show(supportFragmentManager, null)
            }
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
        btFit.setOnClickListener {
            uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
        }
        btFixedWidth.setOnClickListener {
            uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH)
        }
        btFixedHeight.setOnClickListener {
            uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT)
        }
        btFill.setOnClickListener {
            uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)
        }
        btZoom.setOnClickListener {
            uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
        }
    }

    private fun onPlay(link: String) {
        if (link.isEmpty()) {
            toast("Link play is empty")
            return
        }
        if (uzVideoView.isViewCreated()) {
            val uzPlayback = UZPlayback(linkPlay = link)
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
