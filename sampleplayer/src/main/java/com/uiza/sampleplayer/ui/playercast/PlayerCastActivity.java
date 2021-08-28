package com.uiza.sampleplayer.ui.playercast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

import com.uiza.sampleplayer.R;
import com.uiza.sampleplayer.app.UZApplication;
import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.interfaces.UZPlayerCallback;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;

public class PlayerCastActivity extends AppCompatActivity implements UZPlayerCallback, UZPlayerView.OnSingleTap {
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
        uzVideo.setPlayerCallback(this);
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
    public void onScreenRotate(boolean isLandscape) {
        if (!isLandscape) {
            int w = UZViewUtils.getScreenWidth();
            int h = w * 9 / 16;
            uzVideo.setFreeSize(false);
            uzVideo.setSize(w, h);
        }
    }

    @Override
    public void onError(UZException e) {
        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onTimeShiftChange(boolean timeShiftOn) {

    }
}
