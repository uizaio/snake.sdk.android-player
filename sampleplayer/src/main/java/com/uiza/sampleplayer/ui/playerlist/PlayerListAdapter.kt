package com.uiza.sampleplayer.ui.playerlist

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uiza.sampleplayer.R
import kotlinx.android.synthetic.main.view_item_player_list.view.*

class PlayerListAdapter(private val list: List<Item>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onClickItem: ((index: Int, item: Item) -> Unit)? = null

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(item: Item) {
            itemView.tvName.text = item.uzPlayback?.name
            itemView.tvAd.text = "urlIMAAd: ${item.uzPlayback?.urlIMAAd}"
            itemView.tvIsPortrait.text = "isPortraitVideo ${item.uzPlayback?.isPortraitVideo}"
            itemView.tvPoster.text = "poster ${item.uzPlayback?.poster}"
            itemView.tvLinkPlay.text = "linkPlay ${item.uzPlayback?.linkPlay}"

            if (item.isPlaying) {
                itemView.linearLayout.setBackgroundColor(Color.GREEN)
            } else {
                itemView.linearLayout.setBackgroundColor(Color.WHITE)
            }
            itemView.cardView.setOnClickListener {
                onClickItem?.invoke(adapterPosition, item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_item_player_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val movie = list[position]
            holder.bind(movie)
        }
    }
}
