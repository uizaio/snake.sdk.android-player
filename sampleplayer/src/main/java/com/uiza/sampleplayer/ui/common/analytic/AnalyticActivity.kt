package com.uiza.sampleplayer.ui.common.analytic

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.uiza.api.UZApi.getLiveViewers
import com.uiza.sampleplayer.R
import com.uiza.sdk.analytics.UZAnalytic
import com.uiza.sdk.models.UZEventType
import com.uiza.sdk.models.UZPlaybackInfo
import com.uiza.sdk.models.UZTrackingData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_common_analytic.*
import okhttp3.ResponseBody
import java.util.*

class AnalyticActivity : AppCompatActivity() {

    private fun log(msg: String) {
        Log.d("loitpp" + javaClass.simpleName, msg)
    }

    var disposables: CompositeDisposable = CompositeDisposable()
    var sessionId: String = UUID.randomUUID().toString()
    var info = UZPlaybackInfo(
        "b963b465c34e4ffb9a71922442ee0dca",
        "b938c0a6-e9bc-4b25-9e66-dbf81d755c25",
        "live"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_analytic)
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
        btOneEvent.setOnClickListener {
            trackEvent()
        }
        btSomeEvents.setOnClickListener {
            trackEvents()
        }
        btLiveViewers.setOnClickListener {
            liveViewers
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.dispose()
    }

    private fun trackEvent() {
        val data = UZTrackingData(info, sessionId, UZEventType.WATCHING)
        data.eventType = UZEventType.WATCHING
        disposables.add(
            UZAnalytic.pushEvent(data,
                { responseBody: ResponseBody? ->
                    log("onNext " + responseBody?.contentLength())
                    txtLog.text = "trackEvent onNext " + responseBody?.contentLength()
                },
                { error: Throwable? ->
                    log("onError $error")
                    txtLog.text = "trackEvent::onError $error"
                }
            ) {
                log("completed")
            })
    }

    private fun trackEvents() {
        val data1 = UZTrackingData(info, sessionId, UZEventType.WATCHING)
        val data2 = UZTrackingData(info, sessionId, UZEventType.WATCHING)
        disposables.add(UZAnalytic.pushEvents(
            listOf(data1, data2), { responseBody: ResponseBody? ->
                log("onNext " + responseBody?.contentLength())
                txtLog.text = "trackEvents::onNext " + responseBody?.contentLength()
            },
            { error: Throwable? ->
                log("onError $error")
                txtLog.text = "trackEvents::onError $error"
            }
        ) {
            log("completed")
        }
        )
    }

    private val liveViewers: Unit
        get() {
            disposables.add(
                getLiveViewers(
                    appId = info.appId,
                    entityId = info.entityId,
                    onNext = Consumer { (views) ->
                        log("onNext: $views")
                        txtLog.text = "Views: $views"
                    },
                    onError = Consumer { error: Throwable ->
                        log("onError $error")
                        txtLog.text = "getLiveViewers::onError $error"
                    })
            )
        }
}
