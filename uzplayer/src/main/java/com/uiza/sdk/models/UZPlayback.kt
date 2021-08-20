package com.uiza.sdk.models

import android.os.Parcel
import android.os.Parcelable
import java.net.MalformedURLException
import java.net.URL
import java.util.*

open class UZPlayback : Parcelable {

    companion object CREATOR : Parcelable.Creator<UZPlayback> {
        override fun createFromParcel(parcel: Parcel): UZPlayback {
            return UZPlayback(parcel)
        }

        override fun newArray(size: Int): Array<UZPlayback?> {
            return arrayOfNulls(size)
        }
    }

    var id: String? = null
    var name: String? = null
    var description: String? = null
        private set
    var duration = 0f
        private set
    var poster: String? = null
    var createdAt: Date? = null
        private set
    private var linkPlays = ArrayList<String>()

    constructor()

    constructor(id: String?, name: String?, description: String?, poster: String?) {
        this.id = id
        this.name = name
        this.description = description
        this.poster = poster
    }

    protected constructor(parcel: Parcel) {
        id = parcel.readString()
        name = parcel.readString()
        description = parcel.readString()
        poster = parcel.readString()
        duration = parcel.readFloat()
        createdAt = Date(parcel.readLong())
        parcel.readStringList(linkPlays)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(description)
        dest.writeString(poster)
        dest.writeFloat(duration)
        dest.writeLong(createdAt?.time ?: 0)
        dest.writeStringList(linkPlays)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun canPlay(): Boolean {
        return linkPlays.isNotEmpty()
    }

    fun addLinkPlay(linkPlay: String) {
        if (linkPlay.isNotEmpty()) {
            linkPlays.add(linkPlay)
        }
    }

    fun getLinkPlays(): List<String> {
        return linkPlays
    }

    val size: Int
        get() = linkPlays.size

    fun getLinkPlay(pos: Int): String? {
        return if (linkPlays.isEmpty() || pos >= linkPlays.size) {
            null
        } else {
            linkPlays[pos]
        }
    }

    /**
     * default: dash -> hls -> single file
     *
     * @return string of url
     */
    val firstLinkPlay: String?
        get() = if (linkPlays.isEmpty()) {
            null
        } else {
            linkPlays[0]
        }

    val firstPlayUrl: URL?
        get() = try {
            URL(firstLinkPlay)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            null
        }

    override fun toString(): String {
        return "UZPlayback(id: $id, name: $name, description: $description, poster: $poster)"
    }
}
