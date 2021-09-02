package com.uiza.sampleplayer.ui.playerlist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import kotlinx.android.synthetic.main.activity_player_list.*
import java.util.*

class PlayerListActivity : AppCompatActivity() {
    private val list: MutableList<UZPlayback> = ArrayList()
    private var playerListAdapter: PlayerListAdapter? = null
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_list)

        setupViews()
        prepareData()
        onPlay()//auto play first item
    }

    private fun setupViews() {
        uzVideoView.onPlayerViewCreated = {
            uzVideoView.setAlwaysPortraitScreen(true)
            uzVideoView.setPIPModeEnabled(false)
        }
        uzVideoView.onFirstStateReady = {
            uzVideoView.setUseController(true)
        }
        playerListAdapter = PlayerListAdapter(list)
        playerListAdapter?.onClickItem = { index: Int, _: UZPlayback ->
            this.index = index
            onPlay()
        }
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = playerListAdapter
    }

    private fun onPlay() {
        if (uzVideoView.isViewCreated()) {
            uzVideoView.play(list[index])
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

    private fun prepareData() {
        list.add(
            UZPlayback(
                linkPlay = Constant.LINK_PLAY_VOD,
                name = "VOD",
                isPortraitVideo = false
            )
        )
        list.add(
            UZPlayback(
                linkPlay = Constant.LINK_PLAY_VOD_2,
                name = "LIVE",
                isPortraitVideo = false
            )
        )
        list.add(
            UZPlayback(
                linkPlay = Constant.LINK_PLAY_VOD_PORTRAIT,
                name = "Portrait",
                isPortraitVideo = true
            )
        )
        list.add(
            UZPlayback(
                linkPlay = Constant.LINK_PLAY_VOD,
                name = "Ad",
                isPortraitVideo = false,
                urlIMAAd = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator="
            )
        )
        list.add(
            UZPlayback(
                linkPlay = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd",
                name = "This is name of video",
                isPortraitVideo = false,
                poster = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg",
            )
        )

        playerListAdapter?.notifyDataSetChanged()
    }
}
