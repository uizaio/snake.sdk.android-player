package com.uiza.sdk.dialog.playlistfolder;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.uiza.sdk.R;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.ImageUtils;
import com.uiza.sdk.utils.UZViewUtils;

import java.util.List;

public class AdapterPlaylistFolder extends RecyclerView.Adapter<AdapterPlaylistFolder.PlayListHolder> {
    private final List<UZPlayback> playList;
    private final CallbackPlaylistFolder callbackPlaylistFolder;

    public AdapterPlaylistFolder(@NonNull Context context, List<UZPlayback> playList, int currentPositionOfDataList, CallbackPlaylistFolder callbackPlaylistFolder) {
        this.playList = playList;
        this.callbackPlaylistFolder = callbackPlaylistFolder;
    }

    @Override
    @NonNull
    public PlayListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_playlist_folder, parent, false);
        return new PlayListHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlayListHolder playListHolder, final int position) {
        final UZPlayback data = playList.get(position);
        UZViewUtils.setTextDuration(playListHolder.tvDuration, String.valueOf(data.getDuration()));
        playListHolder.tvName.setText(data.getName());

        UZViewUtils.setTextDuration(playListHolder.tvDuration2, String.valueOf(data.getDuration()));
        if (TextUtils.isEmpty(data.getDescription())) {
            playListHolder.tvDescription.setVisibility(View.GONE);
        } else {
            playListHolder.tvDescription.setText(data.getDescription());
            playListHolder.tvDescription.setVisibility(View.VISIBLE);
        }

        ImageUtils.Companion.loadThumbnail(playListHolder.ivCover, data.getPoster());

        playListHolder.rootView.setOnClickListener(v -> {
            if (callbackPlaylistFolder != null) {
                callbackPlaylistFolder.onClickItem(data, position);
            }
        });

        playListHolder.rootView.setOnFocusChangeListener((view, isFocus) -> {
            if (isFocus) {
                playListHolder.rootView.setBackgroundResource(R.drawable.bkg_item_playlist_folder);
            } else {
                playListHolder.rootView.setBackgroundResource(0);
            }
            if (callbackPlaylistFolder != null) {
                callbackPlaylistFolder.onFocusChange(data, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playList == null ? 0 : playList.size();
    }

    public static class PlayListHolder extends RecyclerView.ViewHolder {
        private final TextView tvDuration;
        private final TextView tvDuration2;
        private final ImageView ivCover;
        private final TextView tvName;
        private final TextView tvDescription;
        private final CardView rootView;

        public PlayListHolder(View view) {
            super(view);
            rootView = view.findViewById(R.id.rootView);
            tvDuration = view.findViewById(R.id.tvDuration);
            tvDuration2 = view.findViewById(R.id.tvDuration2);
            tvName = view.findViewById(R.id.tvName);
            tvDescription = view.findViewById(R.id.tvDescription);
            ivCover = view.findViewById(R.id.ivCover);
        }
    }
}
