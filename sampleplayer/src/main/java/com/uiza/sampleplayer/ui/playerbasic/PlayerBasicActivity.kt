package com.uiza.sampleplayer.ui.playerbasic

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.UZApplication
import com.uiza.sdk.UZPlayer
import com.uiza.sdk.exceptions.UZException
import com.uiza.sdk.interfaces.UZPlayerCallback
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZViewUtils
import com.uiza.sdk.view.UZPlayerView
import kotlinx.android.synthetic.main.activity_player_basic.*

class PlayerBasicActivity : AppCompatActivity() {

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_default)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_basic)
        setupViews()
    }

    private fun setupViews() {
        uzVideoView.setPlayerCallback(object : UZPlayerCallback {
            override fun playerViewCreated(playerView: UZPlayerView) {
                onPlay()
            }

            override fun isInitResult(linkPlay: String) {
                log("LinkPlay $linkPlay")
            }

            override fun onTimeShiftChange(timeShiftOn: Boolean) {
            }

            override fun onScreenRotate(isLandscape: Boolean) {
                if (!isLandscape) {
                    val w = UZViewUtils.screenWidth
                    val h = w * 9 / 16
                    uzVideoView.setFreeSize(false)
                    uzVideoView.setSize(w, h)
                }
            }

            override fun onError(e: UZException) {
                e.printStackTrace()
            }
        })

    }

    private fun onPlay() {
        val uzPlayback = UZPlayback()
        uzPlayback.poster = UZApplication.thumbnailUrl
        uzPlayback.addLinkPlay("https://hls.ted.com/talks/2639.m3u8?preroll=Thousands")
        uzVideoView.play(uzPlayback)
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
