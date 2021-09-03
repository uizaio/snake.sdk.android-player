package com.uiza.sampleplayer.ui.playerskin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import kotlinx.android.synthetic.main.activity_player_skin.*
import kotlinx.android.synthetic.main.uzplayer_skin_custom.*

class PlayerSkinActivity : AppCompatActivity() {

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_skin)
        setupViews()
    }

    private fun setupViews() {
        uzVideoView.onPlayerViewCreated = {
            uzVideoView.setAlwaysPortraitScreen(true)
            uzVideoView.setPIPModeEnabled(false)
        }
        uzVideoView.onFirstStateReady = {
            uzVideoView.setUseController(true)
        }
        uzVideoView.onSkinChange = { skinId ->
            if (skinId == R.layout.uzplayer_skin_custom) {
                btTest.setOnClickListener {
                    toast("Click")
                }
            }
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
        btSkinDefault.setOnClickListener {
            changeSkin(R.layout.uzplayer_skin_default)
        }
        btSkin1.setOnClickListener {
            changeSkin(R.layout.uzplayer_skin_1)
        }
        btSkin2.setOnClickListener {
            changeSkin(R.layout.uzplayer_skin_2)
        }
        btSkin3.setOnClickListener {
            changeSkin(R.layout.uzplayer_skin_3)
        }
        btSkinCustom.setOnClickListener {
            changeSkin(R.layout.uzplayer_skin_custom)
        }
    }

    private fun changeSkin(skinId: Int) {
        val isSuccess = uzVideoView.changeSkin(skinId)
        if (isSuccess) {
            toast("changeSkin Success")
        } else {
            toast("changeSkin Failed (Player is not ready)")
        }
    }

    private fun onPlay(link: String) {
        if (link.isEmpty()) {
            Toast.makeText(this, "Link play is empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (uzVideoView.isViewCreated()) {
            val uzPlayback = UZPlayback(linkPlay = link)
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
