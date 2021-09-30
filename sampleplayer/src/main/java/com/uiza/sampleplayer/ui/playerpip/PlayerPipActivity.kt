package com.uiza.sampleplayer.ui.playerpip

import android.app.PendingIntent
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZViewUtils
import kotlinx.android.synthetic.main.activity_player_pip.*


class PlayerPipActivity : AppCompatActivity() {
    companion object {
        private const val BROADCAST_ACTION_1 = "BROADCAST_ACTION_1"
        private const val BROADCAST_ACTION_2 = "BROADCAST_ACTION_2"
        private const val BROADCAST_ACTION_3 = "BROADCAST_ACTION_3"
        private const val REQUEST_CODE = 1221
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private var isPortraitVideo = false
    private var listRemoteAction: ArrayList<RemoteAction>? = null
    private var receiver: BroadcastReceiver? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setContentView(R.layout.activity_player_pip)

        setupViews()
    }

    private fun setupActionsDefault() {
        this.listRemoteAction = null
        uzVideoView.listRemoteAction = listRemoteAction
        enterPIPMode()
    }

    private fun setupActionsNone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            listRemoteAction = ArrayList()

            val actionIntent1 = Intent(BROADCAST_ACTION_1)
            val pendingIntent1 = PendingIntent.getBroadcast(this, REQUEST_CODE, actionIntent1, 0)
            val icon1 = Icon.createWithResource(this, R.drawable.ic_transparent)
            val remoteAction1 = RemoteAction(icon1, "Info", "Some info", pendingIntent1)
            remoteAction1.isEnabled = false
            listRemoteAction?.add(remoteAction1)

            uzVideoView.listRemoteAction = listRemoteAction
            enterPIPMode()
        } else {
            toast("Not supported")
        }
    }

    private fun setupActionsCustom() {
        //You only customer the PIP controller if android SDK >= Android O
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            listRemoteAction = ArrayList()

            val actionIntent1 = Intent(BROADCAST_ACTION_1)
            val pendingIntent1 = PendingIntent.getBroadcast(this, REQUEST_CODE, actionIntent1, 0)
            val icon1 = Icon.createWithResource(this, android.R.drawable.ic_dialog_info)
            val remoteAction1 = RemoteAction(icon1, "Info", "Some info", pendingIntent1)
            listRemoteAction?.add(remoteAction1)

            val actionIntent2 = Intent(BROADCAST_ACTION_2)
            val pendingIntent2 = PendingIntent.getBroadcast(this, REQUEST_CODE, actionIntent2, 0)
            val icon2 = Icon.createWithResource(this, android.R.drawable.ic_btn_speak_now)
            val remoteAction2 = RemoteAction(icon2, "Speak", "Speak info", pendingIntent2)
            listRemoteAction?.add(remoteAction2)

            val actionIntent3 = Intent(BROADCAST_ACTION_3)
            val pendingIntent3 = PendingIntent.getBroadcast(this, REQUEST_CODE, actionIntent3, 0)
            val icon3 = Icon.createWithResource(this, android.R.drawable.ic_dialog_map)
            val remoteAction3 = RemoteAction(icon3, "Map", "Map info", pendingIntent3)
            listRemoteAction?.add(remoteAction3)

            uzVideoView.listRemoteAction = listRemoteAction
            enterPIPMode()
        } else {
            toast("Not supported")
        }
    }

    private fun setupViews() {
        uzVideoView.setPIPModeEnabled(true)

        //the first time the player has playbackState == Player.STATE_READY
        uzVideoView.onFirstStateReady = {
            uzVideoView.setUseController(true)
        }

        //will be called when screen is rotated
        uzVideoView.onScreenRotate = { isLandscape ->
            if (!uzVideoView.isInPipMode()) {
                if (isLandscape) {
                    sv.visibility = View.GONE
                } else {
                    sv.visibility = View.VISIBLE
                }
            }
        }

        // If link play is livestream, it will auto move to live edge when onResume is called
        uzVideoView.setAutoMoveToLiveEdge(true)

        btnVOD.setOnClickListener {
            updateSize(false)
            etLinkPlay.setText(Constant.LINK_PLAY_VOD)
            btnPlay.performClick()
        }
        btnPortrait.setOnClickListener {
            updateSize(true)
            etLinkPlay.setText(Constant.LINK_PLAY_VOD_PORTRAIT)
            btnPlay.performClick()
        }
        btnLive.setOnClickListener {
            updateSize(false)
            etLinkPlay.setText(Constant.LINK_PLAY_LIVE)
            btnPlay.performClick()
        }
        btnPlay.setOnClickListener { onPlay() }

        btnPipControllerDefault.setOnClickListener {
            setupActionsDefault()
        }
        btnPipControllerNone.setOnClickListener {
            setupActionsNone()
        }
        btnPipControllerCustom.setOnClickListener {
            setupActionsCustom()
        }
    }

    private fun updateSize(isPortraitVideo: Boolean) {
        this.isPortraitVideo = isPortraitVideo
        if (this.isPortraitVideo) {
            uzVideoView.setFreeSize(true)
            uzVideoView.setSize(width = UZViewUtils.screenWidth, height = UZViewUtils.screenHeight)
        } else {
            uzVideoView.setFreeSize(false)
        }
    }

    private fun onPlay() {
        val linkPlay = etLinkPlay.text.toString().trim()
        if (linkPlay.isEmpty()) {
            toast("Linkplay cannot be null or empty")
            return
        }
        val playback = UZPlayback(
            linkPlay = linkPlay
        )
        uzVideoView.play(playback)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        uzVideoView.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        uzVideoView.onRestoreInstanceState(savedInstanceState)
    }

    public override fun onDestroy() {
        super.onDestroy()
        uzVideoView.onDestroyView()
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

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            sv.visibility = View.GONE
        } else {
            sv.visibility = View.VISIBLE
        }
        uzVideoView.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        //update UI
        if (!isInPictureInPictureMode) {
            if (this.isPortraitVideo) {
                uzVideoView.post {
                    uzVideoView.setFreeSize(true)
                    uzVideoView.setSize(
                        width = UZViewUtils.screenWidth,
                        height = UZViewUtils.screenHeight
                    )
                }
            }
        }

        //handle actions
        if (isInPictureInPictureMode) {
            val filter = IntentFilter()
            filter.addAction(BROADCAST_ACTION_1)
            filter.addAction(BROADCAST_ACTION_2)
            filter.addAction(BROADCAST_ACTION_3)
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    when (intent.action) {
                        BROADCAST_ACTION_1 -> {
                            toast("BROADCAST_ACTION_1")
                        }
                        BROADCAST_ACTION_2 -> {
                            toast("BROADCAST_ACTION_2")
                        }
                        BROADCAST_ACTION_3 -> {
                            toast("BROADCAST_ACTION_3")
                        }
                    }
                }
            }
            registerReceiver(receiver, filter)
        } else {
            unregisterReceiver(receiver)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPIPMode()
    }

    private fun enterPIPMode() {
        try {
            if (!uzVideoView.isLandscapeScreen()) {
                uzVideoView.enterPIPMode()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
