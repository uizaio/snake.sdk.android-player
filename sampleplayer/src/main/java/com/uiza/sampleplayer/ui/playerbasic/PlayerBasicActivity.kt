package com.uiza.sampleplayer.ui.playerbasic

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sampleplayer.app.UZApplication
import com.uiza.sdk.UZPlayer
import com.uiza.sdk.exceptions.UZException
import com.uiza.sdk.interfaces.UZPlayerCallback
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.view.UZPlayerView
import kotlinx.android.synthetic.main.activity_player_basic.*

class PlayerBasicActivity : AppCompatActivity() {

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_default)
        setContentView(R.layout.activity_player_basic)
        setupViews()
    }

    private fun setupViews() {
        uzVideoView.setPlayerCallback(object : UZPlayerCallback {
            override fun playerViewCreated(playerView: UZPlayerView) {
                uzVideoView.isAlwaysPortraitScreen = true
                uzVideoView.setPIPModeEnabled(false)
                uzVideoView.setUseController(true)
            }

            override fun isInitResult(linkPlay: String) {
            }

            override fun onTimeShiftChange(timeShiftOn: Boolean) {
            }

            override fun onScreenRotate(isLandscape: Boolean) {
            }

            override fun onError(e: UZException) {
                e.printStackTrace()
            }
        })
        btPlayVOD.setOnClickListener {
            etLinkPlay.setText(Constant.LINK_PLAY_VOD)
            btPlayLink.performClick()
        }
        btPlayLive.setOnClickListener {
            etLinkPlay.setText(Constant.LINK_PLAY_LIVE)
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
        if (uzVideoView.isViewCreated) {
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
