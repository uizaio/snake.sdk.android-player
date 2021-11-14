package com.uiza.sdk.dialog.setting

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.uiza.sdk.R

class SettingAdapter(
    private val mContext: Context,
    private val items: List<SettingItem>
) :
    ArrayAdapter<SettingItem>(
        mContext,
        0,
        items
    ) {
    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder
        val item = getItem(position)

        if (convertView == null) {
            val inflater =
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (getItemViewType(position) == 0) {
                view = inflater.inflate(R.layout.view_setting_list_item_toggle_uz, null)
                holder = ViewHolder0(view)
            } else {
                view = inflater.inflate(R.layout.view_setting_list_item_uz, null)
                holder = ViewHolder(view)
            }
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        if (item != null) {
            holder.setTitle(item.title)
            if (holder is ViewHolder0) {
                holder.setChecked(item.checked)
                holder.setOnToggleChangeListener(item.listener)
            }
        }
        return view
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isToggle) 0 else 1
    }

    internal open class ViewHolder0(root: View) : ViewHolder(root) {
        private var toggleBox: SwitchCompat = root.findViewById(R.id.toggleBox)
        var listener: OnToggleChangeListener? = null

        fun setChecked(checked: Boolean) {
            toggleBox.isChecked = checked
        }

        fun setOnToggleChangeListener(listener: OnToggleChangeListener?) {
            this.listener = listener
        }

        init {
            root.setOnClickListener {
                listener?.let {
                    val nextCheck = !toggleBox.isChecked
                    if (it.onCheckedChanged(nextCheck)) {
                        toggleBox.isChecked = nextCheck
                    }
                }
            }
            toggleBox.setOnCheckedChangeListener { _: CompoundButton?, checked: Boolean ->
                if (listener?.onCheckedChanged(checked) == true) {
                    toggleBox.isChecked = checked
                }
            }
        }
    }

    internal open class ViewHolder(root: View) {
        private var text1: TextView = root.findViewById(android.R.id.text1)

        fun setTitle(title: String?) {
            text1.text = title
        }
    }
}
