package com.uiza.sdk.dialog.speed

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.widget.CheckedTextView
import com.uiza.sdk.R
import kotlinx.android.synthetic.main.dlg_speed_uz.*

class UZSpeedDialog(
    context: Context,
    private val currentSpeed: Float,
    private val callback: Callback?
) : Dialog(context), View.OnClickListener {
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val SPEED_025 = "0.25"
        private const val SPEED_050 = "0.5"
        private const val SPEED_075 = "0.75"
        private const val SPEED_100 = "Normal"
        private const val SPEED_125 = "1.25"
        private const val SPEED_150 = "1.5"
        private const val SPEED_200 = "2.0"
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_speed_uz)
        setupViews()
    }

    private fun setupViews() {
        val speed0 = Speed(name = SPEED_025, value = 0.25f)
        val speed1 = Speed(name = SPEED_050, value = 0.5f)
        val speed2 = Speed(name = SPEED_075, value = 0.75f)
        val speed3 = Speed(name = SPEED_100, value = 1f)
        val speed4 = Speed(name = SPEED_125, value = 1.25f)
        val speed5 = Speed(name = SPEED_150, value = 1.5f)
        val speed6 = Speed(name = SPEED_200, value = 2f)

        ct0.text = speed0.name
        ct1.text = speed1.name
        ct2.text = speed2.name
        ct3.text = speed3.name
        ct4.text = speed4.name
        ct5.text = speed5.name
        ct6.text = speed6.name

        ct0.tag = speed0
        ct1.tag = speed1
        ct2.tag = speed2
        ct3.tag = speed3
        ct4.tag = speed4
        ct5.tag = speed5
        ct6.tag = speed6

        setEvent(ct0)
        setEvent(ct1)
        setEvent(ct2)
        setEvent(ct3)
        setEvent(ct4)
        setEvent(ct5)
        setEvent(ct6)

        when (currentSpeed) {
            speed0.value -> {
                scrollTo(ct0)
            }
            speed1.value -> {
                scrollTo(ct1)
            }
            speed2.value -> {
                scrollTo(ct2)
            }
            speed3.value -> {
                scrollTo(ct3)
            }
            speed4.value -> {
                scrollTo(ct4)
            }
            speed5.value -> {
                scrollTo(ct5)
            }
            speed6.value -> {
                scrollTo(ct6)
            }
        }
    }

    private fun scrollTo(checkedTextView: CheckedTextView) {
        checkedTextView.isChecked = true
//        handler.postDelayed({
//            sv.scrollTo(0, checkedTextView.top)
//        }, 100)
    }

    private fun setEvent(checkedTextView: CheckedTextView) {
        checkedTextView.isFocusable = true
        checkedTextView.isSoundEffectsEnabled = false
        checkedTextView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        ct0.isChecked = false
        ct1.isChecked = false
        ct2.isChecked = false
        ct3.isChecked = false
        ct4.isChecked = false
        ct5.isChecked = false
        ct6.isChecked = false
        if (view is CheckedTextView) {
            view.isChecked = !view.isChecked
            if (view.getTag() is Speed) {
                callback?.onSelectItem((view.getTag() as Speed))
            }
        }
        handler.postDelayed({
            cancel()
        }, 100)
    }
}
