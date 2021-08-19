package com.uiza.sdk.dialog.playlistfolder

import com.uiza.sdk.models.UZPlayback

interface CallbackPlaylistFolder {
    fun onClickItem(playback: UZPlayback, position: Int) {}
    fun onFocusChange(playback: UZPlayback, position: Int) {}
    fun onDismiss() {}
}