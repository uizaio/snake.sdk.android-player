package com.uiza.sampleplayer.ui.playerrecyclerview

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import kotlinx.android.synthetic.main.activity_player_recycler_view.*
import java.util.* // ktlint-disable no-wildcard-imports

class PlayerRecyclerViewActivity : AppCompatActivity() {
    private val list: MutableList<ItemRv> = ArrayList()
    private var recyclerAdapter: RecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_recycler_view)

        setupViews()
        prepareData()
    }

    private fun setupViews() {
        recyclerAdapter = RecyclerAdapter(list)
        recyclerAdapter?.onClickItem = { index: Int, _: ItemRv ->
            onPlay(index)
        }
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = recyclerAdapter
    }

    private fun onPlay(index: Int) {
        list.forEach { item ->
            item.isFocussed = false
        }
        list[index].isFocussed = true
        notifyDataSetChanged()
    }

    private fun prepareData() {
        list.add(
            ItemRv(
                UZPlayback(
                    linkPlay = Constant.LINK_PLAY_VOD,
                    name = "VOD",
                    isPortraitVideo = false
                )
            )
        )
        list.add(
            ItemRv(
                UZPlayback(
                    linkPlay = Constant.LINK_PLAY_LIVE,
                    name = "LIVE",
                    isPortraitVideo = false
                )
            )
        )
        list.add(
            ItemRv(
                UZPlayback(
                    linkPlay = Constant.LINK_PLAY_VOD_PORTRAIT,
                    name = "Portrait",
                    isPortraitVideo = true
                )
            )
        )
        list.add(
            ItemRv(
                UZPlayback(
                    linkPlay = Constant.LINK_PLAY_VOD,
                    name = "Ad",
                    isPortraitVideo = false,
                    urlIMAAd = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator="
                )
            )
        )
        list.add(
            ItemRv(
                UZPlayback(
                    linkPlay = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd",
                    name = "This is name of video",
                    isPortraitVideo = false,
                    poster = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg",
                )
            )
        )

        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyDataSetChanged() {
        recyclerAdapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        recyclerAdapter?.onDestroy()
        super.onDestroy()
    }
}
