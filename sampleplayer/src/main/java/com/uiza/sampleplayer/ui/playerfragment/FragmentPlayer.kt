package com.uiza.sampleplayer.ui.playerfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import kotlinx.android.synthetic.main.activity_player_pip.*

class FragmentPlayer : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        uzVideoView.setPIPModeEnabled(true)
        uzVideoView.onFirstStateReady = {
            uzVideoView.setUseController(true)
        }

        uzVideoView.setAutoMoveToLiveEdge(true)

        btnVOD.setOnClickListener {
            etLinkPlay.setText(Constant.LINK_PLAY_VOD)
            btnPlay.performClick()
        }
        btnLive.setOnClickListener {
            etLinkPlay.setText(Constant.LINK_PLAY_LIVE)
            btnPlay.performClick()
        }
        btnPlay.setOnClickListener { onPlay() }

        //on back pressed
        activity?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!uzVideoView.onBackPressed()) {
                    activity?.finish()
                }
            }
        })
    }

    private fun onPlay() {
        val linkPlay = etLinkPlay.text.toString().trim()
        if (linkPlay.isEmpty()) {
            Toast.makeText(context, "Linkplay cannot be null or empty", Toast.LENGTH_SHORT).show()
            return
        }
        val playback = UZPlayback(
            linkPlay = linkPlay
        )
        uzVideoView.play(playback)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        uzVideoView.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            uzVideoView.onRestoreInstanceState(savedInstanceState)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uzVideoView.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        uzVideoView.onResumeView()
    }

    override fun onPause() {
        super.onPause()
        uzVideoView.onPauseView()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        uzVideoView.onPictureInPictureModeChanged(isInPictureInPictureMode, null)
    }

    fun onUserLeaveHint() {
        try {
            if (!uzVideoView.isLandscapeScreen()) {
                uzVideoView.enterPIPMode()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
