package com.uiza.sdk.interfaces;

import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;

public interface UZAdPlayerCallback {
    default void onPlay() {
    }

    default void onVolumeChanged(int i) {
    }

    default void onAdProgress(VideoProgressUpdate videoProgressUpdate) {
    }

    default void onPause() {
    }

    default void onLoaded() {
    }

    default void onResume() {
    }

    default void onEnded() {
    }

    default void onError() {
    }

    default void onBuffering() {
    }
}
