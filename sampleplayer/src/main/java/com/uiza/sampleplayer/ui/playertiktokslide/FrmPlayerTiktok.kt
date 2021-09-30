package com.uiza.sampleplayer.ui.playertiktokslide

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
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZViewUtils
import kotlinx.android.synthetic.main.fragment_player_tiktok.*

class FrmPlayerTiktok : Fragment() {

    companion object {

        private const val DATA = "DATA"

        fun newInstance(uzPlayback: UZPlayback): FrmPlayerTiktok {
            val fragment = FrmPlayerTiktok()
            val bundle = Bundle()
            bundle.putSerializable(DATA, uzPlayback)
            fragment.arguments = bundle
            return fragment
        }
    }

    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    private var data: UZPlayback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        data = arguments?.getSerializable(DATA) as UZPlayback?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player_tiktok, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvLinkPlay.text =
            "linkPlay ${data?.linkPlay}\nisPortraitVideo: ${data?.isPortraitVideo}"

        //will be called when player is created
        uzVideoView.onPlayerViewCreated = {
            uzVideoView.setAlwaysPortraitScreen(true)
            uzVideoView.setFreeSize(true)
            uzVideoView.setSize(width = UZViewUtils.screenWidth, height = UZViewUtils.screenHeight)
            uzVideoView.setAutoReplay(true)
            if (data?.isPortraitVideo == true) {
                uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)
            } else {
                uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
            }
            onPlay(data?.linkPlay)
        }

        btFit.setOnClickListener {
            uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
        }
        btFixedWidth.setOnClickListener {
            uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH)
        }
        btFixedHeight.setOnClickListener {
            uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT)
        }
        btFill.setOnClickListener {
            uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)
        }
        btZoom.setOnClickListener {
            uzVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
        }
    }

    private fun onPlay(link: String?) {
        if (link.isNullOrEmpty()) {
            Toast.makeText(context, "Link play is empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (uzVideoView.isViewCreated()) {
            data?.let {
                uzVideoView.play(it)
                uzVideoView.pause()
            }
        }
    }

    override fun onDestroyView() {
        uzVideoView.onDestroyView()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        uzVideoView.onResumeView()
    }

    override fun onPause() {
        super.onPause()
        uzVideoView.onPauseView()
    }
}
