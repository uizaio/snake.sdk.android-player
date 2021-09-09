package com.uiza.sampleplayer.ui.playeradvanced

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.widget.previewseekbar.PreviewView
import kotlinx.android.synthetic.main.activity_player_advanced.*

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
            tvOnSurfaceChanged.text = "onSurfaceChanged format $format, width $width, height $height"
        }
        uzVideoView.onSurfaceDestroyed = {
            tvOnSurfaceDestroyed.text = "onSurfaceDestroyed"
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
