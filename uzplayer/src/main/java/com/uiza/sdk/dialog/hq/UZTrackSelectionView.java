package com.uiza.sdk.dialog.hq;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider;
import com.google.android.exoplayer2.ui.TrackNameProvider;
import com.google.android.exoplayer2.util.Assertions;
import com.uiza.sdk.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * A view for making track selections.
 */
public class UZTrackSelectionView extends LinearLayout {
    private final String TAG = getClass().getSimpleName();
    private final int selectableItemBackgroundResourceId;
    private final LayoutInflater inflater;
    private final CheckedTextView disableView;
    private final CheckedTextView defaultView;
    private final ComponentListener componentListener;

    private boolean allowAdaptiveSelections;

    private TrackNameProvider trackNameProvider;
    private CheckedTextView[][] trackViews;

    private DefaultTrackSelector trackSelector;
    private int rendererIndex;
    private TrackGroupArray trackGroups;
    private boolean isDisabled;
    private @Nullable
    SelectionOverride override;
    private List<UZItem> uzItemList = new ArrayList<>();
    private Callback callback;

    public UZTrackSelectionView(@NonNull Context context) {
        this(context, null);
    }

    public UZTrackSelectionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("nullness")
    public UZTrackSelectionView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributeArray = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});
        selectableItemBackgroundResourceId = attributeArray.getResourceId(0, 0);
        attributeArray.recycle();

        inflater = LayoutInflater.from(context);
        componentListener = new UZTrackSelectionView.ComponentListener();
        trackNameProvider = new DefaultTrackNameProvider(getResources());

        // View for disabling the renderer.
        disableView = (CheckedTextView) inflater.inflate(android.R.layout.simple_list_item_single_choice, this, false);
        disableView.setBackgroundResource(selectableItemBackgroundResourceId);
        disableView.setText(R.string.exo_track_selection_none);
        disableView.setEnabled(false);
        disableView.setFocusable(true);
        disableView.setOnClickListener(componentListener);
        disableView.setVisibility(View.GONE);
        disableView.setSoundEffectsEnabled(false);
        addView(disableView);
        // Divider view.
        addView(inflater.inflate(R.layout.exo_list_divider, this, false));
        // View for clearing the override to allow the selector to use its default selection logic.
        defaultView = (CheckedTextView) inflater.inflate(android.R.layout.simple_list_item_single_choice, this, false);
        defaultView.setBackgroundResource(selectableItemBackgroundResourceId);
        defaultView.setText(R.string.exo_track_selection_auto);
        defaultView.setEnabled(false);
        defaultView.setFocusable(true);
        defaultView.setOnClickListener(componentListener);
        defaultView.setSoundEffectsEnabled(false);
        addView(defaultView);

        UZItem uzItemDisableView = UZItem.create();
        uzItemDisableView.setCheckedTextView(disableView);
        uzItemDisableView.setDescription(disableView.getText().toString());
        uzItemList.add(uzItemDisableView);

        UZItem uzItemDefaultView = UZItem.create();
        uzItemDefaultView.setCheckedTextView(defaultView);
        uzItemDefaultView.setDescription(defaultView.getText().toString());
        uzItemList.add(uzItemDefaultView);
    }

    /**
     * Gets a pair consisting of a dialog and the {@link com.google.android.exoplayer2.ui.TrackSelectionView} that will be shown by it.
     *
     * @param context       The parent activity.
     * @param title         The dialog's title.
     * @param trackSelector The track selector.
     * @param rendererIndex The index of the renderer.
     * @return The dialog and the {@link com.google.android.exoplayer2.ui.TrackSelectionView} that will be shown by it.
     */
    public static Pair<AlertDialog, UZTrackSelectionView> getDialog(
            @NonNull Context context,
            CharSequence title,
            DefaultTrackSelector trackSelector,
            int rendererIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Inflate with the builder's context to ensure the correct style is used.
        LayoutInflater dialogInflater = LayoutInflater.from(builder.getContext());
        @SuppressLint("InflateParams") View dialogView = dialogInflater.inflate(R.layout.uz_track_selection_dialog, null);
        final UZTrackSelectionView selectionView = dialogView.findViewById(R.id.uz_track_selection_view);
        selectionView.init(trackSelector, rendererIndex);
        AlertDialog dialog = builder
                .setTitle(title)
                .setView(dialogView)
                .create();
        return Pair.create(dialog, selectionView);
    }

    private static int[] getTracksAdding(int[] tracks, int addedTrack) {
        tracks = Arrays.copyOf(tracks, tracks.length + 1);
        tracks[tracks.length - 1] = addedTrack;
        return tracks;
    }

    private static int[] getTracksRemoving(int[] tracks, int removedTrack) {
        int[] newTracks = new int[tracks.length - 1];
        int trackCount = 0;
        for (int track : tracks) {
            if (track != removedTrack) {
                newTracks[trackCount++] = track;
            }
        }
        return newTracks;
    }

    public List<UZItem> getUZItemList() {
        return uzItemList;
    }

    /**
     * Sets whether adaptive selections (consisting of more than one track) can be made using this
     * selection view.
     *
     * <p>For the view to enable adaptive selection it is necessary both for this feature to be
     * enabled, and for the target renderer to support adaptation between the available tracks.
     *
     * @param allowAdaptiveSelections Whether adaptive selection is enabled.
     */
    public void setAllowAdaptiveSelections(boolean allowAdaptiveSelections) {
        if (this.allowAdaptiveSelections != allowAdaptiveSelections) {
            this.allowAdaptiveSelections = allowAdaptiveSelections;
            updateViews();
        }
    }

    // Private methods.

    /**
     * Sets whether an option is available for disabling the renderer.
     *
     * @param showDisableOption Whether the disable option is shown.
     */
    public void setShowDisableOption(boolean showDisableOption) {
        disableView.setVisibility(showDisableOption ? View.VISIBLE : View.GONE);
    }

    /**
     * Sets the {@link TrackNameProvider} used to generate the user visible name of each track and
     * updates the view with track names queried from the specified provider.
     *
     * @param trackNameProvider The {@link TrackNameProvider} to use.
     */
    public void setTrackNameProvider(TrackNameProvider trackNameProvider) {
        this.trackNameProvider = Assertions.checkNotNull(trackNameProvider);
        updateViews();
    }

    /**
     * Initialize the view to select tracks for a specified renderer using a {@link
     * DefaultTrackSelector}.
     *
     * @param trackSelector The {@link DefaultTrackSelector}.
     * @param rendererIndex The index of the renderer.
     */
    public void init(DefaultTrackSelector trackSelector, int rendererIndex) {
        this.trackSelector = trackSelector;
        this.rendererIndex = rendererIndex;
        updateViews();
    }

    private void updateViews() {
        // Remove previous per-track views.
        for (int i = getChildCount() - 1; i >= 3; i--) {
            removeViewAt(i);
        }

        MappingTrackSelector.MappedTrackInfo trackInfo = trackSelector == null ? null : trackSelector.getCurrentMappedTrackInfo();
        if (trackSelector == null || trackInfo == null) {
            // The view is not initialized.
            disableView.setEnabled(false);
            defaultView.setEnabled(false);
            return;
        }
        disableView.setEnabled(true);
        defaultView.setEnabled(true);

        trackGroups = trackInfo.getTrackGroups(rendererIndex);

        DefaultTrackSelector.Parameters parameters = trackSelector.getParameters();
        isDisabled = parameters.getRendererDisabled(rendererIndex);
        override = parameters.getSelectionOverride(rendererIndex, trackGroups);

        // Add per-track views.
        trackViews = new CheckedTextView[trackGroups.length][];
        for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
            TrackGroup group = trackGroups.get(groupIndex);
            boolean enableAdaptiveSelections =
                    allowAdaptiveSelections
                            && trackGroups.get(groupIndex).length > 1
                            && trackInfo.getAdaptiveSupport(rendererIndex, groupIndex, false)
                            != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED;

            trackViews[groupIndex] = new CheckedTextView[group.length];
            for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                if (trackIndex == 0) {
                    addView(inflater.inflate(R.layout.exo_list_divider, this, false));
                }
                int trackViewLayoutId =
                        enableAdaptiveSelections
                                ? android.R.layout.simple_list_item_multiple_choice
                                : android.R.layout.simple_list_item_single_choice;
                CheckedTextView trackView = (CheckedTextView) inflater.inflate(trackViewLayoutId, this, false);
                trackView.setSoundEffectsEnabled(false);
                trackView.setBackgroundResource(selectableItemBackgroundResourceId);
                Format f = group.getFormat(trackIndex);
                UZItem uzItem = UZItem.create(f, parserNameProvider(f));
                trackView.setText(uzItem.getDescription());
                if (trackInfo.getTrackSupport(rendererIndex, groupIndex, trackIndex) == RendererCapabilities.FORMAT_HANDLED) {
                    trackView.setFocusable(true);
                    trackView.setTag(Pair.create(groupIndex, trackIndex));
                    trackView.setOnClickListener(componentListener);
                } else {
                    trackView.setFocusable(false);
                    trackView.setEnabled(false);
                }
                trackViews[groupIndex][trackIndex] = trackView;
                addView(trackView);
                uzItem.setCheckedTextView(trackView);
                uzItemList.add(uzItem);
            }
        }
        updateViewStates();
    }

    private String parserNameProvider(Format format) {
        if (format.frameRate == Format.NO_VALUE) {
            int minRes = Math.min(format.width, format.height);
            if (minRes < 0) {
                return trackNameProvider.getTrackName(format);
            }
            return String.format(Locale.getDefault(), "%dp", minRes);
        } else
            return String.format(Locale.getDefault(), "%dp (%.0f)", Math.min(format.width, format.height), format.frameRate);
    }

    private void updateViewStates() {
        disableView.setChecked(isDisabled);
        defaultView.setChecked(!isDisabled && override == null);
        for (int i = 0; i < trackViews.length; i++) {
            for (int j = 0; j < trackViews[i].length; j++) {
                trackViews[i][j].setChecked(override != null && override.groupIndex == i && override.containsTrack(j));
            }
        }

        //scroll to
        if (disableView.isChecked())
            scrollTo(disableView);
        if (defaultView.isChecked())
            scrollTo(defaultView);
        for (CheckedTextView[] trackView : trackViews) {
            for (CheckedTextView checkedTextView : trackView) {
                if (checkedTextView.isChecked()) {
                    scrollTo(checkedTextView);
                }
            }
        }
    }

    private void scrollTo(final CheckedTextView checkedTextView) {
        if (this.getParent() instanceof ScrollView) {
            final ScrollView sv = (ScrollView) this.getParent();
            if (sv == null) {
                return;
            }
            sv.postDelayed(() -> sv.scrollTo(0, checkedTextView.getTop()), 100);
        }
    }

    private void applySelection() {
        DefaultTrackSelector.ParametersBuilder parametersBuilder = trackSelector.buildUponParameters();
        parametersBuilder.setRendererDisabled(rendererIndex, isDisabled);
        if (override != null) {
            parametersBuilder.setSelectionOverride(rendererIndex, trackGroups, override);
        } else {
            parametersBuilder.clearSelectionOverrides(rendererIndex);
        }
        trackSelector.setParameters(parametersBuilder);
    }

    private void onClick(View view) {
        if (view == disableView) {
            onDisableViewClicked();
        } else if (view == defaultView) {
            onDefaultViewClicked();
        } else {
            onTrackViewClicked(view);
        }
        updateViewStates();
        applySelection();
        if (callback != null) {
            callback.onClick();
        }
    }

    private void onDisableViewClicked() {
        isDisabled = true;
        override = null;
    }

    private void onDefaultViewClicked() {
        isDisabled = false;
        override = null;
    }

    private void onTrackViewClicked(View view) {
        isDisabled = false;
        @SuppressWarnings("unchecked")
        Pair<Integer, Integer> tag = (Pair<Integer, Integer>) view.getTag();
        int groupIndex = tag.first;
        int trackIndex = tag.second;
        if (override == null || override.groupIndex != groupIndex || !allowAdaptiveSelections) {
            // A new override is being started.
            override = new SelectionOverride(groupIndex, trackIndex);
        } else {
            // An existing override is being modified.
            int overrideLength = override.length;
            int[] overrideTracks = override.tracks;
            if (((CheckedTextView) view).isChecked()) {
                // Remove the track from the override.
                if (overrideLength == 1) {
                    // The last track is being removed, so the override becomes empty.
                    override = null;
                    isDisabled = true;
                } else {
                    int[] tracks = getTracksRemoving(overrideTracks, trackIndex);
                    override = new SelectionOverride(groupIndex, tracks);
                }
            } else {
                int[] tracks = getTracksAdding(overrideTracks, trackIndex);
                override = new SelectionOverride(groupIndex, tracks);
            }
        }
        //applySelection();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onClick();
    }

    // Internal classes.
    private class ComponentListener implements OnClickListener {

        @Override
        public void onClick(View view) {
            UZTrackSelectionView.this.onClick(view);
        }
    }
}
