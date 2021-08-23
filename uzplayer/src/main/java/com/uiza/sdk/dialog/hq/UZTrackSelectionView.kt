package com.uiza.sdk.dialog.hq

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckedTextView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.annotation.AttrRes
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.RendererCapabilities
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.google.android.exoplayer2.ui.TrackNameProvider
import com.google.android.exoplayer2.util.Assertions
import com.uiza.sdk.R
import java.util.*
import kotlin.math.min

/**
 * A view for making track selections.
 */
class UZTrackSelectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // Internal classes.
    private inner class ComponentListener : OnClickListener {
        override fun onClick(view: View) {
            this@UZTrackSelectionView.onClick(view)
        }
    }

    companion object {
        /**
         * Gets a pair consisting of a dialog and the [com.google.android.exoplayer2.ui.TrackSelectionView] that will be shown by it.
         *
         * @param context       The parent activity.
         * @param title         The dialog's title.
         * @param trackSelector The track selector.
         * @param rendererIndex The index of the renderer.
         * @return The dialog and the [com.google.android.exoplayer2.ui.TrackSelectionView] that will be shown by it.
         */
        @JvmStatic
        fun getDialog(
            context: Context,
            title: CharSequence?,
            trackSelector: DefaultTrackSelector?,
            rendererIndex: Int
        ): Pair<AlertDialog, UZTrackSelectionView> {
            val builder = AlertDialog.Builder(context)

            // Inflate with the builder's context to ensure the correct style is used.
            val dialogInflater = LayoutInflater.from(builder.context)

            @SuppressLint("InflateParams")
            val dialogView = dialogInflater.inflate(R.layout.uz_track_selection_dialog, null)
            val uzTrackSelectionView: UZTrackSelectionView =
                dialogView.findViewById(R.id.uzTrackSelectionView)
            uzTrackSelectionView.init(trackSelector = trackSelector, rendererIndex = rendererIndex)
            val dialog = builder
                .setTitle(title)
                .setView(dialogView)
                .create()
            return Pair.create(dialog, uzTrackSelectionView)
        }

        private fun getTracksAdding(_tracks: IntArray, addedTrack: Int): IntArray {
            var tracks = _tracks
            tracks = tracks.copyOf(tracks.size + 1)
            tracks[tracks.size - 1] = addedTrack
            return tracks
        }

        private fun getTracksRemoving(tracks: IntArray, removedTrack: Int): IntArray {
            val newTracks = IntArray(tracks.size - 1)
            var trackCount = 0
            for (track in tracks) {
                if (track != removedTrack) {
                    newTracks[trackCount++] = track
                }
            }
            return newTracks
        }
    }

    private val selectableItemBackgroundResourceId: Int
    private val inflater: LayoutInflater
    private val disableView: CheckedTextView
    private val defaultView: CheckedTextView
    private val componentListener: ComponentListener = ComponentListener()
    private var allowAdaptiveSelections = false
    private var trackNameProvider: TrackNameProvider
    private var trackViews: Array<Array<CheckedTextView?>?>? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var rendererIndex = 0
    private var trackGroups: TrackGroupArray? = null
    private var isDisabled = false
    private var override: SelectionOverride? = null
    private val uzItemList: MutableList<UZItem> = ArrayList()
    private var callback: Callback? = null
    val uZItemList: List<UZItem>
        get() = uzItemList

    init {
        val attributeArray =
            context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        selectableItemBackgroundResourceId = attributeArray.getResourceId(0, 0)
        attributeArray.recycle()
        inflater = LayoutInflater.from(context)

        trackNameProvider = DefaultTrackNameProvider(resources)

        // View for disabling the renderer.
        disableView = inflater.inflate(
            android.R.layout.simple_list_item_single_choice,
            this,
            false
        ) as CheckedTextView
        disableView.setBackgroundResource(selectableItemBackgroundResourceId)
        disableView.setText(R.string.exo_track_selection_none)
        disableView.isEnabled = false
        disableView.isFocusable = true
        disableView.setOnClickListener(componentListener)
        disableView.visibility = GONE
        disableView.isSoundEffectsEnabled = false
        addView(disableView)

        // Divider view.
        addView(inflater.inflate(R.layout.exo_list_divider, this, false))

        // View for clearing the override to allow the selector to use its default selection logic.
        defaultView = inflater.inflate(
            android.R.layout.simple_list_item_single_choice,
            this,
            false
        ) as CheckedTextView
        defaultView.setBackgroundResource(selectableItemBackgroundResourceId)
        defaultView.setText(R.string.exo_track_selection_auto)
        defaultView.isEnabled = false
        defaultView.isFocusable = true
        defaultView.setOnClickListener(componentListener)
        defaultView.isSoundEffectsEnabled = false
        addView(defaultView)

        val uzItemDisableView = UZItem.create()
        uzItemDisableView.checkedTextView = disableView
        uzItemDisableView.description = disableView.text.toString()
        uzItemList.add(uzItemDisableView)

        val uzItemDefaultView = UZItem.create()
        uzItemDefaultView.checkedTextView = defaultView
        uzItemDefaultView.description = defaultView.text.toString()
        uzItemList.add(uzItemDefaultView)
    }

    /**
     * Sets whether adaptive selections (consisting of more than one track) can be made using this
     * selection view.
     *
     *
     * For the view to enable adaptive selection it is necessary both for this feature to be
     * enabled, and for the target renderer to support adaptation between the available tracks.
     *
     * @param allowAdaptiveSelections Whether adaptive selection is enabled.
     */
    fun setAllowAdaptiveSelections(allowAdaptiveSelections: Boolean) {
        if (this.allowAdaptiveSelections != allowAdaptiveSelections) {
            this.allowAdaptiveSelections = allowAdaptiveSelections
            updateViews()
        }
    }
    // Private methods.
    /**
     * Sets whether an option is available for disabling the renderer.
     *
     * @param showDisableOption Whether the disable option is shown.
     */
    fun setShowDisableOption(showDisableOption: Boolean) {
        disableView.visibility = if (showDisableOption) VISIBLE else GONE
    }

    /**
     * Sets the [TrackNameProvider] used to generate the user visible name of each track and
     * updates the view with track names queried from the specified provider.
     *
     * @param trackNameProvider The [TrackNameProvider] to use.
     */
    fun setTrackNameProvider(trackNameProvider: TrackNameProvider?) {
        this.trackNameProvider = Assertions.checkNotNull(trackNameProvider)
        updateViews()
    }

    /**
     * Initialize the view to select tracks for a specified renderer using a [ ].
     *
     * @param trackSelector The [DefaultTrackSelector].
     * @param rendererIndex The index of the renderer.
     */
    fun init(trackSelector: DefaultTrackSelector?, rendererIndex: Int) {
        this.trackSelector = trackSelector
        this.rendererIndex = rendererIndex
        updateViews()
    }

    private fun updateViews() {
        // Remove previous per-track views.
        for (i in childCount - 1 downTo 3) {
            removeViewAt(i)
        }
        val trackInfo = if (trackSelector == null) {
            null
        } else {
            trackSelector?.currentMappedTrackInfo
        }
        if (trackSelector == null || trackInfo == null) {
            // The view is not initialized.
            disableView.isEnabled = false
            defaultView.isEnabled = false
            return
        } else {
            disableView.isEnabled = true
            defaultView.isEnabled = true
            trackGroups = trackInfo.getTrackGroups(rendererIndex)
            trackSelector?.let { dts ->
                val parameters = dts.parameters
                isDisabled = parameters.getRendererDisabled(rendererIndex)
                override = parameters.getSelectionOverride(rendererIndex, trackGroups)

                // Add per-track views.
                trackGroups?.let { tga ->
                    trackViews = arrayOfNulls(tga.length)
                    trackViews?.let { a ->
                        for (groupIndex in 0 until tga.length) {
                            val group = tga.get(groupIndex)
                            val enableAdaptiveSelections =
                                (allowAdaptiveSelections && tga.get(groupIndex).length > 1 && (trackInfo.getAdaptiveSupport(
                                    rendererIndex,
                                    groupIndex,
                                    false
                                ) != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED))
                            a[groupIndex] = arrayOfNulls(group.length)
                            for (trackIndex in 0 until group.length) {
                                if (trackIndex == 0) {
                                    addView(
                                        inflater.inflate(R.layout.exo_list_divider, this, false)
                                    )
                                }
                                val trackViewLayoutId =
                                    if (enableAdaptiveSelections) {
                                        android.R.layout.simple_list_item_multiple_choice
                                    } else {
                                        android.R.layout.simple_list_item_single_choice
                                    }
                                val trackView =
                                    inflater.inflate(
                                        trackViewLayoutId,
                                        this,
                                        false
                                    ) as CheckedTextView
                                trackView.isSoundEffectsEnabled = false
                                trackView.setBackgroundResource(selectableItemBackgroundResourceId)
                                val f = group.getFormat(trackIndex)
                                val uzItem = UZItem.create(
                                    format = f,
                                    description = parserNameProvider(f)
                                )
                                trackView.text = uzItem.description
                                if (trackInfo.getTrackSupport(
                                        rendererIndex,
                                        groupIndex,
                                        trackIndex
                                    ) == RendererCapabilities.FORMAT_HANDLED
                                ) {
                                    trackView.isFocusable = true
                                    trackView.tag = Pair.create(groupIndex, trackIndex)
                                    trackView.setOnClickListener(componentListener)
                                } else {
                                    trackView.isFocusable = false
                                    trackView.isEnabled = false
                                }
                                a[groupIndex]?.set(index = trackIndex, value = trackView)
                                addView(trackView)
                                uzItem.checkedTextView = trackView
                                uzItemList.add(uzItem)
                            }
                        }
                        updateViewStates()
                    }
                }
            }
        }
    }

    private fun parserNameProvider(format: Format): String {
        return if (format.frameRate == Format.NO_VALUE.toFloat()) {
            val minRes = min(format.width, format.height)
            if (minRes < 0) {
                trackNameProvider.getTrackName(format)
            } else String.format(Locale.getDefault(), "%dp", minRes)
        } else String.format(
            Locale.getDefault(),
            "%dp (%.0f)",
            min(format.width, format.height),
            format.frameRate
        )
    }

    private fun updateViewStates() {
        disableView.isChecked = isDisabled
        defaultView.isChecked = !isDisabled && override == null
        trackViews?.let { a ->
            for (i in a.indices) {
                a[i]?.let {
                    for (j in it.indices) {
                        it[j]?.isChecked =
                            override != null
                                    && override?.groupIndex == i && override?.containsTrack(j) == true
                    }
                }
            }
            //scroll to
            if (disableView.isChecked) {
                scrollTo(disableView)
            }
            if (defaultView.isChecked) {
                scrollTo(defaultView)
            }
            for (trackView in a) {
                trackView?.let {
                    for (checkedTextView in it) {
                        if (checkedTextView?.isChecked == true) {
                            scrollTo(checkedTextView)
                        }
                    }
                }
            }
        }
    }

    private fun scrollTo(checkedTextView: CheckedTextView) {
        if (this.parent is ScrollView) {
            val sv = this.parent as ScrollView
            sv.postDelayed(
                {
                    sv.scrollTo(0, checkedTextView.top)
                }, 100
            )
        }
    }

    private fun applySelection() {
        trackSelector?.let { dts ->
            val parametersBuilder = dts.buildUponParameters()
            parametersBuilder.setRendererDisabled(rendererIndex, isDisabled)
            if (override != null) {
                parametersBuilder.setSelectionOverride(rendererIndex, trackGroups, override)
            } else {
                parametersBuilder.clearSelectionOverrides(rendererIndex)
            }
            dts.setParameters(parametersBuilder)
        }
    }

    private fun onClick(view: View) {
        when {
            view === disableView -> {
                onDisableViewClicked()
            }
            view === defaultView -> {
                onDefaultViewClicked()
            }
            else -> {
                onTrackViewClicked(view)
            }
        }
        updateViewStates()
        applySelection()
        callback?.onClick()
    }

    private fun onDisableViewClicked() {
        isDisabled = true
        override = null
    }

    private fun onDefaultViewClicked() {
        isDisabled = false
        override = null
    }

    private fun onTrackViewClicked(view: View) {
        isDisabled = false
        val tag = view.tag as Pair<Int, Int>
        val groupIndex = tag.first
        val trackIndex = tag.second
        if (override == null || override?.groupIndex != groupIndex || !allowAdaptiveSelections) {
            // A new override is being started.
            override = SelectionOverride(groupIndex, trackIndex)
        } else {
            // An existing override is being modified.
            override?.let { so ->
                val overrideLength = so.length
                val overrideTracks = so.tracks
                if (view is CheckedTextView) {
                    if (view.isChecked) {
                        // Remove the track from the override.
                        if (overrideLength == 1) {
                            // The last track is being removed, so the override becomes empty.
                            override = null
                            isDisabled = true
                        } else {
                            val tracks = getTracksRemoving(
                                tracks = overrideTracks,
                                removedTrack = trackIndex
                            )
                            override = SelectionOverride(groupIndex, *tracks)
                        }
                    } else {
                        val tracks = getTracksAdding(
                            _tracks = overrideTracks,
                            addedTrack = trackIndex
                        )
                        override = SelectionOverride(groupIndex, *tracks)
                    }
                }
            }
        }
    }

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }
}
