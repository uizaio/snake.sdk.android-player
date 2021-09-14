package com.uiza.sampleplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uiza.samplebroadcast.DisplayBasicActivity
import com.uiza.sampleplayer.ui.common.error.ErrorActivity
import com.uiza.sampleplayer.ui.playerad.PlayerAdActivity
import com.uiza.sampleplayer.ui.playeradvanced.PlayerAdvancedActivity
import com.uiza.sampleplayer.ui.playerbasic.PlayerBasicActivity
import com.uiza.sampleplayer.ui.playerfragment.PlayerFragmentActivity
import com.uiza.sampleplayer.ui.playerlist.PlayerListActivity
import com.uiza.sampleplayer.ui.playerpip.PlayerPipActivity
import com.uiza.sampleplayer.ui.playerpreviewseekbar.PlayerPreviewSeekbarActivity
import com.uiza.sampleplayer.ui.playerrecyclerview.PlayerRecyclerViewActivity
import com.uiza.sampleplayer.ui.playerskin.PlayerSkinActivity
import com.uiza.sampleplayer.ui.playertiktok.PlayerTiktokActivity
import com.uiza.sampleplayer.ui.playertiktokslide.PlayerTiktokSlideActivity
import com.uiza.sampleplayer.ui.playerwithuzdragview.PlayerWithUZDragViewActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

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
        btnPlayerFragment.setOnClickListener {
            gotoActivity(PlayerFragmentActivity::class.java)
        }
        btnPlayerList.setOnClickListener {
            gotoActivity(PlayerListActivity::class.java)
        }
        btnPlayerPip.setOnClickListener {
            gotoActivity(PlayerPipActivity::class.java)
        }
        btnPlayerPreviewSeekbar.setOnClickListener {
            gotoActivity(PlayerPreviewSeekbarActivity::class.java)
        }
        btnPlayerRecyclerView.setOnClickListener {
            gotoActivity(PlayerRecyclerViewActivity::class.java)
        }
        btnPlayerSkin.setOnClickListener {
            gotoActivity(PlayerSkinActivity::class.java)
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
        btnPlayerYoutube.setOnClickListener {
            toast("Coming soon")
        }
        btnDisplayBasic.setOnClickListener {
            gotoActivity(DisplayBasicActivity::class.java)
        }
        btnGithub.setOnClickListener {
            val intent =
                Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
            intent.data = Uri.parse("https://github.com/uizaio/snake.sdk.android-player")
            startActivity(intent)
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
