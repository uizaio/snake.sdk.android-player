package com.uiza.sampleplayer.ui.playertiktokslide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uiza.sampleplayer.R
import kotlinx.android.synthetic.main.fragment_player_tiktok.*

class FrmPlayerTiktok : Fragment() {

    companion object {

        private const val TEXT = "text"

        fun newInstance(data: String): FrmPlayerTiktok {
            val fragment = FrmPlayerTiktok()
            val bundle = Bundle(1)
            bundle.putString(TEXT, data)
            fragment.arguments = bundle
            return fragment
        }
    }

    var text: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        text = arguments?.getString(TEXT) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player_tiktok, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv.text = text
    }

}
