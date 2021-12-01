package com.uiza.samplebroadcast

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.uiza.display.UZDisplayView
import com.uiza.sampleplayer.R
import com.uiza.util.UZConstant
import com.uiza.util.UZUtil
import kotlinx.android.synthetic.main.activity_display_basic.*

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class DisplayBasicActivity : AppCompatActivity() {

    companion object {
        const val URL_STREAM =
            "rtmp://a.rtmp.youtube.com/live2/tkrp-q1kj-e8x4-4b41-exxf"

        private const val REQUEST_CODE = 1
        private val PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private var videoWidth = UZConstant.VIDEO_WIDTH_DEFAULT
    private var videoHeight = UZConstant.VIDEO_HEIGHT_DEFAULT
    private var videoFps = UZConstant.VIDEO_FPS_DEFAULT
    private var videoBitrate = UZConstant.VIDEO_BITRATE_DEFAULT
    private var videoRotation = UZConstant.VIDEO_ROTATION_DEFAULT
    private var videoDpi = UZConstant.VIDEO_DPI_DEFAULT

    private var audioBitrate = UZConstant.AUDIO_BITRATE_DEFAULT
    private var audioSampleRate = UZConstant.AUDIO_SAMPLE_RATE_DEFAULT
    private var audioIsStereo = UZConstant.AUDIO_IS_STEREO_DEFAULT
    private var audioEchoCanceler = UZConstant.AUDIO_ECHO_CANCELER_DEFAULT
    private var audioNoiseSuppressor = UZConstant.AUDIO_NOISE_SUPPRESSOR_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_display_basic)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupViews()
    }

    override fun onResume() {
        super.onResume()
        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
        }
        handleUI()
    }

    private fun showPermissionsErrorAndRequest() {
        showToast("You need permissions before")
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        setupTvSetting()
        Glide.with(this).load(UZUtil.URL_GIF_2).into(iv)
        etRtpUrl.setText(URL_STREAM)

        uzDisplayBroadCast.onConnectionStartedRtp = { rtpUrl ->
            tvStatus.text = "onConnectionStartedRtp $rtpUrl"
        }
        uzDisplayBroadCast.onConnectionSuccessRtp = {
            tvStatus.text = "onConnectionSuccessRtp"
            handleUI()
        }
        uzDisplayBroadCast.onNewBitrateRtp = { bitrate ->
            tvStatus.text = "onNewBitrateRtp bitrate $bitrate"
        }
        uzDisplayBroadCast.onConnectionFailedRtp = { reason ->
            handleUI()
            tvStatus.text = "onConnectionFailedRtp reason $reason"
        }
        uzDisplayBroadCast.onDisconnectRtp = {
            tvStatus.text = "onDisconnectRtp"
            handleUI()
        }
        uzDisplayBroadCast.onAuthErrorRtp = {
            tvStatus.text = "onAuthErrorRtp"
        }
        uzDisplayBroadCast.onAuthSuccessRtp = {
            tvStatus.text = "onAuthSuccessRtp"
        }
        bStartTop.setOnClickListener {
            handleBStartTop()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && (
            requestCode == UZDisplayView.REQUEST_CODE_STREAM ||
                requestCode == UZDisplayView.REQUEST_CODE_RECORD &&
                resultCode == RESULT_OK
            )
        ) {
            val endPoint = etRtpUrl.text.toString()
            uzDisplayBroadCast.onActivityResult(
                requestCode = requestCode,
                resultCode = resultCode,
                data = data,
                endPoint = endPoint,
                videoWidth = videoWidth,
                videoHeight = videoHeight,
                videoFps = videoFps,
                videoBitrate = videoBitrate,
                videoRotation = videoRotation,
                videoDpi = videoDpi,
                audioBitrate = audioBitrate,
                audioSampleRate = audioSampleRate,
                audioIsStereo = audioIsStereo,
                audioEchoCanceler = audioEchoCanceler,
                audioNoiseSuppressor = audioNoiseSuppressor,
            )
        } else {
            showToast("No permissions available")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupTvSetting() {
        tvSetting.text =
            "videoWidth: $videoWidth, videoHeight: $videoHeight, videoFps: $videoFps, videoBitrate: $videoBitrate, videoRotation: $videoRotation, videoDpi: $videoDpi" +
            "\naudioBitrate: $audioBitrate, audioSampleRate: $audioSampleRate, audioIsStereo: $audioIsStereo, audioEchoCanceler: $audioEchoCanceler, audioNoiseSuppressor: $audioNoiseSuppressor"
    }

    private fun handleBStartTop() {
        if (hasPermissions(this, *PERMISSIONS)) {
            if (uzDisplayBroadCast.isStreaming() == false) {
                uzDisplayBroadCast.start(this)
            } else {
                uzDisplayBroadCast.stop(
                    onStopPreExecute = {
                        bStartTop.isVisible = false
                    },
                    onStopSuccess = {
                        bStartTop.isVisible = true
                    }
                )
            }
            if (uzDisplayBroadCast.isStreaming() == false && uzDisplayBroadCast.isRecording() == false) {
                uzDisplayBroadCast.stopNotification()
            }
        } else {
            showPermissionsErrorAndRequest()
        }
    }

    private fun showToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetTextI18n")
    private fun handleUI() {
        if (uzDisplayBroadCast.isStreaming() == true) {
            bStartTop.text = "Stop"
        } else {
            bStartTop.text = "Start"
        }
    }
}
