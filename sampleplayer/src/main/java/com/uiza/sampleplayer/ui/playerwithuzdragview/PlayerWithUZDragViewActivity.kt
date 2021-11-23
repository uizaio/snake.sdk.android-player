package com.uiza.sampleplayer.ui.playerwithuzdragview

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZViewUtils
import com.uiza.sdk.view.UZDragView
import com.uiza.sdk.view.UZPlayerView
import kotlinx.android.synthetic.main.activity_player_with_uz_drag_view.*

class PlayerWithUZDragViewActivity : AppCompatActivity() {

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_with_uz_drag_view)
        setupViews()
    }

    private fun setupViews() {
        uzDragView.setCallback(object : UZDragView.Callback {
            override fun onOverScroll(state: UZDragView.State?, part: UZDragView.Part?) {
                log("onOverScroll")
                uzVideoView.pause()
                uzDragView.disappear()
                showToast("Disappear successfully")
            }

            override fun onEnableRevertMaxSize(isEnableRevertMaxSize: Boolean) {
            }

            override fun onAppear(isAppear: Boolean) {
            }
        })
        uzDragView.setScreenRotate(false)

        // will be called when player is created
        uzVideoView.onPlayerViewCreated = {
            uzVideoView.uzPlayerView?.setControllerStateCallback(object :
                    UZPlayerView.ControllerStateCallback {
                    override fun onVisibilityChange(visible: Boolean) {
                        uzDragView.setVisibilityChange(visible)
                    }
                })
            uzVideoView.setUseUZDragView(true)
        }

        // result when init resources
        uzVideoView.onIsInitResult = {
            uzDragView.setInitResult(true)
        }

        // the first time the player has playbackState == Player.STATE_READY
        uzVideoView.onFirstStateReady = {
            uzVideoView.setUseController(true)
        }
        uzVideoView.onScreenRotate = { isLandscape ->
            if (!isLandscape) {
                val w = UZViewUtils.screenWidth
                val h = w * 9 / 16
                uzVideoView.setFreeSize(false)
                uzVideoView.setSize(w, h)
            }
            uzDragView.setScreenRotate(isLandscape)
        }
        uzVideoView.setAutoMoveToLiveEdge(true)
        hsvBottom.visibility = View.VISIBLE
        etLinkPlay.visibility = View.VISIBLE
        bt0.setOnClickListener {
            updateView(index = 0)
        }
        bt1.setOnClickListener {
            updateView(index = 1)
        }
        bt2.setOnClickListener {
            updateView(index = 2)
        }
        bt4.setOnClickListener {
            updateView(index = 3)
        }
    }

    private fun updateView(index: Int) {
        etLinkPlay.visibility = View.VISIBLE
        etLinkPlay.setText(Constant.urls[index])
        setLastCursorEditText(etLinkPlay)
        onPlay()
    }

    private fun onPlay() {
        val linkPlay = etLinkPlay.text.toString().trim()
        if (linkPlay.isEmpty()) {
            Toast.makeText(this, "Empty link play", Toast.LENGTH_SHORT).show()
        } else {
            val uzPlayback = UZPlayback(linkPlay = linkPlay)
            uzVideoView.play(uzPlayback)
        }
    }

    public override fun onDestroy() {
        uzVideoView.onDestroyView()
        super.onDestroy()
    }

    public override fun onResume() {
        super.onResume()
        if (uzDragView.isAppear) {
            uzVideoView.onResumeView()
        }
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

    private fun setLastCursorEditText(editText: EditText) {
        if (editText.text.toString().isNotEmpty()) {
            editText.setSelection(editText.text.length)
        }
    }
}
