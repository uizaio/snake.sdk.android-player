package com.uiza.sampleplayer.ui.playerrecyclerview

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uiza.sampleplayer.R
import com.uiza.sdk.view.UZVideoView
import kotlinx.android.synthetic.main.view_item_recycler.view.*

class RecyclerAdapter(private val list: List<ItemRv>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onClickItem: ((index: Int, itemRv: ItemRv) -> Unit)? = null
    private val listUZVideo = ArrayList<UZVideoView>()

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(itemRv: ItemRv) {
            itemView.tvName.text = itemRv.uzPlayback?.name
            itemView.tvAd.text = "urlIMAAd: ${itemRv.uzPlayback?.urlIMAAd}"
            itemView.tvIsPortrait.text = "isPortraitVideo ${itemRv.uzPlayback?.isPortraitVideo}"
            itemView.tvPoster.text = "poster ${itemRv.uzPlayback?.poster}"
            itemView.tvLinkPlay.text = "linkPlay ${itemRv.uzPlayback?.linkPlay}"

            if (itemRv.isPlaying) {
                listUZVideo.forEach {
                    it.pause()
                    listUZVideo.remove(it)
                }
                itemView.linearLayout.setBackgroundColor(Color.GREEN)
                if (itemView.uzVideoView.isViewCreated()) {
                    itemRv.uzPlayback?.let {
                        itemView.uzVideoView.play(it)
                        listUZVideo.add(itemView.uzVideoView)
                    }
                }
            } else {
                itemView.linearLayout.setBackgroundColor(Color.WHITE)
                itemView.uzVideoView.onDestroyView()
            }
            itemView.cardView.setOnClickListener {
                onClickItem?.invoke(adapterPosition, itemRv)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_item_recycler, parent, false)
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

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        listUZVideo.forEach {
            it.onDestroyView()
        }
    }
}
