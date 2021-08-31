package com.uiza.sampleplayer

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.ui.common.error.ErrorActivity
import com.uiza.sampleplayer.ui.playerad.PlayerAdActivity
import com.uiza.sampleplayer.ui.playeradvanced.PlayerAdvancedActivity
import com.uiza.sampleplayer.ui.playerbasic.PlayerBasicActivity
import com.uiza.sampleplayer.ui.playerpip.PlayerPipActivity
import com.uiza.sampleplayer.ui.playerpreviewseekbar.PlayerPreviewSeekbarActivity
import com.uiza.sampleplayer.ui.playertiktok.PlayerTiktokActivity
import com.uiza.sampleplayer.ui.playertiktokslide.PlayerTiktokSlideActivity
import com.uiza.sampleplayer.ui.playerwithuzdragview.PlayerWithUZDragViewActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViews() {
        btnPlayerCast.visibility = View.GONE
        btnPlayerFragment.visibility = View.GONE
        btnPlayerList.visibility = View.GONE
        btnPlayerPip.visibility = View.GONE
        btnPlayerRecyclerView.visibility = View.GONE
        btnPlayerWithUZDragView.visibility = View.GONE
        btnPlayerYoutube.visibility = View.GONE

        btnError.setOnClickListener {
            gotoActivity(ErrorActivity::class.java)
        }
        btnPlayerAd.setOnClickListener {
            gotoActivity(PlayerAdActivity::class.java)
        }
        btnPlayerAdvanced.setOnClickListener {
            gotoActivity(PlayerAdvancedActivity::class.java)
        }
        btnPlayerBasic.setOnClickListener {
            gotoActivity(PlayerBasicActivity::class.java)
        }
        btnPlayerPip.setOnClickListener {
            gotoActivity(PlayerPipActivity::class.java)
        }
        btnPlayerPreviewSeekbar.setOnClickListener {
            gotoActivity(PlayerPreviewSeekbarActivity::class.java)
        }
        btnPlayerSkin.setOnClickListener {

        }
        btnPlayerTiktok.setOnClickListener {
            gotoActivity(PlayerTiktokActivity::class.java)
        }
        btnPlayerTiktokSlide.setOnClickListener {
            gotoActivity(PlayerTiktokSlideActivity::class.java)
        }
        btnPlayerWithUZDragView.setOnClickListener {
            gotoActivity(PlayerWithUZDragViewActivity::class.java)
        }
        btnPlayerRecyclerView.setOnClickListener {
        }

        txtVersion.text = String.format(
            Locale.getDefault(),
            "%s (%s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE
        )
    }

    private fun <T> gotoActivity(clazz: Class<T>) {
        startActivity(Intent(this, clazz))
    }
}
