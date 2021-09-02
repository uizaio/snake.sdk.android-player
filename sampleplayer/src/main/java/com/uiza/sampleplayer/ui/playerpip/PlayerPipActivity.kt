package com.uiza.sampleplayer.ui.playerpip

import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sdk.models.UZPlayback
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_player_pip.*

class PlayerPipActivity : AppCompatActivity() {
    private var disposables: CompositeDisposable? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setContentView(R.layout.activity_player_pip)

        setupViews()
    }

    private fun setupViews() {
        uzVideoView.setPIPModeEnabled(true)
        uzVideoView.onScreenRotate = { isLandscape: Boolean ->
//            if (!isLandscape) {
//                val w = screenWidth
//                val h = w * 9 / 16
//                uzVideoView.setFreeSize(false)
//                uzVideoView.setSize(w, h)
//            }
        }
        // If link play is livestream, it will auto move to live edge when onResume is called
        uzVideoView.setAutoMoveToLiveEdge(true)

        btnPlay.setOnClickListener { onPlay() }
        disposables = CompositeDisposable()
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
        disposables?.dispose()
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
        uzVideoView.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
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
