package com.uiza.sampleplayer.ui.playerlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uiza.sampleplayer.R
import com.uiza.sdk.models.UZPlayback
import kotlinx.android.synthetic.main.view_item_player_list.view.*

class PlayerListAdapter(private val list: List<UZPlayback>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onClickItem: ((index: Int, uzPlayback: UZPlayback) -> Unit)? = null

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(uzPlayback: UZPlayback) {
            itemView.tvName.text = uzPlayback.name
            itemView.tvAd.text = "Ad: ${uzPlayback.urlIMAAd}"
            itemView.tvIsPortrait.text = "isPortraitVideo ${uzPlayback.isPortraitVideo}"
            itemView.tvPoster.text = "poster ${uzPlayback.poster}"
            itemView.tvLinkPlay.text = "poster ${uzPlayback.linkPlay}"

            itemView.cardView.setOnClickListener {
                onClickItem?.invoke(adapterPosition, uzPlayback)
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
