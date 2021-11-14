package com.uiza.sampleplayer.ui.common.error

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.uiza.sampleplayer.R
import com.uiza.sdk.exceptions.ErrorConstant
import kotlinx.android.synthetic.main.activity_common_error.*

class ErrorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_error)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViews() {
        val s = """${ErrorConstant.ERR_CODE_0} - ${ErrorConstant.ERR_0}
${ErrorConstant.ERR_CODE_5} - ${ErrorConstant.ERR_5}
${ErrorConstant.ERR_CODE_6} - ${ErrorConstant.ERR_6}
${ErrorConstant.ERR_CODE_7} - ${ErrorConstant.ERR_7}
${ErrorConstant.ERR_CODE_9} - ${ErrorConstant.ERR_9}
${ErrorConstant.ERR_CODE_10} - ${ErrorConstant.ERR_10}
${ErrorConstant.ERR_CODE_11} - ${ErrorConstant.ERR_11}
${ErrorConstant.ERR_CODE_12} - ${ErrorConstant.ERR_12}
${ErrorConstant.ERR_CODE_13} - ${ErrorConstant.ERR_13}
${ErrorConstant.ERR_CODE_14} - ${ErrorConstant.ERR_14}
${ErrorConstant.ERR_CODE_15} - ${ErrorConstant.ERR_15}
${ErrorConstant.ERR_CODE_16} - ${ErrorConstant.ERR_16}
${ErrorConstant.ERR_CODE_17} - ${ErrorConstant.ERR_17}
${ErrorConstant.ERR_CODE_18} - ${ErrorConstant.ERR_18}
${ErrorConstant.ERR_CODE_19} - ${ErrorConstant.ERR_19}
${ErrorConstant.ERR_CODE_20} - ${ErrorConstant.ERR_20}
${ErrorConstant.ERR_CODE_22} - ${ErrorConstant.ERR_22}
${ErrorConstant.ERR_CODE_23} - ${ErrorConstant.ERR_23}
${ErrorConstant.ERR_CODE_24} - ${ErrorConstant.ERR_24}
${ErrorConstant.ERR_CODE_400} - ${ErrorConstant.ERR_400}
${ErrorConstant.ERR_CODE_401} - ${ErrorConstant.ERR_401}
${ErrorConstant.ERR_CODE_404} - ${ErrorConstant.ERR_404}
${ErrorConstant.ERR_CODE_422} - ${ErrorConstant.ERR_422}
${ErrorConstant.ERR_CODE_500} - ${ErrorConstant.ERR_500}
${ErrorConstant.ERR_CODE_503} - ${ErrorConstant.ERR_503}
"""
        tvErr.text = s
    }
}
