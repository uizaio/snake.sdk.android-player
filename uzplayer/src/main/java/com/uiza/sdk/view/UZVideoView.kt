package com.uiza.sdk.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.Pair
import android.util.Rational
import android.view.* // ktlint-disable no-wildcard-imports
import android.widget.* // ktlint-disable no-wildcard-imports
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.ezralazuardy.orb.Orb
import com.ezralazuardy.orb.OrbHelper
import com.ezralazuardy.orb.OrbListener
import com.google.android.exoplayer2.* // ktlint-disable no-wildcard-imports
import com.google.android.exoplayer2.MediaItem.AdsConfiguration
import com.google.android.exoplayer2.MediaItem.Builder
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.decoder.DecoderReuseEvaluation
import com.google.android.exoplayer2.drm.FrameworkMediaDrm
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.ErrorMessageProvider
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoSize
import com.uiza.sdk.R
import com.uiza.sdk.UZPlayer
import com.uiza.sdk.dialog.speed.Callback
import com.uiza.sdk.dialog.speed.Speed
import com.uiza.sdk.dialog.speed.UZSpeedDialog
import com.uiza.sdk.exceptions.ErrorConstant
import com.uiza.sdk.exceptions.ErrorUtils
import com.uiza.sdk.exceptions.UZException
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.observers.SensorOrientationChangeNotifier
import com.uiza.sdk.utils.* // ktlint-disable no-wildcard-imports
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
import kotlinx.android.synthetic.main.layout_stream_stopped.view.*
import kotlinx.android.synthetic.main.layout_uz_ima_video_core.view.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class UZVideoView :
    RelativeLayout,
    SensorOrientationChangeNotifier.Listener,
    View.OnClickListener {

    companion object {
        private const val HYPHEN = "-"
        private const val FAST_FORWARD_REWIND_INTERVAL = 10000L // 10s
        private const val DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS = 5000 // 5s
        private const val ARG_VIDEO_POSITION = "ARG_VIDEO_POSITION"
    }

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
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
    var finishAndRemoveTaskIsInPipMode = true
    var onFinishAndRemoveTaskIsInPipMode: (() -> Unit)? = null
    private var isPIPModeEnabled = false
    private var isUSeControllerRestorePip = false
    private var positionPIPPlayer = 0L
    private var isAutoReplay = false
    private var isFreeSize = false
    private var isPlayerControllerAlwayVisible = false
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
    var isAutoRetryPlayerIfError = false

    var listRemoteAction: List<RemoteAction>? = null

    // will be called when player is created
    var onPlayerViewCreated: ((playerView: UZPlayerView) -> Unit)? = null

    // result when init resources
    var onIsInitResult: ((linkPlay: String) -> Unit)? = null

    // will be called when you change skin of player
    var onSkinChange: ((skinId: Int) -> Unit)? = null

    // will be called when screen is rotated
    var onScreenRotate: ((isLandscape: Boolean) -> Unit)? = null

    // will be called when the player has any UZException
    var onError: ((e: UZException) -> Unit)? = null

    // will be called when player state is changed
    var onPlayerStateChanged: ((playbackState: Int) -> Unit)? = null

    // the first time the player has playbackState == Player.STATE_READY
    var onFirstStateReady: (() -> Unit)? = null

    // will be called if you play a video has poster in player
    var onStartPreviewTimeBar: ((previewView: PreviewView?, progress: Int) -> Unit)? = null

    // will be called if you play a video has poster in player
    var onStopPreviewTimeBar: ((previewView: PreviewView?, progress: Int) -> Unit)? = null

    // will be called if you play a video has poster in player
    var onPreviewTimeBar: ((previewView: PreviewView?, progress: Int, fromUser: Boolean) -> Unit)? =
        null

    // will be called if your network is changed
    var onNetworkChange: ((isConnected: Boolean) -> Unit)? = null

    // help you know the current video is Live content or not
    var onCurrentWindowDynamic: ((isLIVE: Boolean) -> Unit)? = null

    // listener for surface view
    var onSurfaceRedrawNeeded: ((holder: SurfaceHolder) -> Unit)? = null
    var onSurfaceCreated: ((holder: SurfaceHolder) -> Unit)? = null
    var onSurfaceChanged: ((holder: SurfaceHolder, format: Int, width: Int, height: Int) -> Unit)? =
        null
    var onSurfaceDestroyed: ((holder: SurfaceHolder) -> Unit)? = null

    // listener for double tap on the player
    var onDoubleTapFinished: (() -> Unit)? = null
    var onDoubleTapProgressDown: ((posX: Float, posY: Float) -> Unit)? = null
    var onDoubleTapStarted: ((posX: Float, posY: Float) -> Unit)? = null
    var onDoubleTapProgressUp: ((posX: Float, posY: Float) -> Unit)? = null

    // Called when the shuffle mode changed.
    // Params:
    // eventTime – The event time.
    // shuffleModeEnabled – Whether the shuffle mode is enabled.
    var onShuffleModeChanged: ((eventTime: AnalyticsListener.EventTime, shuffleModeEnabled: Boolean) -> Unit)? =
        null

    // Called when a media source started loading data.
    // Params:
    // eventTime – The event time.
    // loadEventInfo – The LoadEventInfo defining the load event.
    // mediaLoadData – The MediaLoadData defining the data being loaded.
    var onLoadStarted: (
        (
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) -> Unit
    )? = null

    // Called when a media source completed loading data.
    // Params:
    // eventTime – The event time.
    // loadEventInfo – The LoadEventInfo defining the load event.
    // mediaLoadData – The MediaLoadData defining the data being loaded.
    var onLoadCompleted: (
        (
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) -> Unit
    )? = null

    // Called when a media source canceled loading data.
    // Params:
    // eventTime – The event time.
    // loadEventInfo – The LoadEventInfo defining the load event.
    // mediaLoadData – The MediaLoadData defining the data being loaded.
    var onLoadCanceled: (
        (
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData
        ) -> Unit
    )? = null

    // Called when a media source loading error occurred.
    // This method being called does not indicate that playback has failed, or that it will fail. The player may be able to recover from the error. Hence applications should not implement this method to display a user visible error or initiate an application level retry. Player.Listener.onPlayerError is the appropriate place to implement such behavior. This method is called to provide the application with an opportunity to log the error if it wishes to do so.
    // Params:
    // eventTime – The event time.
    // loadEventInfo – The LoadEventInfo defining the load event.
    // mediaLoadData – The MediaLoadData defining the data being loaded.
    // error – The load error.
    // wasCanceled – Whether the load was canceled as a result of the error
    var onLoadError: (
        (
            eventTime: AnalyticsListener.EventTime,
            loadEventInfo: LoadEventInfo,
            mediaLoadData: MediaLoadData,
            error: IOException,
            wasCanceled: Boolean
        ) -> Unit
    )? = null

    // Called when the downstream format sent to the renderers changed.
    // Params:
    // eventTime – The event time.
    // mediaLoadData – The MediaLoadData defining the newly selected media data
    var onDownstreamFormatChanged: (
        (
            eventTime: AnalyticsListener.EventTime,
            mediaLoadData: MediaLoadData
        ) -> Unit
    )? = null

    // Called when data is removed from the back of a media buffer, typically so that it can be re-buffered in a different format.
    // Params:
    // eventTime – The event time.
    // mediaLoadData – The MediaLoadData defining the media being discarded.
    var onUpstreamDiscarded: (
        (
            eventTime: AnalyticsListener.EventTime,
            mediaLoadData: MediaLoadData
        ) -> Unit
    )? = null

    // Called when the bandwidth estimate for the current data source has been updated.
    // Params:
    // eventTime – The event time.
    // totalLoadTimeMs – The total time spend loading this update is based on, in milliseconds.
    // totalBytesLoaded – The total bytes loaded this update is based on.
    // bitrateEstimate – The bandwidth estimate, in bits per second
    var onBandwidthEstimate: (
        (
            eventTime: AnalyticsListener.EventTime,
            totalLoadTimeMs: Int,
            totalBytesLoaded: Long,
            bitrateEstimate: Long
        ) -> Unit
    )? = null

    // Called when an audio renderer is enabled.
    // Params:
    // eventTime – The event time.
    // decoderCounters – DecoderCounters that will be updated by the renderer for as long as it remains enabled.
    var onAudioEnabled: (
        (
            eventTime: AnalyticsListener.EventTime,
            decoderCounters: DecoderCounters
        ) -> Unit
    )? = null

    // Called when an audio renderer creates a decoder.
    // Params:
    // eventTime – The event time.
    // decoderName – The decoder that was created.
    // initializedTimestampMs – SystemClock.elapsedRealtime() when initialization finished.
    // initializationDurationMs – The time taken to initialize the decoder in milliseconds.
    var onAudioDecoderInitialized: (
        (
            eventTime: AnalyticsListener.EventTime,
            decoderName: String,
            initializedTimestampMs: Long,
            initializationDurationMs: Long
        ) -> Unit
    )? = null

    // Called when the format of the media being consumed by an audio renderer changes.
    // Params:
    // eventTime – The event time.
    // format – The new format.
    // decoderReuseEvaluation – The result of the evaluation to determine whether an existing decoder instance can be reused for the new format, or null if the renderer did not have a decoder.
    var onAudioInputFormatChanged: (
        (
            eventTime: AnalyticsListener.EventTime,
            format: Format,
            decoderReuseEvaluation: DecoderReuseEvaluation?
        ) -> Unit
    )? = null

    // Called when the audio position has increased for the first time since the last pause or position reset.
    // Params:
    // eventTime – The event time.
    // playoutStartSystemTimeMs – The approximate derived System.currentTimeMillis() at which playout started.
    var onAudioPositionAdvancing: (
        (
            eventTime: AnalyticsListener.EventTime,
            playoutStartSystemTimeMs: Long
        ) -> Unit
    )? = null

    // Called when an audio underrun occurs.
    // Params:
    // eventTime – The event time.
    // bufferSize – The size of the audio output buffer, in bytes.
    // bufferSizeMs – The size of the audio output buffer, in milliseconds, if it contains PCM encoded audio. C.TIME_UNSET if the output buffer contains non-PCM encoded audio.
    // elapsedSinceLastFeedMs – The time since audio was last written to the output buffer.
    var onAudioUnderrun: (
        (
            eventTime: AnalyticsListener.EventTime,
            bufferSize: Int,
            bufferSizeMs: Long,
            elapsedSinceLastFeedMs: Long
        ) -> Unit
    )? = null

    // Called when an audio renderer releases a decoder.
    // Params:
    // eventTime – The event time.
    // decoderName – The decoder that was released.
    var onAudioDecoderReleased: (
        (
            eventTime: AnalyticsListener.EventTime,
            decoderName: String
        ) -> Unit
    )? = null

    // Called when an audio renderer is disabled.
    // Params:
    // eventTime – The event time.
    // decoderCounters – DecoderCounters that were updated by the renderer
    var onAudioDisabled: (
        (
            eventTime: AnalyticsListener.EventTime,
            decoderCounters: DecoderCounters
        ) -> Unit
    )? = null

    // Called when AudioSink has encountered an error.
    // This method being called does not indicate that playback has failed, or that it will fail. The player may be able to recover from the error. Hence applications should not implement this method to display a user visible error or initiate an application level retry. Player.Listener.onPlayerError is the appropriate place to implement such behavior. This method is called to provide the application with an opportunity to log the error if it wishes to do so.
    // Params:
    // eventTime – The event time.
    // audioSinkError – The error that occurred. Typically an AudioSink.InitializationException, a AudioSink.WriteException, or an AudioSink.UnexpectedDiscontinuityException
    var onAudioSinkError: (
        (
            eventTime: AnalyticsListener.EventTime,
            audioSinkError: java.lang.Exception
        ) -> Unit
    )? = null

    // Called when an audio decoder encounters an error.
    // This method being called does not indicate that playback has failed, or that it will fail. The player may be able to recover from the error. Hence applications should not implement this method to display a user visible error or initiate an application level retry. Player.Listener.onPlayerError is the appropriate place to implement such behavior. This method is called to provide the application with an opportunity to log the error if it wishes to do so.
    // Params:
    // eventTime – The event time.
    // audioCodecError – The error. Typically a MediaCodec.CodecException if the renderer uses MediaCodec, or a DecoderException if the renderer uses a software decoder
    var onAudioCodecError: (
        (
            eventTime: AnalyticsListener.EventTime,
            audioCodecError: java.lang.Exception
        ) -> Unit
    )? = null

    // Called when a video renderer is enabled.
    // Params:
    // eventTime – The event time.
    // decoderCounters – DecoderCounters that will be updated by the renderer for as long as it remains enabled.
    var onVideoEnabled: (
        (
            eventTime: AnalyticsListener.EventTime,
            decoderCounters: DecoderCounters
        ) -> Unit
    )? = null

    // Called when a video renderer creates a decoder.
    // Params:
    // eventTime – The event time.
    // decoderName – The decoder that was created.
    // initializedTimestampMs – SystemClock.elapsedRealtime() when initialization finished.
    // initializationDurationMs – The time taken to initialize the decoder in milliseconds.
    var onVideoDecoderInitialized: (
        (
            eventTime: AnalyticsListener.EventTime,
            decoderName: String,
            initializedTimestampMs: Long,
            initializationDurationMs: Long
        ) -> Unit
    )? = null

    // Called when the format of the media being consumed by a video renderer changes.
    // Params:
    // eventTime – The event time.
    // format – The new format.
    // decoderReuseEvaluation – The result of the evaluation to determine whether an existing decoder instance can be reused for the new format, or null if the renderer did not have a decoder.
    var onVideoInputFormatChanged: (
        (
            eventTime: AnalyticsListener.EventTime,
            format: Format,
            decoderReuseEvaluation: DecoderReuseEvaluation?
        ) -> Unit
    )? = null

    // Called after video frames have been dropped.
    // Params:
    // eventTime – The event time.
    // droppedFrames – The number of dropped frames since the last call to this method.
    // elapsedMs – The duration in milliseconds over which the frames were dropped. This duration is timed from when the renderer was started or from when dropped frames were last reported (whichever was more recent), and not from when the first of the reported drops occurred.
    var onDroppedVideoFrames: (
        (
            eventTime: AnalyticsListener.EventTime,
            droppedFrames: Int,
            elapsedMs: Long
        ) -> Unit
    )? = null

    // Called when a video renderer releases a decoder.
    // Params:
    // eventTime – The event time.
    // decoderName – The decoder that was released.
    var onVideoDecoderReleased: (
        (
            eventTime: AnalyticsListener.EventTime,
            decoderName: String
        ) -> Unit
    )? = null

    // Called when a video renderer is disabled.
    // Params:
    // eventTime – The event time.
    // decoderCounters – DecoderCounters that were updated by the renderer.
    var onVideoDisabled: (
        (
            eventTime: AnalyticsListener.EventTime,
            decoderCounters: DecoderCounters
        ) -> Unit
    )? = null

    // Called when there is an update to the video frame processing offset reported by a video renderer.
    // The processing offset for a video frame is the difference between the time at which the frame became available to render, and the time at which it was scheduled to be rendered. A positive value indicates the frame became available early enough, whereas a negative value indicates that the frame wasn't available until after the time at which it should have been rendered.
    // Params:
    // eventTime – The event time.
    // totalProcessingOffsetUs – The sum of the video frame processing offsets for frames rendered since the last call to this method.
    // frameCount – The number to samples included in totalProcessingOffsetUs.
    var onVideoFrameProcessingOffset: (
        (
            eventTime: AnalyticsListener.EventTime,
            totalProcessingOffsetUs: Long,
            frameCount: Int
        ) -> Unit
    )? = null

    // Called when the Player is released.
    // Params:
    // eventTime – The event time.
    var onPlayerReleased: ((eventTime: AnalyticsListener.EventTime) -> Unit)? = null
    var onProgressChange: ((currentPosition: Long, duration: Long, isPlayingAd: Boolean?) -> Unit)? =
        null

    // Called each time there's a change in the size of the video being rendered.
    // Params:
    // videoSize – The new size of the video.
    var onVideoSizeChanged: ((videoSize: VideoSize) -> Unit)? = null

    // Called each time there's a change in the size of the surface onto which the video is being rendered.
    // Params:
    // width – The surface width in pixels. May be C.LENGTH_UNSET if unknown, or 0 if the video is not rendered onto a surface.
    // height – The surface height in pixels. May be C.LENGTH_UNSET if unknown, or 0 if the video is not rendered onto a surface.
    var onSurfaceSizeChanged: ((width: Int, height: Int) -> Unit)? = null
    var onRenderedFirstFrame: (() -> Unit)? = null

    // Called when the audio session ID changes.
    // Params:
    // audioSessionId – The audio session ID.
    var onAudioSessionIdChanged: ((audioSessionId: Int) -> Unit)? = null

    // Called when the audio attributes change.
    // Params:
    // audioAttributes – The audio attributes
    var onAudioAttributesChanged: ((audioAttributes: AudioAttributes) -> Unit)? = null

    // Called when the volume changes.
    // Params:
    // volume – The new volume, with 0 being silence and 1 being unity gain
    var onVolumeChanged: ((volume: Float) -> Unit)? = null

    // Called when skipping silences is enabled or disabled in the audio stream.
    // Params:
    // eventTime – The event time.
    // skipSilenceEnabled – Whether skipping silences in the audio stream is enabled.
    var onSkipSilenceEnabledChanged: ((skipSilenceEnabled: Boolean) -> Unit)? = null

    // Called when there is a change in the Cues.
    // cues is in ascending order of priority. If any of the cue boxes overlap when displayed, the Cue nearer the end of the list should be shown on top.
    // Params:
    // cues – The Cues. May be empty.
    var onCues: ((cues: MutableList<Cue>) -> Unit)? = null

    // Called when there is Metadata associated with the current playback time.
    // Params:
    // eventTime – The event time.
    // metadata – The metadata
    var onMetadata: ((metadata: com.google.android.exoplayer2.metadata.Metadata) -> Unit)? = null

    // Called when the device information changes.
    var onDeviceInfoChanged: ((deviceInfo: DeviceInfo) -> Unit)? = null

    // Called when the device volume or mute state changes.
    var onDeviceVolumeChanged: ((volume: Int, muted: Boolean) -> Unit)? = null

    // Called when the timeline has been refreshed.
    // Note that the current window or period index may change as a result of a timeline change. If playback can't continue smoothly because of this timeline change, a separate onPositionDiscontinuity(Player.PositionInfo, Player.PositionInfo, int) callback will be triggered.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // timeline – The latest timeline. Never null, but may be empty.
    // reason – The Player.TimelineChangeReason responsible for this timeline change.
    var onTimelineChanged: ((timeline: Timeline, reason: Int) -> Unit)? = null

    // Called when playback transitions to a media item or starts repeating a media item according to the current repeat mode.
    // Note that this callback is also called when the playlist becomes non-empty or empty as a consequence of a playlist change.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // mediaItem – The MediaItem. May be null if the playlist becomes empty.
    // reason – The reason for the transition.
    var onMediaItemTransition: ((mediaItem: MediaItem?, reason: Int) -> Unit)? = null

    //    Called when the available or selected tracks change.
//    onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
//    Params:
//    tracksInfo – The available tracks information. Never null, but may be of length zero.
    var onTracksInfoChanged: ((tracksInfo: TracksInfo) -> Unit)? =
        null

    // Called when the player starts or stops loading the source.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // isLoading – Whether the source is currently being loaded.
    var onIsLoadingChanged: ((isLoading: Boolean) -> Unit)? = null

    // Called when the value returned from isCommandAvailable(int) changes for at least one Player.Command.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // availableCommands – The available Player.Commands.
    var onAvailableCommandsChanged: ((availableCommands: Player.Commands) -> Unit)? = null

    // Called when the value returned from getPlayWhenReady() changes.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // playWhenReady – Whether playback will proceed when ready.
    // reason – The reason for the change.
    var onPlayWhenReadyChanged: ((playWhenReady: Boolean, reason: Int) -> Unit)? = null

    // Called when the value of isPlaying() changes.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // isPlaying – Whether the player is playing
    var onIsPlayingChanged: ((isPlaying: Boolean) -> Unit)? = null

    // Called when the value of getRepeatMode() changes.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // repeatMode – The Player.RepeatMode used for playback.
    var onRepeatModeChanged: ((repeatMode: Int) -> Unit)? = null

    // Called when the value of getShuffleModeEnabled() changes.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // shuffleModeEnabled – Whether shuffling of windows is enabled.
    var onShuffleModeEnabledChanged: ((shuffleModeEnabled: Boolean) -> Unit)? = null

    // Called when an error occurs. The playback state will transition to STATE_IDLE immediately after this method is called. The player instance can still be used, and release() must still be called on the player should it no longer be required.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Implementations of Player may pass an instance of a subclass of PlaybackException to this method in order to include more information about the error.
    // Params:
    // error – The error.
    var onPlayerError: ((error: PlaybackException) -> Unit)? = null

    // Called when the PlaybackException returned by getPlayerError() changes.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Implementations of Player may pass an instance of a subclass of PlaybackException to this method in order to include more information about the error.
    // Params:
    // error – The new error, or null if the error is being cleared.
    var onPlayerErrorChanged: ((error: PlaybackException?) -> Unit)? = null

    // Called when a position discontinuity occurs.
    // A position discontinuity occurs when the playing period changes, the playback position jumps within the period currently being played, or when the playing period has been skipped or removed.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // oldPosition – The position before the discontinuity.
    // newPosition – The position after the discontinuity.
    // reason – The Player.DiscontinuityReason responsible for the discontinuity.
    var onPositionDiscontinuity: ((oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) -> Unit)? =
        null

    // Called when the current playback parameters change. The playback parameters may change due to a call to setPlaybackParameters(PlaybackParameters), or the player itself may change them (for example, if audio playback switches to passthrough or offload mode, where speed adjustment is no longer possible).
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // playbackParameters – The playback parameters.
    var onPlaybackParametersChanged: ((playbackParameters: PlaybackParameters) -> Unit)? = null

    // Called when the value of getSeekBackIncrement() changes.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // seekBackIncrementMs – The seekBack() increment, in milliseconds.
    var onSeekBackIncrementChanged: ((seekBackIncrementMs: Long) -> Unit)? = null

    // Called when the value of getSeekForwardIncrement() changes.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // seekForwardIncrementMs – The seekForward() increment, in milliseconds.
    var onSeekForwardIncrementChanged: ((seekForwardIncrementMs: Long) -> Unit)? = null

    // Called when the value of getMaxSeekToPreviousPosition() changes.
    // onEvents(Player, Player.Events) will also be called to report this event along with other events that happen in the same Looper message queue iteration.
    // Params:
    // maxSeekToPreviousPositionMs – The maximum position for which seekToPrevious() seeks to the previous position, in milliseconds.
    var onMaxSeekToPreviousPositionChanged: ((maxSeekToPreviousPositionMs: Long) -> Unit)? = null

    private var orb: Orb? = null
    private val compositeDisposable = CompositeDisposable()

    var player: ExoPlayer? = null
    private var dataSourceFactory: DataSource.Factory? = null
    private var mediaItems: List<MediaItem>? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
    private var lastSeenTracksInfo: TracksInfo? = null
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
                                addLayoutOverlay()
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
                                        if (finishAndRemoveTaskIsInPipMode) {
                                            if (context is Activity) {
                                                (context as Activity).finishAndRemoveTask()
                                            }
                                        } else {
                                            onFinishAndRemoveTaskIsInPipMode?.invoke()
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
                false // khong cho dung controller cho den khi isFirstStateReady == true
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
                btPipUZ?.isVisible = false
            }

            setEventForViews()
            updateUIButton(currentPosition)
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

    // return pixel
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
        if (!isPlayerControllerShowing) {
            showController()
        }
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
        updateUIIbRewIconDependOnProgress(
            currentMls = currentPosition,
            isCalledFromUZTimeBarEvent = false
        )
        resume()

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
        // khong cho dung controller cho den khi isFirstStateReady == true
        uzPlayerView?.useController = false
        releasePlayerManager()
        showProgress()
        initDataSource()
        initPlayerManager()
        initializePlayer()
        onIsInitResult?.invoke(linkPlay)
        return true
    }

    fun resume() {
        if (isPlayingAd() == true) {
            return
        }
        player?.playWhenReady = true
        updateUIIbRewIconDependOnProgress(
            currentMls = currentPosition,
            isCalledFromUZTimeBarEvent = false
        )
    }

    fun pause() {
        if (isPlayingAd() == true) {
            return
        }
        player?.playWhenReady = false
        updateUIIbRewIconDependOnProgress(
            currentMls = currentPosition,
            isCalledFromUZTimeBarEvent = false
        )
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
            if (finishAndRemoveTaskIsInPipMode) {
                if (context is Activity) {
                    (context as Activity).finishAndRemoveTask()
                }
            } else {
                onFinishAndRemoveTaskIsInPipMode?.invoke()
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

    // If link play is livestream, it will auto move to live edge when onResume is called
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
            startWindow = it.currentMediaItemIndex
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
                btPipUZ?.isVisible = false
            } else { // portrait screen
                if (!isInPipMode) {
                    UZViewUtils.hideSystemUi(pv)
                }
                isLandscape = false
                btFullscreenUZ?.let {
                    UZViewUtils.setUIFullScreenIcon(imageButton = it, isFullScreen = false)
                }
                if (isPIPEnable) {
                    btPipUZ?.isVisible = true
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

    var controllerShowTimeoutMs: Int = DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS
        get() = uzPlayerView?.controllerShowTimeoutMs ?: -1
        set(controllerShowTimeoutMs) {
            field = controllerShowTimeoutMs
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
        if (isAutoReplay) {
            replay()
        }
        hideProgress()
        showController()
    }

    fun replay() {
        if (isPlayingAd() == true) {
            return
        }
        seekTo(0)
        updateUIIbRewIconDependOnProgress(
            currentMls = currentPosition,
            isCalledFromUZTimeBarEvent = false
        )
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
                }
            )
            UZViewUtils.showDialog(uzDlgSpeed)
        }
    }

    val isLIVE: Boolean
        get() {
            return player?.isCurrentMediaItemLive ?: false
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
            layoutRootView.removeView(uzPlayerView)
            uzPlayerView = null

            uzPlayerView = inflater.inflate(skinId, null) as UZPlayerView?
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT)
            layoutParams.addRule(CENTER_IN_PARENT, TRUE)
            uzPlayerView?.let {
                it.layoutParams = layoutParams
                it.requestFocus()

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
                                        if (finishAndRemoveTaskIsInPipMode) {
                                            if (context is Activity) {
                                                (context as Activity).finishAndRemoveTask()
                                            }
                                        } else {
                                            onFinishAndRemoveTaskIsInPipMode?.invoke()
                                        }
                                    }
                                }
                                onSurfaceDestroyed?.invoke(holder)
                            }
                        })
                }

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
            checkToSetUpResource()
            updateUISizeThumbnailTimeBar()
            updateUIButtonVolume()

            uzPlayerView?.player = player
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
                setTextPosition(currentMls)
            }
        }
        if (isLIVE) {
            btReplayUZ?.isVisible = false
            if (isPlaying) {
                btPlayUZ?.isVisible = false
                btPauseUZ?.isVisible = true
            } else {
                btPlayUZ?.isVisible = true
                btPauseUZ?.isVisible = false
            }
        } else {
            if (isOnPlayerEnded) {
                btReplayUZ?.isVisible = true
                btPlayUZ?.isVisible = false
                btPauseUZ?.isVisible = false

                btRewUZ?.setSrcDrawableEnabled()
                btFfwdUZ?.setSrcDrawableDisabled()
            } else {
                if (isPlayerControllerShowing) {
                    updateUIButton(currentMls)
                }
            }
        }
    }

    private fun updateUIButton(currentMls: Long) {
        if (isPlaying) {
            btPlayUZ?.isVisible = false
            btReplayUZ?.isVisible = false
            btPauseUZ?.isVisible = true
        } else {
            btPlayUZ?.isVisible = true
            btReplayUZ?.isVisible = false
            btPauseUZ?.isVisible = false
        }

        btRewUZ?.let { r ->
            btFfwdUZ?.let { f ->
                if (currentMls <= 1000L) {
                    r.setSrcDrawableDisabled()
                    f.setSrcDrawableEnabled()
                } else {
                    r.setSrcDrawableEnabled()
                    f.setSrcDrawableEnabled()
                }
            }
        }
    }

    private fun updateUIButtonVolume() {
        if (volume == 0f) {
            btVolumeUZ?.setSrcDrawableDisabledCanTouch()
        } else {
            btVolumeUZ?.setSrcDrawableEnabled()
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
            // only hide button pip if device is TV
            btPipUZ?.isVisible = false
        }
        onCurrentWindowDynamic?.invoke(isLIVE)
//        log("updateUIDependOnLiveStream isLIVE $isLIVE")
        if (isLIVE) {
            btSpeedUZ?.isVisible = false
            tvDurationUZ?.isVisible = false
            tvPositionUZ?.isVisible = false
            btRewUZ?.isVisible = false
            btFfwdUZ?.isVisible = false
        } else {
            btSpeedUZ?.isVisible = true
            tvDurationUZ?.isVisible = true
            tvPositionUZ?.isVisible = true
            btRewUZ?.isVisible = true
            btFfwdUZ?.isVisible = true
        }
        tvTitleUZ?.text = uzPlayback?.name ?: ""
        if (UZAppUtils.isTV(context)) {
            btFullscreenUZ?.isVisible = false
        }
    }

    private var isShowingTrackSelectionDialog = false

    @SuppressLint("InflateParams")
    fun showSettingsDialog() {
        if (!isShowingTrackSelectionDialog &&
            trackSelector != null &&
            trackSelector?.currentMappedTrackInfo != null &&
            TrackSelectionDialog.willHaveContent(trackSelector)
        ) {
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
                updateUIButtonVolume()
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

            override fun onTracksInfoChanged(tracksInfo: TracksInfo) {
                super.onTracksInfoChanged(tracksInfo)
                if (tracksInfo === lastSeenTracksInfo) {
                    return
                }
                if (!tracksInfo.isTypeSupportedOrEmpty(C.TRACK_TYPE_VIDEO)) {
//                    throw Exception("Media includes video tracks, but none are playable by this device")
                    val exception = UZException(
                        code = ErrorConstant.ERR_CODE_27,
                        message = ErrorConstant.ERR_27
                    )
                    onError?.invoke(exception)
                }
                if (!tracksInfo.isTypeSupportedOrEmpty(C.TRACK_TYPE_AUDIO)) {
//                    throw Exception("Media includes audio tracks, but none are playable by this device")
                    val exception = UZException(
                        code = ErrorConstant.ERR_CODE_28,
                        message = ErrorConstant.ERR_28
                    )
                    onError?.invoke(exception)
                }
                lastSeenTracksInfo = tracksInfo
                onTracksInfoChanged?.invoke(tracksInfo)
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
                        isOnPlayerEnded = false
                        showProgress()
                    }
                    Player.STATE_IDLE -> {
//                        log("onPlaybackStateChanged STATE_IDLE")
                        isOnPlayerEnded = false
                        showProgress()

                        if (isAutoRetryPlayerIfError) {
                            layoutOverlayUZVideo?.isVisible = true
                            if (uzPlayback == null || uzPlayback?.linkPlay.isNullOrEmpty()) {
                                tvStreamStopped?.isVisible = false
                                tvClickRetry?.isVisible = false
                            } else {
                                tvStreamStopped?.isVisible = true
                                tvClickRetry?.isVisible = true
                            }
                            retryVideo()
                        }
                    }
                    Player.STATE_ENDED -> {
//                        log("onPlaybackStateChanged STATE_ENDED")
                        isOnPlayerEnded = true
                        onPlayerEnded()
                    }
                    Player.STATE_READY -> {
                        isOnPlayerEnded = false
                        hideProgress()
                        updateTvDuration()
                        if (player?.playWhenReady == true) {
                            timeBarUZ?.hidePreview()
                        }
                        if (context is Activity) {
                            (context as Activity).setResult(Activity.RESULT_OK)
                        }

                        if (isPlayingAd() == true) {
                            // do nothing
                        } else {
                            if (!isFirstStateReady) {
                                setFirstStateReady(true)
                                updateUIDependOnLiveStream()
                            }
                        }

                        if (isAutoRetryPlayerIfError) {
                            layoutOverlayUZVideo?.isVisible = false
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
//                log("onIsPlayingChanged isPlaying $isPlaying")
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

            override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Long) {
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
            return Observable.interval(0, 1000, TimeUnit.MILLISECONDS)
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
        // 270 land trai
        // 0 portrait duoi
        // 90 land phai
        // 180 portrait tren
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
                lastSeenTracksInfo = TracksInfo.EMPTY
                trackSelector?.let {
                    player = ExoPlayer.Builder(context)
                        .setRenderersFactory(renderersFactory)
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
            builder.setUri(Uri.parse(uzp.linkPlay))
            if (uzp.urlIMAAd.isNullOrEmpty()) {
                // do nothing
            } else {
                builder.setAdsConfiguration(
                    AdsConfiguration.Builder(Uri.parse(uzp.urlIMAAd)).build()
                )
            }
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
                    Assertions.checkNotNull(mediaItem.localConfiguration).drmConfiguration
                if (drmConfiguration != null) {
                    if (Util.SDK_INT < 18) {
                        throw Exception("DRM content not supported on API levels below 18")
                    } else if (!FrameworkMediaDrm.isCryptoSchemeSupported(drmConfiguration.scheme)) {
                        throw Exception("This device does not support the required DRM scheme")
                    }
                }
                hasAds = hasAds or (mediaItem.localConfiguration?.adsConfiguration != null)
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

    fun getTrackSelector(): DefaultTrackSelector? {
        return this.trackSelector
    }

    fun isControllerHideOnTouch(): Boolean? {
        return uzPlayerView?.controllerHideOnTouch
    }

    fun setControllerHideOnTouch(controllerHideOnTouch: Boolean) {
        uzPlayerView?.controllerHideOnTouch = controllerHideOnTouch
    }

    @SuppressLint("InflateParams")
    private fun addLayoutOverlay() {
        if (layoutOverlayUZVideo != null) {
//            log("onPlaybackStateChanged addLayoutOverlay return")
            return
        }
//        log("onPlaybackStateChanged addLayoutOverlay")
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_stream_stopped, null)

        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            this.height
        )
        params.addRule(ALIGN_PARENT_LEFT, TRUE)
        params.addRule(ALIGN_PARENT_TOP, TRUE)
        this.addView(view, params)

        layoutOverlayUZVideo.setOnClickListener {
            retryVideo()
        }

        if (isAutoRetryPlayerIfError) {
            if (uzPlayback == null || uzPlayback?.linkPlay.isNullOrEmpty()) {
                tvStreamStopped?.isVisible = false
                tvClickRetry?.isVisible = false
            } else {
                tvStreamStopped?.isVisible = true
                tvClickRetry?.isVisible = true
            }
        } else {
            layoutOverlayUZVideo?.isVisible = false
        }
    }

    private val handlerRetry = Handler(Looper.getMainLooper())
    private fun retryVideo() {
        if (!isAutoRetryPlayerIfError) {
            return
        }
        handlerRetry.removeCallbacksAndMessages(null)
        handlerRetry.postDelayed(
            {
                uzPlayback?.let {
                    play(it)
                }
            },
            2000
        )
    }

    fun getCurrentBitmap(): Bitmap? {
        val v = uzPlayerView?.videoSurfaceView
        if (v is TextureView) {
            return v.bitmap
        }
        return null
    }
}
