package com.uiza.sampleplayer.ui.playerpipinscrollview

import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.uiza.sampleplayer.R
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZViewUtils
import kotlinx.android.synthetic.main.activity_player_pip_sv.*

class PlayerPipScrollViewActivity : AppCompatActivity() {
    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setContentView(R.layout.activity_player_pip_sv)
        setupViews()
    }

    private fun setupViews() {
        uzVideoView.isAutoRetryPlayerIfError = true
        uzVideoView.setPIPModeEnabled(true)
        uzVideoView.setAutoReplay(true)
        uzVideoView.onFirstStateReady = {
            uzVideoView.setUseController(true)
            uzVideoView.setAutoMoveToLiveEdge(true)
        }
        uzVideoView.onPlayerViewCreated = {
            updateSize()
            onPlay()
        }
        uzVideoView.onScreenRotate = { isLandscape ->
            if (!isLandscape) {
                updateSize()
            }
        }
        keyboardHeightProvider = KeyboardHeightProvider(this)
        keyboardHeightProvider?.addKeyboardListener(object :
                KeyboardHeightProvider.KeyboardListener {
                override fun onHeightChanged(height: Int) {
                    if (height == 0) {
                        layoutKeyboardFake.isVisible = false
                    } else {
                        if (layoutKeyboardFake.layoutParams.height != height) {
                            layoutKeyboardFake.layoutParams.height = height
                            layoutKeyboardFake.requestLayout()
                        }
                        layoutKeyboardFake.isVisible = true
                    }
                }
            })
    }

    private fun updateSize() {
        uzVideoView.setFreeSize(true)
        uzVideoView.setSize(width = UZViewUtils.screenWidth, height = UZViewUtils.screenHeight)
    }

    private fun onPlay() {
        val playback = UZPlayback(
            linkPlay = "https://assets.mixkit.co/videos/preview/mixkit-man-runs-past-ground-level-shot-32809-large.mp4"
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
        keyboardHeightProvider?.onResume()
    }

    public override fun onPause() {
        super.onPause()
        uzVideoView.onPauseView()
        keyboardHeightProvider?.onPause()
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
        layoutKeyboardFake.isVisible = false
        if (isInPictureInPictureMode) {
            svChat.isVisible = false
            etChat.isVisible = false
        } else {
            svChat.isVisible = true
            etChat.isVisible = true
        }
        uzVideoView.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        if (!isInPictureInPictureMode) {
            uzVideoView.post {
                uzVideoView.setFreeSize(true)
                uzVideoView.setSize(
                    width = UZViewUtils.screenWidth,
                    height = UZViewUtils.screenHeight
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPIPMode()
    }

    private fun enterPIPMode() {
        try {
            if (!uzVideoView.isLandscapeScreen()) {
                uzVideoView.enterPIPMode()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
