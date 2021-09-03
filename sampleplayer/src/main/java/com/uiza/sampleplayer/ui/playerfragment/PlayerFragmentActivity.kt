package com.uiza.sampleplayer.ui.playerfragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.uiza.sampleplayer.R
import kotlinx.android.synthetic.main.activity_player_fragment.*

class PlayerFragmentActivity : AppCompatActivity() {
    private val fragmentPlayer = FragmentPlayer()

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setContentView(R.layout.activity_player_fragment)

        switchFragment(fragmentPlayer)
    }

    private fun switchFragment(fragment: Fragment) {
        try {
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            ft.replace(R.id.flContainer, fragment)
            ft.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        fragmentPlayer.onUserLeaveHint()
    }
}
