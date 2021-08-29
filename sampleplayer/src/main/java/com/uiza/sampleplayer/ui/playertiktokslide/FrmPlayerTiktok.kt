package com.uiza.sampleplayer.ui.playertiktokslide

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.UZApplication
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZViewUtils
import kotlinx.android.synthetic.main.fragment_player_tiktok.*

class FrmPlayerTiktok : Fragment() {

    companion object {

        private const val LINK_PLAY = "LINK_PLAY"

        fun newInstance(linkPlay: String): FrmPlayerTiktok {
            val fragment = FrmPlayerTiktok()
            val bundle = Bundle()
            bundle.putString(LINK_PLAY, linkPlay)
            fragment.arguments = bundle
            return fragment
        }
    }

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    var linkPlay: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        linkPlay = arguments?.getString(LINK_PLAY) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player_tiktok, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvLinkPlay.text = linkPlay

        if (uzVideoView.isViewCreated()) {
            uzVideoView.setAlwaysPortraitScreen(true)
            uzVideoView.setUseController(false)
            uzVideoView.setFreeSize(true)
            uzVideoView.setSize(width = UZViewUtils.screenWidth, height = UZViewUtils.screenHeight)
            uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)
            uzVideoView.setAutoReplay(true)
            onPlay(linkPlay)
        }
    }

    private fun onPlay(link: String?) {
        if (link.isNullOrEmpty()) {
            Toast.makeText(context, "Link play is empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (uzVideoView.isViewCreated()) {
            val uzPlayback = UZPlayback()
            uzPlayback.poster = UZApplication.thumbnailUrl
            uzPlayback.addLinkPlay(link)
            uzVideoView.play(uzPlayback)
            uzVideoView.pause()
        }
    }

    override fun onDestroyView() {
        uzVideoView.onDestroyView()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        log("onResume linkPlay $linkPlay")
        uzVideoView.onResumeView()
    }

    override fun onPause() {
        super.onPause()
        uzVideoView.onPauseView()
    }
}
