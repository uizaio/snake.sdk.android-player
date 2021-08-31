package com.uiza.sampleplayer.ui.playeradvanced

import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.interfaces.UZAdPlayerCallback
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.widget.previewseekbar.PreviewView
import kotlinx.android.synthetic.main.activity_player_advanced.*

class PlayerAdvancedActivity : AppCompatActivity() {
    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun log(msg: String) {
        Log.d("loitpp" + javaClass.simpleName, msg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_advanced)
        setupViews()
    }

    private fun setupViews() {
        uzVideoView.onPlayerViewCreated = {
            log("onPlayerViewCreated")
            uzVideoView.isAutoStart = true//default is true
            uzVideoView.setUseController(true)
            uzVideoView.setAutoReplay(true)//default is false
//            uzVideoView.setPlayerControllerAlwaysVisible()//make the controller always show
            uzVideoView.setEnableDoubleTapToSeek(true)//default is false
            logInformation()
        }
        uzVideoView.onIsInitResult = { linkPlay ->
            log("onIsInitResult linkPlay $linkPlay")
        }
        uzVideoView.onStartPreviewTimeBar = { _: PreviewView?, progress: Int ->
            //will be called if you play a video has poster in UZPlayer
            log("onStartPreviewTimeBar progress $progress")
        }
        uzVideoView.onStopPreviewTimeBar = { _: PreviewView?, progress: Int ->
            //will be called if you play a video has poster in UZPlayer
            log("onStopPreviewTimeBar progress $progress")
        }
        uzVideoView.onPreviewTimeBar = { _: PreviewView?, progress: Int, fromUser: Boolean ->
            //will be called if you play a video has poster in UZPlayer
            log("onPreviewTimeBar progress $progress, fromUser $fromUser")
        }
        uzVideoView.onNetworkChange = { isConnected ->
            log("onNetworkChange isConnected $isConnected")
        }
        uzVideoView.onSkinChange = {
            log("onSkinChange")
        }
        uzVideoView.onTimeShiftChange = { timeShiftOn: Boolean ->
            log("onTimeShiftChange timeShiftOn $timeShiftOn")
        }
        uzVideoView.onScreenRotate = { isLandscape: Boolean ->
            log("onScreenRotate isLandscape $isLandscape")
        }
        uzVideoView.onError = {
            toast(it.toString())
        }
        uzVideoView.onDoubleTapFinished = {
            log("onDoubleTapFinished")
        }
        uzVideoView.onDoubleTapProgressDown = { posX: Float, posY: Float ->
            log("onDoubleTapProgressDown $posX $posY")
        }
        uzVideoView.onDoubleTapStarted = { posX: Float, posY: Float ->
            log("onDoubleTapStarted $posX $posY")
        }
        uzVideoView.onDoubleTapProgressUp = { posX: Float, posY: Float ->
            log("onDoubleTapProgressUp $posX $posY")
        }
        uzVideoView.onPlayerStateChanged = { playWhenReady: Boolean, playbackState: Int ->
            when (playbackState) {
                Player.STATE_IDLE -> {
                    log("onPlayerStateChanged playWhenReady $playWhenReady, playbackState STATE_IDLE")
                }
                Player.STATE_BUFFERING -> {
                    log("onPlayerStateChanged playWhenReady $playWhenReady, playbackState STATE_BUFFERING")
                }
                Player.STATE_READY -> {
                    log("onPlayerStateChanged playWhenReady $playWhenReady, playbackState STATE_READY")
                    logInformation()
                }
                Player.STATE_ENDED -> {
                    log("onPlayerStateChanged playWhenReady $playWhenReady, playbackState STATE_ENDED")
                }
            }
        }
        uzVideoView.adPlayerCallback = object : UZAdPlayerCallback {
            override fun onPlay() {
                log("adPlayerCallback onPlay")
            }

            override fun onVolumeChanged(i: Int) {
                log("adPlayerCallback onVolumeChanged $i")
            }

            override fun onPause() {
                log("adPlayerCallback onPause")
            }

            override fun onLoaded() {
                log("adPlayerCallback onLoaded")
            }

            override fun onResume() {
                log("adPlayerCallback onResume")
            }

            override fun onEnded() {
                log("adPlayerCallback onEnded")
                toast("onCurrentWindowDynamic isLIVE ${uzVideoView.isLIVE}")
                logInformation()
            }

            override fun onError() {
                log("adPlayerCallback onError")
            }

            override fun onBuffering() {
                log("adPlayerCallback onBuffering")
            }
        }
        uzVideoView.onCurrentWindowDynamic = { isLIVE ->
            if (isLIVE) {
                toast("onCurrentWindowDynamic isLIVE")
            } else {
                toast("onCurrentWindowDynamic !isLIVE")
            }
        }
        uzVideoView.onSurfaceRedrawNeeded = {
            log("onSurfaceRedrawNeeded")
        }
        uzVideoView.onSurfaceCreated = {
            log("onSurfaceCreated")
        }
        uzVideoView.onSurfaceChanged = { _: SurfaceHolder, format: Int, width: Int, height: Int ->
            log("onSurfaceChanged format $format, width $width, height $height")
        }
        uzVideoView.onSurfaceDestroyed = {
            log("onSurfaceDestroyed")
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
    }

    private fun logInformation() {
        log("isAutoReplay ${uzVideoView.isAutoReplay()}")
        log("isPlayerControllerAlwayVisible ${uzVideoView.isPlayerControllerAlwayVisible()}")
        log("isLandscapeScreen ${uzVideoView.isLandscapeScreen()}")
        log("isAlwaysPortraitScreen ${uzVideoView.isAlwaysPortraitScreen()}")
        log("isShowLayoutDebug ${uzVideoView.isShowLayoutDebug()}")
        log("controllerAutoShow ${uzVideoView.controllerAutoShow}")
        log("heightTimeBar ${uzVideoView.heightTimeBar}")
        log("videoProfileW ${uzVideoView.videoProfileW}")
        log("videoProfileH ${uzVideoView.videoProfileH}")
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
            logInformation()
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

    override fun onBackPressed() {
        if (!uzVideoView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
