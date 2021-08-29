package com.uiza.sampleplayer.ui.playerpip;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.uiza.sampleplayer.R;
import com.uiza.sampleplayer.app.Constant;
import com.uiza.sampleplayer.app.UZApplication;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.view.UZVideoView;

import io.reactivex.disposables.CompositeDisposable;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Demo UZPlayer with Picture In Picture
 */
public class PlayerPipActivity extends AppCompatActivity {

    private UZVideoView uzVideo;
    private EditText etLinkPlay;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CompositeDisposable disposables;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_player_pip);
        uzVideo = findViewById(R.id.uzVideoView);
        etLinkPlay = findViewById(R.id.etLinkPlay);
        uzVideo.setPIPModeEnabled(true);
        uzVideo.setOnScreenRotate(new Function1<Boolean, Unit>() {
            @Override
            public Unit invoke(Boolean isLandscape) {
                if (!isLandscape) {
                    int w = UZViewUtils.getScreenWidth();
                    int h = w * 9 / 16;
                    uzVideo.setFreeSize(false);
                    uzVideo.setSize(w, h);
                }
                return null;
            }
        });
        // If linkplay is livestream, it will auto move to live edge when onResume is called
        uzVideo.setAutoMoveToLiveEdge(true);
        UZPlayback playbackInfo = null;
        if (getIntent() != null) {
            playbackInfo = getIntent().getParcelableExtra(Constant.EXTRA_PLAYBACK_INFO);
        }
        if (playbackInfo != null)
            etLinkPlay.setText(playbackInfo.getFirstLinkPlay());
        else
            etLinkPlay.setText(UZApplication.urls[0]);

//        etLinkPlay.setText("http://worker-live.uizadev.io/stream/app_id/entity_id/master.m3u8");

        findViewById(R.id.btnPlay).setOnClickListener(view -> onPlay());
        disposables = new CompositeDisposable();
        (new Handler()).postDelayed(this::onPlay, 100);
    }

    private void onPlay() {
        final UZPlayback playback = new UZPlayback();
        playback.addLinkPlay(etLinkPlay.getText().toString());
        uzVideo.play(playback);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        uzVideo.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        uzVideo.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uzVideo.onDestroyView();
        if (disposables != null)
            disposables.dispose();
        handler = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        uzVideo.onResumeView();
    }

    @Override
    public void onPause() {
        super.onPause();
        uzVideo.onPauseView();
    }

    @Override
    public void onBackPressed() {
        if (!uzVideo.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (newConfig != null) {
            uzVideo.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        try {
            if (!uzVideo.isLandscapeScreen()) {
                uzVideo.enterPIPMode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}
