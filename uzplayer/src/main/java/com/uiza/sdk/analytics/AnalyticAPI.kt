package com.uiza.sdk.analytics

import retrofit2.http.POST
import com.uiza.sdk.models.UZTrackingBody
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Body

interface AnalyticAPI {
    //UZ Tracking API
    @POST("/v1/events")
    fun pushEvents(@Body trackingBody: UZTrackingBody<*>?): Observable<ResponseBody?>?
}
