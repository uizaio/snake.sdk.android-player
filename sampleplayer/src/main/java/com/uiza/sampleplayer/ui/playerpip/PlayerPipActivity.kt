package com.uiza.sampleplayer.ui.playerpip

import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZViewUtils
import kotlinx.android.synthetic.main.activity_player_pip.*

class PlayerPipActivity : AppCompatActivity() {

    private var isPortraitVideo = false

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setContentView(R.layout.activity_player_pip)

        setupViews()
    }

    private fun setupViews() {
        uzVideoView.setPIPModeEnabled(true)
        uzVideoView.onFirstStateReady = {
            uzVideoView.setUseController(true)
        }
        uzVideoView.onScreenRotate = { isLandscape ->
            if (!uzVideoView.isInPipMode()) {
                if (isLandscape) {
                    sv.visibility = View.GONE
                } else {
                    sv.visibility = View.VISIBLE
                }
            }
        }

        // If link play is livestream, it will auto move to live edge when onResume is called
        uzVideoView.setAutoMoveToLiveEdge(true)

        btnVOD.setOnClickListener {
            updateSize(false)
            etLinkPlay.setText(Constant.LINK_PLAY_VOD)
            btnPlay.performClick()
        }
        btnPortrait.setOnClickListener {
            updateSize(true)
            etLinkPlay.setText(Constant.LINK_PLAY_VOD_PORTRAIT)
            btnPlay.performClick()
        }
        btnLive.setOnClickListener {
            updateSize(false)
            etLinkPlay.setText(Constant.LINK_PLAY_LIVE)
            btnPlay.performClick()
        }
        btnPlay.setOnClickListener { onPlay() }
    }

    private fun updateSize(isPortraitVideo: Boolean) {
        this.isPortraitVideo = isPortraitVideo
        if (this.isPortraitVideo) {
            uzVideoView.setFreeSize(true)
            uzVideoView.setSize(width = UZViewUtils.screenWidth, height = UZViewUtils.screenHeight)
        } else {
            uzVideoView.setFreeSize(false)
        }
    }

    private fun onPlay() {
        val linkPlay = etLinkPlay.text.toString().trim()
        if (linkPlay.isEmpty()) {
            Toast.makeText(this, "Linkplay cannot be null or empty", Toast.LENGTH_SHORT).show()
            return
        }
        val playback = UZPlayback(
            linkPlay = linkPlay
        )
        uzVideoView.play(playback)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        uzVideoView.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        uzVideoView.onRestoreInstanceState(savedInstanceState)
    }

    public override fun onDestroy() {
        super.onDestroy()
        uzVideoView.onDestroyView()
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

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            sv.visibility = View.GONE
        } else {
            sv.visibility = View.VISIBLE
        }
        uzVideoView.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (!isInPictureInPictureMode) {
            if (this.isPortraitVideo) {
                uzVideoView.post {
                    uzVideoView.setFreeSize(true)
                    uzVideoView.setSize(
                        width = UZViewUtils.screenWidth,
                        height = UZViewUtils.screenHeight
                    )
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        try {
            if (!uzVideoView.isLandscapeScreen()) {
                uzVideoView.enterPIPMode()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
