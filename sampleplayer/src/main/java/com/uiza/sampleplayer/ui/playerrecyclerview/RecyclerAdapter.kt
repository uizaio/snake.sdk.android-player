package com.uiza.sampleplayer.ui.playerrecyclerview

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uiza.sampleplayer.R
import com.uiza.sdk.models.UZPlayback
import com.uiza.sdk.view.UZVideoView
import kotlinx.android.synthetic.main.view_item_recycler.view.*

class RecyclerAdapter(private val list: List<ItemRv>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onClickItem: ((index: Int, itemRv: ItemRv) -> Unit)? = null
    var uzVideoView: UZVideoView? = null

    private fun play(uzPlayback: UZPlayback?) {
        uzVideoView?.let { v ->
            if (v.isViewCreated()) {
                uzPlayback?.let {
                    v.play(it)
                }
            }
        }
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(itemRv: ItemRv) {
            itemView.tvName.text = itemRv.uzPlayback?.name
            itemView.tvAd.text = "urlIMAAd: ${itemRv.uzPlayback?.urlIMAAd}"
            itemView.tvIsPortrait.text = "isPortraitVideo ${itemRv.uzPlayback?.isPortraitVideo}"
            itemView.tvPoster.text = "poster ${itemRv.uzPlayback?.poster}"
            itemView.tvLinkPlay.text = "linkPlay ${itemRv.uzPlayback?.linkPlay}"

            if (itemRv.isFocussed) {
                itemView.linearLayout.setBackgroundColor(Color.GREEN)
                if (uzVideoView == itemView.uzVideoView) {
                    uzVideoView?.onResumeView()
                    itemView.tvPause.visibility = View.GONE
                } else {
                    uzVideoView?.onPauseView()
                    uzVideoView = itemView.uzVideoView
                    play(itemRv.uzPlayback)
                }
            } else {
                itemView.linearLayout.setBackgroundColor(Color.WHITE)
                itemView.tvPause.visibility = View.GONE
                itemView.uzVideoView.onDestroyView()
            }
            itemView.cardView.setOnClickListener {
                if (itemRv.isFocussed) {
                    if (uzVideoView?.isPlaying == false) {
                        onClickItem?.invoke(bindingAdapterPosition, itemRv)
                    }
                } else {
                    onClickItem?.invoke(bindingAdapterPosition, itemRv)
                }
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

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        uzVideoView?.onPauseView()
        try {
            if (list[holder.bindingAdapterPosition].isFocussed) {
                holder.itemView.tvPause.visibility = View.VISIBLE
            } else {
                holder.itemView.tvPause.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onDestroy() {
        uzVideoView?.onPauseView()
    }
}
