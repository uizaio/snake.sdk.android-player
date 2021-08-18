package com.uiza.sampleplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnPlayer).setOnClickListener(v -> gotoActivity(PlayerActivity.class));
        findViewById(R.id.btnPipPlayer).setOnClickListener(v -> gotoActivity(PipPlayerActivity.class));
        findViewById(R.id.btnCastPlayer).setVisibility(View.GONE);
        findViewById(R.id.btnAnalytic).setOnClickListener(v -> gotoActivity(AnalyticActivity.class));
        //.setOnClickListener(v -> gotoActivity(CastPlayerActivity.class));
        ((AppCompatTextView) findViewById(R.id.txtVersion)).setText(String.format(Locale.getDefault(),
                "%s (%s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
    }

    private <T> void gotoActivity(Class<T> clazz) {
        startActivity(new Intent(MainActivity.this, clazz));
    }
}