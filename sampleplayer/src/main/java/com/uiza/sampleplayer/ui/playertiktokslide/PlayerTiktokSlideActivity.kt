package com.uiza.sampleplayer.ui.playertiktokslide

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.uiza.sampleplayer.R
import com.uiza.sampleplayer.app.Constant
import kotlinx.android.synthetic.main.activity_player_tiktok_slide.*
import java.util.*

class PlayerTiktokSlideActivity : AppCompatActivity() {
    private val stringList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_tiktok_slide)
        setupViews()
    }

    private fun setupViews() {
        addData()
        viewPager.adapter = VerticalAdapter(supportFragmentManager, stringList)
    }

    private fun addData() {
        stringList.add(Constant.LINK_PLAY_VOD_PORTRAIT)
        stringList.add(Constant.LINK_PLAY_VOD_PORTRAIT_1)
        stringList.add(Constant.LINK_PLAY_VOD_PORTRAIT_2)
        stringList.add(Constant.LINK_PLAY_VOD_PORTRAIT_3)
        stringList.add(Constant.LINK_PLAY_VOD_PORTRAIT_4)
        stringList.add(Constant.LINK_PLAY_VOD_PORTRAIT_5)
        stringList.add(Constant.LINK_PLAY_VOD_PORTRAIT_6)
    }

    class VerticalAdapter(
        fm: FragmentManager,
        private val stringList: List<String>
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return FrmPlayerTiktok.newInstance(stringList[position])
        }

        override fun getCount(): Int {
            return stringList.size
        }
    }
}
