package com.uiza.sampleplayer.ui.playerpreviewseekbar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sdk.models.UZPlayback
import kotlinx.android.synthetic.main.activity_player_preview_seekbar.*

class PlayerPreviewSeekbarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_preview_seekbar)
        setupViews()
    }

    private fun setupViews() {
        uzVideoView.isAutoRetryPlayerIfError = true
        // will be called when player is created
        uzVideoView.onPlayerViewCreated = {
            uzVideoView.setAlwaysPortraitScreen(true)
            uzVideoView.setPIPModeEnabled(false)
        }

        // the first time the player has playbackState == Player.STATE_READY
        uzVideoView.onFirstStateReady = {
            uzVideoView.setUseController(true)
        }
        btPlayLink.setOnClickListener {
            onPlay()
        }
    }

    private fun onPlay() {
        if (uzVideoView.isViewCreated()) {
            val uzPlayback = UZPlayback(
                linkPlay = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd",
                poster = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg",
                name = "This is name of video"
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

    override fun onBackPressed() {
        if (!uzVideoView.onBackPressed()) {
            super.onBackPressed()
        }
    }
}
