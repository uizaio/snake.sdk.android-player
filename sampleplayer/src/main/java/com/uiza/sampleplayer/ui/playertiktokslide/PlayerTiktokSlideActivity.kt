package com.uiza.sampleplayer.ui.playertiktokslide

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sampleplayer.model.DataVideo
import kotlinx.android.synthetic.main.activity_player_tiktok_slide.*
import java.util.*

class PlayerTiktokSlideActivity : AppCompatActivity() {
    private val listDataVideo = ArrayList<DataVideo>()

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
    }

    private fun setUpViewPager() {
        val adapter = VerticalAdapter(this, listDataVideo)
        viewPager.adapter = adapter
    }

    private fun addData() {
        listDataVideo.add(DataVideo(Constant.LINK_PLAY_VOD_PORTRAIT, true))
        listDataVideo.add(DataVideo(Constant.LINK_PLAY_VOD_PORTRAIT_1, true))
        listDataVideo.add(DataVideo(Constant.LINK_PLAY_VOD_PORTRAIT_2, true))
        listDataVideo.add(DataVideo(Constant.LINK_PLAY_VOD, false))
        listDataVideo.add(DataVideo(Constant.LINK_PLAY_VOD_PORTRAIT_3, true))
        listDataVideo.add(DataVideo(Constant.LINK_PLAY_VOD_PORTRAIT_4, true))
        listDataVideo.add(DataVideo(Constant.LINK_PLAY_VOD_PORTRAIT_5, true))
        listDataVideo.add(DataVideo(Constant.LINK_PLAY_VOD_PORTRAIT_6, true))
    }

    private inner class VerticalAdapter(
        fragmentActivity: FragmentActivity,
        private val stringList: List<DataVideo>
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
