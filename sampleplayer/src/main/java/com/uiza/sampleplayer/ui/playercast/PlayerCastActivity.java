package com.uiza.sampleplayer.ui.playercast;

import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.uiza.sampleplayer.R;
import com.uiza.sampleplayer.app.UZApplication;
import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class PlayerCastActivity extends AppCompatActivity implements UZPlayerView.OnSingleTap {
    private UZVideoView uzVideo;
    private EditText etLinkPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UZPlayer.setCasty(this);
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_cast);
        uzVideo = findViewById(R.id.uzVideoView);
        etLinkPlay = findViewById(R.id.etLinkPlay);
        UZPlayer.getCasty().setUpMediaRouteButton(findViewById(R.id.media_route_button));
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
        uzVideo.getPlayerView().setOnSingleTap(this);
        // If linkplay is livestream, it will auto move to live edge when onResume is called
        uzVideo.setAutoMoveToLiveEdge(true);
        etLinkPlay.setText(UZApplication.urls[0]);
        findViewById(R.id.btnPlay).setOnClickListener(view -> onPlay());
    }

    private void onPlay() {
        final UZPlayback playback = new UZPlayback();
        playback.setPoster(UZApplication.thumbnailUrl);
        playback.addLinkPlay(etLinkPlay.getText().toString());
        uzVideo.play(playback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        UZPlayer.getCasty().addMediaRouteMenuItem(menu);
        return true;
    }

    @Override
    public void onSingleTapConfirmed(float x, float y) {
        uzVideo.toggleShowHideController();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uzVideo.onDestroyView();
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
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
