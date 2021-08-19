package com.uiza.sdk.analytics

import com.uiza.sdk.analytics.RxBinder.bind
import com.uiza.sdk.models.UZTrackingBody
import com.uiza.sdk.models.UZTrackingData
import com.uiza.sdk.utils.Constants
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import okhttp3.ResponseBody

class UZAnalytic private constructor() {
    companion object {

        var isProdEnv = false

        var sourceName: String? = null

        @JvmStatic
        var deviceId: String? = null

        /**
         * @param deviceId: DeviceId or AndroidId
         */
        @JvmStatic
        fun init(deviceId: String?, prodEnv: Boolean) {
            Companion.deviceId = deviceId
            sourceName = String.format("UZData/AndroidSDK/%s", Constants.PLAYER_SDK_VERSION)
            isProdEnv = prodEnv
        }

        @Throws(IllegalStateException::class)
        fun pushEvent(
            data: UZTrackingData, onNext: Consumer<ResponseBody?>?,
            onError: Consumer<Throwable?>?
        ): Disposable {
            //TODO remove !!
            return bind(
                observable = UZAnalyticClient.getInstance().createAnalyticAPI()
                    .pushEvents(UZTrackingBody.create(data))!!,
                onNext = onNext,
                onError = onError
            )
        }

        @Throws(IllegalStateException::class)
        fun pushEvent(
            data: UZTrackingData, onNext: Consumer<ResponseBody?>?,
            onError: Consumer<Throwable?>?, onComplete: Action?
        ): Disposable {
            //TODO remove !!
            return bind(
                observable = UZAnalyticClient.getInstance().createAnalyticAPI()
                    .pushEvents(UZTrackingBody.create(data))!!,
                onNext = onNext,
                onError = onError,
                onComplete = onComplete
            )
        }

        @Throws(IllegalStateException::class)
        fun pushEvents(
            data: List<UZTrackingData>, onNext: Consumer<ResponseBody?>?,
            onError: Consumer<Throwable?>?
        ): Disposable {
            //TODO remove !!
            return bind(
                observable = UZAnalyticClient.getInstance().createAnalyticAPI()
                    .pushEvents(UZTrackingBody.create(data))!!,
                onNext = onNext,
                onError = onError
            )
        }

        @Throws(IllegalStateException::class)
        fun pushEvents(
            data: List<UZTrackingData>, onNext: Consumer<ResponseBody?>?,
            onError: Consumer<Throwable?>?, onComplete: Action?
        ): Disposable {
            //TODO remove !!
            return bind(
                observable = UZAnalyticClient.getInstance().createAnalyticAPI()
                    .pushEvents(UZTrackingBody.create(data))!!,
                onNext = onNext,
                onError = onError,
                onComplete = onComplete
            )
        }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}
