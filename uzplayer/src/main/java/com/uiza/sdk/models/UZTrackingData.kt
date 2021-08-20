package com.uiza.sdk.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.uiza.sdk.analytics.UZAnalytic.Companion.deviceId
import com.uiza.sdk.analytics.helps.JsonDateSerializer
import com.uiza.sdk.utils.JacksonUtils
import java.util.*

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
class UZTrackingData(
    @field:JsonProperty("app_id")
    val appId: String,
    @field:JsonProperty("entity_id")
    var entityId: String,
    @field:JsonProperty("entity_source")
    var entitySource: String,
    @field:JsonProperty("viewer_session_id")
    val viewerSessionId: String?,
    type: UZEventType?
) {
    @JsonProperty("viewer_user_id")
    val viewerUserId: String? = deviceId

    @JsonProperty("event")
    var eventType: UZEventType? = type

    @JsonProperty("timestamp")
    @JsonSerialize(using = JsonDateSerializer::class)
    val timestamp: Date = Date()

    constructor(
        info: UZPlaybackInfo,
        viewerSessionId: String,
        type: UZEventType
    ) : this(
        appId = info.appId,
        entityId = info.entityId,
        entitySource = info.entitySource,
        viewerSessionId = viewerSessionId,
        type = type
    ) {
    }

    override fun toString(): String {
        return JacksonUtils.toJson(this)
    }

}
