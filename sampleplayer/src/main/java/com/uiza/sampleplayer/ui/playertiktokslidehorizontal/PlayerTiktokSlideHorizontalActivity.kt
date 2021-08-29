package com.uiza.sampleplayer.ui.playertiktokslidehorizontal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import com.uiza.sampleplayer.model.DataVideo
import kotlinx.android.synthetic.main.activity_player_tiktok_slide_horizontal.*
import java.util.*

class PlayerTiktokSlideHorizontalActivity : AppCompatActivity() {
    private val listDataVideo = ArrayList<DataVideo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_tiktok_slide_horizontal)
        setupViews()
    }

    private fun setupViews() {
        addData()
        viewPager.adapter = VerticalAdapter(supportFragmentManager, listDataVideo)
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

    class VerticalAdapter(
        fm: FragmentManager,
        private val stringList: List<DataVideo>
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return FrmPlayerTiktok.newInstance(stringList[position])
        }

        override fun getCount(): Int {
            return stringList.size
        }
    }
}
