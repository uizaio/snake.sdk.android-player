package com.uiza.sampleplayer.ui.playerbasic

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sampleplayer.app.UZApplication
import com.uiza.sdk.models.UZPlayback
import kotlinx.android.synthetic.main.activity_player_basic.*

class PlayerBasicActivity : AppCompatActivity() {

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_basic)
        setupViews()
    }

    private fun setupViews() {
        uzVideoView.onPlayerViewCreated = {
            uzVideoView.urlIMAAd = getString(R.string.ad_tag_url_test)
            uzVideoView.setAlwaysPortraitScreen(true)
            uzVideoView.setPIPModeEnabled(false)
            uzVideoView.setUseController(true)
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
