package com.uiza.sampleplayer.ui.playertiktok

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sampleplayer.app.UZApplication
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZViewUtils
import kotlinx.android.synthetic.main.activity_player_tiktok.*

class PlayerTiktokActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_tiktok)
        setupViews()
    }

    private fun setupViews() {
        uzVideoView.onPlayerViewCreated = {
            uzVideoView.setAlwaysPortraitScreen(true)
            uzVideoView.setUseController(false)
            uzVideoView.setFreeSize(true)
            uzVideoView.setSize(width = UZViewUtils.screenWidth, height = UZViewUtils.screenHeight)
            uzVideoView.setAutoReplay(true)

            btPlayVOD.performClick()
        }
        btPlayVOD.setOnClickListener {
            etLinkPlay.setText(Constant.LINK_PLAY_VOD_PORTRAIT)
            btPlayLink.performClick()
        }
        btPlayLink.setOnClickListener {
            onPlay(etLinkPlay.text.toString())
        }
    }

    private fun onPlay(link: String) {
        if (link.isEmpty()) {
            Toast.makeText(this, "Link play is empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (uzVideoView.isViewCreated()) {
            val uzPlayback = UZPlayback()
            uzPlayback.poster = UZApplication.thumbnailUrl
            uzPlayback.addLinkPlay(link)
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
