package com.uiza.sdk.dialog.playlistfolder

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import com.uiza.sdk.R
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.UZData
import kotlinx.android.synthetic.main.dlg_list_playlist_folder_uz.*

class UZPlaylistFolderDialog(
    private val mContext: Context,
    private val playList: List<UZPlayback>,
    private val currentPositionOfDataList: Int,
    private val callbackPlaylistFolder: CallbackPlaylistFolder?
) : Dialog(mContext) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_list_playlist_folder_uz)

        setupViews()
    }

    private fun setupViews() {
        btExit.setOnClickListener {
            dismiss()
        }
        btExit.onFocusChangeListener = OnFocusChangeListener { _: View?, isFocus: Boolean ->
            if (isFocus) {
                btExit.setColorFilter(Color.WHITE)
                btExit.setBackgroundColor(Color.BLACK)
            } else {
                btExit.setBackgroundColor(Color.TRANSPARENT)
                btExit.setColorFilter(Color.BLACK)
            }
        }
        val layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        val adapterPlaylistFolder = AdapterPlaylistFolder(
            mContext,
            playList,
            currentPositionOfDataList,
            object : CallbackPlaylistFolder {
                override fun onClickItem(playback: UZPlayback, position: Int) {
                    dismiss()
                    callbackPlaylistFolder?.onClickItem(playback, position)
                }

                override fun onFocusChange(playback: UZPlayback, position: Int) {
                    recyclerView.smoothScrollToPosition(position)
                }

                override fun onDismiss() {
                    callbackPlaylistFolder?.onDismiss()
                }
            })
        recyclerView.adapter = adapterPlaylistFolder
        recyclerView.scrollToPosition(currentPositionOfDataList)
        recyclerView.requestFocus()
        recyclerView.postDelayed({
            val holder = recyclerView.findViewHolderForAdapterPosition(currentPositionOfDataList)
                ?: return@postDelayed
            holder.itemView.requestFocus()
        }, 100)
    }
}
