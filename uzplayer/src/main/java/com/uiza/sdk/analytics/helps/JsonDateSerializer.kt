package com.uiza.sdk.analytics.helps

import com.fasterxml.jackson.databind.JsonSerializer
import kotlin.Throws
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import org.joda.time.DateTime
import java.io.IOException
import java.util.*

class JsonDateSerializer : JsonSerializer<Date?>() {

    companion object {
        private const val DATE_TIME_FMT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }

    @Throws(IOException::class)
    override fun serialize(value: Date?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value != null) {
            gen.writeString(DateTime(value.time).toString(DATE_TIME_FMT))
        }
    }

}
