package com.uiza.sdk.utils

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Base64
import com.uiza.sdk.models.UZPlaybackInfo
import com.uiza.sdk.utils.JacksonUtils.fromJson
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.pow

object StringUtils {
    /**
     * Email validation pattern.
     */
    private val EMAIL_PATTERN =
        Pattern.compile("^[_A-Za-z0-9-]+(\\\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\\\.[A-Za-z0-9]+)*(\\\\.[A-Za-z]{2,})$")

    /**
     * Validates if the given input is a valid email address.
     *
     * @param email The email to validate.
     * @return `true` if the input is a valid email. `false` otherwise.
     */
    fun isEmailValid(email: CharSequence?): Boolean {
        return email != null && EMAIL_PATTERN.matcher(email).matches()
    }

    /**
     * convert html to plain text
     *
     * @param htmlText : html String
     * @return plain text
     */
    fun htmlToPlainText(htmlText: String?): String {
        val spanned: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(htmlText)
        }
        val chars = CharArray(spanned.length)
        TextUtils.getChars(spanned, 0, spanned.length, chars, 0)
        return String(chars)
    }

    /**
     * Convert UTC time string to long value
     *
     * @param timeStr the time with format `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`
     * @return UTC time as long value
     */
    fun convertUTCMs(timeStr: String): Long {
        if (TextUtils.isEmpty(timeStr)) return -1
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return try {
            val date = dateFormat.parse(timeStr)
            date?.time ?: -1
        } catch (e: ParseException) {
            -1
        }
    }

    fun convertSecondsToHMmSs(seconds: Long): String {
        if (seconds <= 0) {
            return "0:00"
        }
        val s = seconds % 60
        val m = seconds / 60 % 60
        val h = seconds / (60 * 60) % 24
        return if (h == 0L) {
            String.format(Locale.getDefault(), "%d:%02d", m, s)
        } else {
            String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
        }
    }

    @JvmStatic
    fun convertMlsecondsToHMmSs(mls: Long): String {
        return convertSecondsToHMmSs(mls / 1000)
    }

    @JvmStatic
    fun groupingSeparatorLong(value: Long): String {
        val decimalFormatSymbols = DecimalFormatSymbols()
        decimalFormatSymbols.groupingSeparator = ','
        val decimalFormat = DecimalFormat("###,###", decimalFormatSymbols)
        return decimalFormat.format(value)
    }

    @JvmStatic
    fun doubleFormatted(value: Double, precision: Int): String {
        return DecimalFormat(
            "#0." + if (precision <= 1) "0" else if (precision == 2) "00" else "000"
        ).format(value)
    }

    @JvmStatic
    fun humanReadableByteCount(bytes: Long, si: Boolean, isBits: Boolean): String {
        val unit = if (!si) 1000 else 1024
        if (bytes < unit) return "$bytes KB"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        return if (isBits) String.format(
            Locale.getDefault(),
            "%.1f %sb",
            bytes / unit.toDouble().pow(exp.toDouble()),
            pre
        ) else String.format(
            Locale.getDefault(),
            "%.1f %sB",
            bytes / unit.toDouble().pow(exp.toDouble()),
            pre
        )
    }

    @Throws(Exception::class)
    fun parserJsonInfo(url: String): String? {
        val fromIndex = url.indexOf("?cm=")
        if (fromIndex > 0) {
            val toIndex = url.indexOf("&", fromIndex)
            val cm = if (toIndex > 0) url.substring(fromIndex + 4, toIndex) else url.substring(
                fromIndex + 4
            )
            return String(Base64.decode(cm, Base64.DEFAULT))
        }
        return null
    }

    @JvmStatic
    fun parserInfo(linkPlay: String): UZPlaybackInfo? {
        try {
            val json = parserJsonInfo(linkPlay)
            if (json != null) return fromJson(json = json, classOfT = UZPlaybackInfo::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
