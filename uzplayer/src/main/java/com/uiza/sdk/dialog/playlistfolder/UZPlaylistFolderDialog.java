package com.uiza.sdk.dialog.playlistfolder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uiza.sdk.R;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZData;
import com.uiza.sdk.widget.recyclerview.SnapType;
import com.uiza.sdk.widget.recyclerview.SnappyLinearLayoutManager;

import java.util.List;

public class UZPlaylistFolderDialog extends Dialog {
    private final Context context;
    private AlertDialog dialog;
    private RecyclerView recyclerView;
    private final List<UZPlayback> playList;
    private final int currentPositionOfDataList;
    private final CallbackPlaylistFolder callbackPlaylistFolder;

    public UZPlaylistFolderDialog(@NonNull Context context, boolean isLandscape, List<UZPlayback> playList, int currentPositionOfDataList, CallbackPlaylistFolder callbackPlaylistFolder) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.context = context;
        this.playList = playList;
        this.currentPositionOfDataList = currentPositionOfDataList;
        this.callbackPlaylistFolder = callbackPlaylistFolder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_list_playlist_folder);
        recyclerView = findViewById(R.id.recycler_view);
        final ImageButton btExit = findViewById(R.id.bt_exit);
        btExit.setOnClickListener(v ->
                dismiss()
        );
        btExit.setOnFocusChangeListener((view, isFocus) -> {
            if (isFocus) {
                btExit.setColorFilter(Color.WHITE);
                btExit.setBackgroundColor(Color.BLACK);
            } else {
                btExit.setBackgroundColor(Color.TRANSPARENT);
                btExit.setColorFilter(Color.BLACK);
            }
        });
        setupUI();
    }

    private void setupUI() {
        SnappyLinearLayoutManager layoutManager = new SnappyLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setSnapType(SnapType.CENTER);
        layoutManager.setSnapInterpolator(new DecelerateInterpolator());
        recyclerView.setLayoutManager(layoutManager);
        AdapterPlaylistFolder adapterPlaylistFolder = new AdapterPlaylistFolder(context, playList, currentPositionOfDataList, new CallbackPlaylistFolder() {
            @Override
            public void onClickItem(@NonNull UZPlayback data, int position) {
                if (UZData.getInstance().isSettingPlayer()) {
                    return;
                }
                dismiss();
                if (callbackPlaylistFolder != null) {
                    callbackPlaylistFolder.onClickItem(data, position);
                }
            }

            @Override
            public void onFocusChange(@NonNull UZPlayback data, int position) {
                if (recyclerView != null) {
                    recyclerView.smoothScrollToPosition(position);
                }
            }

            @Override
            public void onDismiss() {
                if (callbackPlaylistFolder != null) {
                    callbackPlaylistFolder.onDismiss();
                }
            }
        });
        recyclerView.setAdapter(adapterPlaylistFolder);
        recyclerView.scrollToPosition(currentPositionOfDataList);
        recyclerView.requestFocus();

        recyclerView.postDelayed(() -> {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(currentPositionOfDataList);
            if (holder == null) {
                return;
            }
            holder.itemView.requestFocus();
        }, 50);
    }
}