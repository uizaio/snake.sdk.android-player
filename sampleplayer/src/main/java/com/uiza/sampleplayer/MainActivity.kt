package com.uiza.sampleplayer

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
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
        btnPlayerWithUZDragView.setOnClickListener {
            gotoActivity(PlayerActivity::class.java)
        }
        btnPipPlayer.setOnClickListener {
            gotoActivity(PipPlayerActivity::class.java)
        }
        btnCastPlayer.setOnClickListener {
            gotoActivity(CastPlayerActivity::class.java)
        }
        btnAnalytic.setOnClickListener {
            gotoActivity(AnalyticActivity::class.java)
        }
        btnError.setOnClickListener {
            gotoActivity(ErrorActivity::class.java)
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
