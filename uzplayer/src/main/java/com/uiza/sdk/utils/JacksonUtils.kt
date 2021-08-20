package com.uiza.sdk.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import timber.log.Timber
import java.io.IOException
import java.io.Reader

/**
 * Utility class for converting objects to and from JSON using Jackson implementation.
 *
 * @author namnd
 * @see ObjectMapper
 */
object JacksonUtils {
    private val mapper = ObjectMapper()

    /**
     * @param <T> the type of the desired object
     * @param src the Object for which JSON representation is to be created
     * @return Json representation of `list`
    </T> */
    fun <T> toJson(src: T): String {
        return try {
            mapper.writeValueAsString(src)
        } catch (e: JsonProcessingException) {
            ""
        }
    }

    /**
     * [ObjectMapper.readValue]
     *
     * @param <T>      the type of the desired object
     * @param json     the string from which the object is to be deserialized
     * @param classOfT the class of T
     * @return an object of type T from the string. Returns `null` if
     * `json` is `null`.
    </T> */
    fun <T> fromJson(json: String, classOfT: Class<T>): T? {
        return try {
            mapper.readValue(json, classOfT)
        } catch (e: JsonProcessingException) {
            Timber.e(e)
            null
        }
    }

    /**
     * [ObjectMapper.readValue]
     *
     * @param <T>      the type of the desired object
     * @param reader   the Reader from which the object is to be deserialized
     * @param classOfT the class of T
     * @return an object of type T from the string. Returns `null` if
     * `json` is `null`.
    </T> */
    fun <T> fromJson(reader: Reader, classOfT: Class<T>): T? {
        return try {
            mapper.readValue(reader, classOfT)
        } catch (e: IOException) {
            null
        }
    }

    /**
     * [ObjectMapper.readValue]
     *
     * @param <T>  the type of the desired object
     * @param json the string from which the object is to be deserialized
     * @return an List of type T from the string. Returns `Collections#emptyList` if
     * `json` is `null`.
    </T> */
    fun <T> fromJson(json: String): List<T> {
        return try {
            mapper.readValue(json, object : TypeReference<List<T>>() {})
        } catch (e: JsonProcessingException) {
            emptyList()
        }
    }

    /**
     * [ObjectMapper.readValue]
     *
     * @param <T>    the type of the desired object
     * @param reader the Reader from which the object is to be deserialized
     * @return an List of type T from the string. Returns `Collections#emptyList` if
     * `json` is `null`.
    </T> */
    fun <T> fromJson(reader: Reader): List<T> {
        return try {
            mapper.readValue(reader, object : TypeReference<List<T>>() {})
        } catch (e: IOException) {
            emptyList()
        }
    }
}
