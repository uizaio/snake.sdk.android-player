package com.uiza.sampleplayer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.api.UZApi.getLiveViewers
import com.uiza.sampleplayer.app.Constant
import com.uiza.sampleplayer.app.UZApplication
import com.uiza.sdk.UZPlayer
import com.uiza.sdk.exceptions.UZException
import com.uiza.sdk.interfaces.UZPlayerCallback
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZViewUtils
import com.uiza.sdk.view.UZDragView
import com.uiza.sdk.view.UZPlayerView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_player.*
import java.util.*

class PlayerActivity : AppCompatActivity() {

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
        UZPlayer.setUseWithUZDragView(true)
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_default)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        handler = Handler(Looper.getMainLooper())
        setupViews()
    }

    private fun setupViews() {
        uzDragView.setCallback(object : UZDragView.Callback {
            override fun onOverScroll(state: UZDragView.State, part: UZDragView.Part) {
                uzVideoView.pause()
                uzDragView.disappear()
                showToast("Disappear successfully")
            }

            override fun onEnableRevertMaxSize(isEnableRevertMaxSize: Boolean) {
                updateUIRevertMaxChange(!isEnableRevertMaxSize)
            }

            override fun onAppear(isAppear: Boolean) {
                updateUIRevertMaxChange(uzDragView.isEnableRevertMaxSize)
            }
        })
        uzDragView.setScreenRotate(false)
        uzVideoView.setPlayerCallback(object : UZPlayerCallback {
            override fun playerViewCreated(playerView: UZPlayerView) {
                uzVideoView.playerView.setControllerStateCallback { visible ->
                    uzDragView.setVisibilityChange(visible)
                }
            }

            override fun isInitResult(linkPlay: String) {
                log("LinkPlay $linkPlay")
                uzDragView.setInitResult(true)
                getLiveViewsTimer(true)
            }

            override fun onTimeShiftChange(timeShiftOn: Boolean) {
                runOnUiThread {
                    showToast("TimeShiftOn: $timeShiftOn")
                }
            }

            override fun onScreenRotate(isLandscape: Boolean) {
                if (!isLandscape) {
                    val w = UZViewUtils.getScreenWidth()
                    val h = w * 9 / 16
                    uzVideoView.setFreeSize(false)
                    uzVideoView.setSize(w, h)
                }
                uzDragView.setScreenRotate(isLandscape)
            }

            override fun onError(e: UZException) {
                runOnUiThread {
                    showToast("$e")
                }
            }
        })
        // If link play is livestream, it will auto move to live edge when onResume is called
        uzVideoView.setAutoMoveToLiveEdge(true)
        var playbackInfo: UZPlayback? = null
        if (intent == null) {
            hsvBottom.visibility = View.VISIBLE
            etLinkPlay.visibility = View.VISIBLE
            initPlaylist()
        } else {
            playbackInfo = intent.getParcelableExtra(Constant.EXTRA_PLAYBACK_INFO)
            if (playbackInfo == null) {
                hsvBottom.visibility = View.VISIBLE
                etLinkPlay.visibility = View.VISIBLE
                initPlaylist()
            } else {
                hsvBottom.visibility = View.GONE
                etLinkPlay.visibility = View.GONE
            }
        }
        bt0.setOnClickListener {
            updateView(0)
        }
        bt1.setOnClickListener {
            updateView(1)
        }
        bt2.setOnClickListener {
            updateView(2)
        }
        bt4.setOnClickListener {
            updateView(3)
        }
        bt5.setOnClickListener {
            etLinkPlay.visibility = View.GONE
            btPlay.visibility = View.GONE
            uzVideoView.play(playlist)
        }
        btPlay.setOnClickListener {
            onPlay()
        }
        if (playbackInfo != null) {
            val isInitSuccess = uzVideoView.play(playbackInfo)
            if (!isInitSuccess) {
                showToast("Init failed")
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            updateView(0)
            onPlay()
        }, 1000)
    }

    private fun updateView(index: Int) {
        etLinkPlay.visibility = View.VISIBLE
        btPlay.visibility = View.VISIBLE
        etLinkPlay.setText(UZApplication.urls[index])
        setLastCursorEditText(etLinkPlay)
    }

    private fun initPlaylist() {
        for (url in UZApplication.urls) {
            val playback = UZPlayback()
            playback.addLinkPlay(url)
            playlist.add(playback)
        }
    }

    private fun onPlay() {
        val playback = UZPlayback()
        playback.poster = UZApplication.thumbnailUrl
        playback.addLinkPlay(etLinkPlay.text.toString().trim())
        uzVideoView.play(playback)
    }

    public override fun onDestroy() {
        super.onDestroy()
        uzVideoView.onDestroyView()
        UZPlayer.setUseWithUZDragView(false)
        compositeDisposable.dispose()
        handler = null
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

    private fun updateUIRevertMaxChange(isEnableRevertMaxSize: Boolean) {
        if (isEnableRevertMaxSize && uzDragView.isAppear) {
            //do sth
        }
    }

    private fun getLiveViewsTimer(firstRun: Boolean) {
        val playback = UZPlayer.getCurrentPlayback()
        if (handler != null && playback != null) {
            handler?.postDelayed({
                val d =
                    getLiveViewers(linkPlay = playback.firstLinkPlay,
                        onNext = Consumer { (views) ->
                            uzVideoView.setLiveViewers(views)
                        }, onError = Consumer { t: Throwable? ->
                            log("$t")
                        })
                d?.let {
                    compositeDisposable.add(it)
                }
                getLiveViewsTimer(false)
            }, if (firstRun) 0 else 5000L)
        }
    }

    private fun setLastCursorEditText(editText: EditText) {
        if (editText.text.toString().isNotEmpty()) {
            editText.setSelection(editText.text.length)
        }
    }
}
