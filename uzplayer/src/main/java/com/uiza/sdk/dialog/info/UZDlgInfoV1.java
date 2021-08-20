package com.uiza.sdk.dialog.info;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.uiza.sdk.R;
import com.uiza.sdk.utils.UZViewUtils;

public class UZDlgInfoV1 extends Dialog {
    private final String logTag = getClass().getSimpleName();
    private ProgressBar progressBar;
    private final String title;
    private final String msg;
    private final Handler handler = new Handler();

    public UZDlgInfoV1(@NonNull Context context, String title, String msg) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.title = title;
        this.msg = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlg_info);
        progressBar = findViewById(R.id.pb);
        UZViewUtils.setColorProgressBar(progressBar, ContextCompat.getColor(getContext(), R.color.colorPrimary));
        TextView tvTitle = findViewById(R.id.tv_title);
        final TextView tvContent = findViewById(R.id.tv_content);
        tvTitle.setText(title);
        handler.postDelayed(() -> {
            tvContent.setText(msg);
            progressBar.setVisibility(View.GONE);
            tvContent.setVisibility(View.VISIBLE);
        }, 1000);
    }
}