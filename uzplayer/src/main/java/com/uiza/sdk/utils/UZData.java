package com.uiza.sdk.utils;

import android.content.pm.ResolveInfo;
import android.util.Log;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.uiza.sdk.R;
import com.uiza.sdk.chromecast.Casty;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.models.UZPlaybackInfo;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class UZData {

    private String logTag = getClass().getSimpleName();
    @LayoutRes
    private int uzPlayerSkinLayoutId = R.layout.uzplayer_skin_1;//id of layout xml
    private Casty casty;
    private UZPlayback playback;
    private String urlIMAAd = "";
    private ArrayList<UZPlayback> playList;
    private UZPlaybackInfo playbackInfo;
    private int currentPositionOfPlayList = 0;
    private boolean useUZDragView;
    private List<ResolveInfo> resolveInfoList;
    private boolean settingPlayer;

    private UZData() {
    }

    // Bill Pugh Singleton Implementation
    private static class UDataHelper {
        private static final UZData INSTANCE = new UZData();
    }

    public static UZData getInstance() {
        return UDataHelper.INSTANCE;
    }

    public int getUZPlayerSkinLayoutId() {
        return uzPlayerSkinLayoutId;
    }

    public void setUZPlayerSkinLayoutId(@LayoutRes int uzPlayerSkinLayoutId) {
        this.uzPlayerSkinLayoutId = uzPlayerSkinLayoutId;
    }

    @Nullable
    public Casty getCasty() {
        if (casty == null) {
            Log.e(logTag, "You must init Casty with activity before using Chromecast. Tips: put 'UZPlayer.setCasty(this);' to your onStart() or onCreate()");
        }
        return casty;
    }

    public void setCasty(Casty casty) {
        this.casty = casty;
    }

    public UZPlayback getPlayback() {
        return playback;
    }

    public String getPosterUrl() {
        return (playback != null) ? playback.getPoster() : null;
    }

    public UZPlaybackInfo getPlaybackInfo() {
        return this.playbackInfo;
    }

    public void setPlayback(@NonNull UZPlayback playback) {
        this.playback = playback;
        String firstLinkPlay = playback.getFirstLinkPlay();
        if (firstLinkPlay != null) {
            this.playbackInfo = StringUtils.parserInfo(firstLinkPlay);
        }
    }

    public String getUrlIMAAd() {
        return urlIMAAd;
    }

    public void setUrlIMAAd(String urlIMAAd) {
        this.urlIMAAd = urlIMAAd;
    }

    public void clear() {
        this.playback = null;
        this.playbackInfo = null;
        this.urlIMAAd = null;
    }

    @Nullable
    public String getEntityId() {
        return (playback == null) ? null : playback.getId();
    }

    @Nullable
    public String getEntityName() {
        return (playback == null) ? null : playback.getName();
    }

    @Nullable
    public String getHost() {
        if (playback == null) return null;
        URL url = playback.getFirstPlayUrl();
        if (url == null) return null;
        return url.getHost();
    }

    public boolean isPlayWithPlaylistFolder() {
        return playList != null;
    }

    public ArrayList<UZPlayback> getPlayList() {
        return playList;
    }

    public void setPlayList(ArrayList<UZPlayback> playlist) {
        this.playList = playlist;
    }

    public int getCurrentPositionOfPlayList() {
        return currentPositionOfPlayList;
    }

    public void setCurrentPositionOfPlayList(int currentPositionOfPlayList) {
        this.currentPositionOfPlayList = currentPositionOfPlayList;
        UZPlayback currentPlayback = playList.get(currentPositionOfPlayList);
        if (currentPlayback != null) {
            this.playback = currentPlayback;
            String firstLinkPlay = playback.getFirstLinkPlay();
            if (firstLinkPlay != null) {
                this.playbackInfo = StringUtils.parserInfo(firstLinkPlay);
            }
        }
    }

    public void clearDataForPlaylistFolder() {
        playList = null;
        currentPositionOfPlayList = 0;
    }

    public boolean isUseUZDragView() {
        return useUZDragView;
    }

    public void setUseWithUZDragView(boolean useUZDragView) {
        this.useUZDragView = useUZDragView;
    }

    public List<ResolveInfo> getResolveInfoList() {
        return resolveInfoList;
    }

    public void setResolveInfoList(List<ResolveInfo> resolveInfoList) {
        this.resolveInfoList = resolveInfoList;
    }

    public boolean isSettingPlayer() {
        return this.settingPlayer;
    }

    public void setSettingPlayer(boolean settingPlayer) {
        this.settingPlayer = settingPlayer;
    }

}
