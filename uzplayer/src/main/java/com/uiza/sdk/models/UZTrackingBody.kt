package com.uiza.sdk.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.uiza.sdk.analytics.UZAnalytic.Companion.sourceName
import com.uiza.sdk.analytics.helps.JsonDateSerializer
import com.uiza.sdk.utils.JacksonUtils
import java.util.*

@Keep
@JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true, allowSetters = true)
class UZTrackingBody<T>() {

    companion object {
        fun <T> create(data: T): UZTrackingBody<*> {
            return UZTrackingBody(data)
        }
    }

    @JsonProperty("specversion")
    val specVersion = "1.0"

    @JsonProperty("source")
    val source: String? = sourceName

    @JsonProperty("type")
    var type: String = "io.uiza.watchingevent"

    @JsonProperty("time")
    @JsonSerialize(using = JsonDateSerializer::class)
    val time: Date = Date()

    @JsonProperty("data")
    var data: T? = null
        private set

    constructor(data: T) : this() {
        this.data = data
    }

    fun setData(data: T) {
        this.data = data
    }

    override fun toString(): String {
        return JacksonUtils.toJson(this)
    }

}
