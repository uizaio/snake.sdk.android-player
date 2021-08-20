package com.uiza.sdk.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
class UZPlaybackInfo(appId: String, entityId: String, entitySource: String) : Serializable {

    @JsonProperty("app_id")
    var appId: String = appId
        private set

    @JsonProperty("entity_id")
    var entityId: String = entityId
        private set

    @JsonProperty("entity_source")
    var entitySource: String = entitySource
        private set

}
