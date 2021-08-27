package com.uiza.sdk.dialog.playlistfolder

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.uiza.sdk.R
import com.uiza.sdk.dialog.playlistfolder.AdapterPlaylistFolder.PlayListHolder
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.utils.ImageUtils
import com.uiza.sdk.utils.UZViewUtils

class AdapterPlaylistFolder(
    context: Context,
    private val playList: List<UZPlayback>,
    currentPositionOfDataList: Int,
    private val callbackPlaylistFolder: CallbackPlaylistFolder?
) : RecyclerView.Adapter<PlayListHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.row_playlist_folder, parent, false)
        return PlayListHolder(itemView)
    }

    override fun onBindViewHolder(playListHolder: PlayListHolder, position: Int) {
        val uzPlayback = playList[position]
        UZViewUtils.setTextDuration(
            textView = playListHolder.tvDuration,
            duration = "${uzPlayback.duration}"
        )
        playListHolder.tvName.text = uzPlayback.name
        UZViewUtils.setTextDuration(
            textView = playListHolder.tvDuration2,
            duration = "${uzPlayback.duration}"
        )
        if (TextUtils.isEmpty(uzPlayback.description)) {
            playListHolder.tvDescription.visibility = View.GONE
        } else {
            playListHolder.tvDescription.text = uzPlayback.description
            playListHolder.tvDescription.visibility = View.VISIBLE
        }
        ImageUtils.loadThumbnail(imageView = playListHolder.ivCover, imageUrl = uzPlayback.poster)
        playListHolder.rootView.setOnClickListener {
            callbackPlaylistFolder?.onClickItem(
                playback = uzPlayback,
                position = position
            )
        }
        playListHolder.rootView.onFocusChangeListener =
            OnFocusChangeListener { _: View?, isFocus: Boolean ->
                if (isFocus) {
                    playListHolder.rootView.setBackgroundResource(R.drawable.bkg_item_playlist_folder)
                } else {
                    playListHolder.rootView.setBackgroundResource(0)
                }
                callbackPlaylistFolder?.onFocusChange(playback = uzPlayback, position = position)
            }
    }

    override fun getItemCount(): Int {
        return playList.size
    }

    class PlayListHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDuration: TextView = view.findViewById(R.id.tvDurationUZ)
        val tvDuration2: TextView = view.findViewById(R.id.tvDuration2)
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val rootView: CardView = view.findViewById(R.id.rootView)
    }
}
