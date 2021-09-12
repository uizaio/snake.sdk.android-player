package com.uiza.sampleplayer.ui.playertiktokslide

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sdk.models.UZPlayback
import kotlinx.android.synthetic.main.activity_player_advanced.*
import kotlinx.android.synthetic.main.activity_player_tiktok_slide.*
import java.util.*

class PlayerTiktokSlideActivity : AppCompatActivity() {
    private val list = ArrayList<UZPlayback>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_tiktok_slide)

        setupViews()
    }

    private fun setupViews() {
        addData()
        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        setUpViewPager()

        btSwitchOrientation.setOnClickListener {
            if (viewPager.orientation == ViewPager2.ORIENTATION_VERTICAL) {
                viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            } else {
                viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
            }
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

    private fun setUpViewPager() {
        val adapter = VerticalAdapter(this, list)
        viewPager.adapter = adapter
    }

    private fun addData() {
        list.add(UZPlayback(linkPlay = Constant.LINK_PLAY_VOD_PORTRAIT, isPortraitVideo = true))
        list.add(UZPlayback(linkPlay = Constant.LINK_PLAY_VOD_PORTRAIT_1, isPortraitVideo = true))
        list.add(UZPlayback(linkPlay = Constant.LINK_PLAY_VOD_PORTRAIT_2, isPortraitVideo = true))
        list.add(UZPlayback(linkPlay = Constant.LINK_PLAY_VOD, isPortraitVideo = false))
        list.add(UZPlayback(linkPlay = Constant.LINK_PLAY_VOD_PORTRAIT_3, isPortraitVideo = true))
        list.add(UZPlayback(linkPlay = Constant.LINK_PLAY_VOD_PORTRAIT_4, isPortraitVideo = true))
        list.add(UZPlayback(linkPlay = Constant.LINK_PLAY_VOD_PORTRAIT_5, isPortraitVideo = true))
        list.add(UZPlayback(linkPlay = Constant.LINK_PLAY_VOD_PORTRAIT_6, isPortraitVideo = true))
    }

    private inner class VerticalAdapter(
        fragmentActivity: FragmentActivity,
        private val stringList: List<UZPlayback>
    ) :
        FragmentStateAdapter(fragmentActivity) {

        override fun createFragment(position: Int): Fragment {
            return FrmPlayerTiktok.newInstance(stringList[position])
        }

        override fun getItemCount(): Int {
            return stringList.size
        }
    }
}
