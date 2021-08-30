package com.uiza.sampleplayer.ui.playerwithuzdragview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.UZApplication
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZViewUtils
import com.uiza.sdk.view.UZDragView
import com.uiza.sdk.view.UZPlayerView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_player_with_uz_drag_view.*
import java.util.*

class PlayerWithUZDragViewActivity : AppCompatActivity() {

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    private var playlist = ArrayList<UZPlayback>()
    private var handler: Handler? = null
    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_with_uz_drag_view)
        handler = Handler(Looper.getMainLooper())
        setupViews()
    }

    private fun setupViews() {
        uzDragView.setCallback(object : UZDragView.Callback {
            override fun onOverScroll(state: UZDragView.State?, part: UZDragView.Part?) {
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
        uzVideoView.onPlayerViewCreated = {
            uzVideoView.playerView?.setControllerStateCallback(object :
                UZPlayerView.ControllerStateCallback {
                override fun onVisibilityChange(visible: Boolean) {
                    uzDragView.setVisibilityChange(visible)
                }
            })
            uzVideoView.setUseUZDragView(true)
        }
        uzVideoView.onIsInitResult = {
            uzDragView.setInitResult(true)
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
        // If link play is livestream, it will auto move to live edge when onResume is called
        uzVideoView.setAutoMoveToLiveEdge(true)
        val playbackInfo: UZPlayback? = null
        if (intent == null) {
            hsvBottom.visibility = View.VISIBLE
            etLinkPlay.visibility = View.VISIBLE
            initPlaylist()
        } else {
            hsvBottom.visibility = View.VISIBLE
            etLinkPlay.visibility = View.VISIBLE
            initPlaylist()
        }
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
        if (playbackInfo != null) {
            val isInitSuccess = uzVideoView.play(playbackInfo)
            if (!isInitSuccess) {
                showToast("Init failed")
            }
        }
    }

    private fun updateView(index: Int) {
        etLinkPlay.visibility = View.VISIBLE
        etLinkPlay.setText(UZApplication.urls[index])
        setLastCursorEditText(etLinkPlay)
        onPlay()
    }

    private fun initPlaylist() {
        for (url in UZApplication.urls) {
            val playback = UZPlayback()
            playback.linkPlay = url
            playlist.add(playback)
        }
    }

    private fun onPlay() {
        val uzPlayback = UZPlayback()
        uzPlayback.poster = UZApplication.thumbnailUrl
        uzPlayback.linkPlay = etLinkPlay.text.toString().trim()
        uzVideoView.play(uzPlayback)
    }

    public override fun onDestroy() {
        uzVideoView.onDestroyView()
        compositeDisposable.dispose()
        handler = null
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
