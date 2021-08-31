package com.uiza.sampleplayer.ui.playeradvanced

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.widget.previewseekbar.PreviewView
import kotlinx.android.synthetic.main.activity_player_advanced.*

class PlayerAdvancedActivity : AppCompatActivity() {

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
            uzVideoView.isAutoStart = true//default is true
            uzVideoView.setUseController(true)
            uzVideoView.setAutoReplay(true)//default is false
//            uzVideoView.setPlayerControllerAlwaysVisible()//make the controller always show

            logInformation()
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
    }

    private fun onPlay(link: String) {
        if (link.isEmpty()) {
            Toast.makeText(this, "Link play is empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (uzVideoView.isViewCreated()) {
            val uzPlayback = UZPlayback(linkPlay = link)
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
