package com.uiza.sampleplayer.ui.playertiktokslidehorizontal

import android.annotation.SuppressLint
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
import com.uiza.sampleplayer.model.DataVideo
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZViewUtils
import kotlinx.android.synthetic.main.fragment_player_tiktok.*

class FrmPlayerTiktok : Fragment() {

    companion object {

        private const val DATA = "DATA"

        fun newInstance(dataVideo: DataVideo): FrmPlayerTiktok {
            val fragment = FrmPlayerTiktok()
            val bundle = Bundle()
            bundle.putSerializable(DATA, dataVideo)
            fragment.arguments = bundle
            return fragment
        }
    }

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    private var dataVideo: DataVideo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataVideo = arguments?.getSerializable(DATA) as DataVideo?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player_tiktok, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        log("onViewCreated $dataVideo")

        tvLinkPlay.text =
            "linkPlay ${dataVideo?.linkPlay}\nisPortraitVideo: ${dataVideo?.isPortraitVideo}"

        uzVideoView.onPlayerViewCreated = {
            uzVideoView.setAlwaysPortraitScreen(true)
            uzVideoView.setUseController(false)
            uzVideoView.setFreeSize(true)
            uzVideoView.setSize(width = UZViewUtils.screenWidth, height = UZViewUtils.screenHeight)
            uzVideoView.setAutoReplay(true)
            if (dataVideo?.isPortraitVideo == true) {
                uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)
            } else {
                uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
            }
            onPlay(dataVideo?.linkPlay)
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
            log("onPlay")
        }
    }

    override fun onDestroyView() {
        uzVideoView.onDestroyView()
        log("onDestroyView")
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        log("onResume")
        uzVideoView.onResumeView()
    }

    override fun onPause() {
        super.onPause()
        log("onPause")
        uzVideoView.onPauseView()
    }
}
