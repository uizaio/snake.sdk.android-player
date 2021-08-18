package com.uiza.sdk.chromecast;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.mediarouter.app.MediaRouteButton;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.uiza.sdk.R;

import java.io.IOException;

import timber.log.Timber;

/**
 * Core class of Casty. It manages buttons/widgets and gives access to the media player.
 */
public class Casty implements CastyPlayer.OnMediaLoadedListener {
    static String receiverId = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
    static CastOptions customCastOptions;

    private SessionManagerListener<CastSession> sessionManagerListener;
    private OnConnectChangeListener onConnectChangeListener;
    private OnCastSessionUpdatedListener onCastSessionUpdatedListener;

    private CastSession castSession;
    private CastyPlayer castyPlayer;
    private Activity activity;
    private IntroductoryOverlay introductionOverlay;

    //Needed for NoOp instance
    Casty() {
        //no-op
    }

    private Casty(@NonNull Activity activity) {
        this.activity = activity;
        sessionManagerListener = createSessionManagerListener();
        castyPlayer = new CastyPlayer(this);
        activity.getApplication().registerActivityLifecycleCallbacks(createActivityCallbacks());
        CastContext.getSharedInstance(activity).addCastStateListener(createCastStateListener());
    }

    /**
     * Sets the custom receiver ID. Should be used in the {@link Application} class.
     *
     * @param receiverId the custom receiver ID, e.g. Styled Media Receiver - with custom logo and background
     */
    public static void configure(@NonNull String receiverId) {
        Casty.receiverId = receiverId;
    }

    /**
     * Sets the custom CastOptions, should be used in the {@link Application} class.
     *
     * @param castOptions the custom CastOptions object, must include a receiver ID
     */
    public static void configure(@NonNull CastOptions castOptions) {
        Casty.customCastOptions = castOptions;
    }

    /**
     * Creates the Casty object.
     *
     * @param activity {@link Activity} in which Casty object is created
     * @return the Casty object
     */
    public static Casty create(@NonNull Activity activity) {
        int playServicesState = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);
        if (playServicesState == ConnectionResult.SUCCESS) {
            return new Casty(activity);
        } else {
            Timber.w("Google Play services not found on a device, Casty won't work.");
            return new CastyNoOp();
        }
    }

    /**
     * Gives access to {@link CastyPlayer}, which allows to control the media files.
     *
     * @return the instance of {@link CastyPlayer}
     */
    public CastyPlayer getPlayer() {
        return castyPlayer;
    }

    /**
     * Checks if a Google Cast device is connected.
     *
     * @return true if a Google Cast is connected, false otherwise
     */
    public boolean isConnected() {
        return castSession != null;
    }

    /**
     * Adds the discovery menu item on a toolbar and creates Introduction Overlay
     * Should be used in {@link Activity#onCreateOptionsMenu(Menu)}.
     *
     * @param menu Menu in which MenuItem should be added
     */
    @UiThread
    public void addMediaRouteMenuItem(@NonNull Menu menu) {
        activity.getMenuInflater().inflate(R.menu.casty_discovery, menu);
        setUpMediaRouteMenuItem(menu);
        MenuItem menuItem = menu.findItem(R.id.casty_media_route_menu_item);
        introductionOverlay = createIntroductionOverlay(menuItem);
    }

    /**
     * Makes {@link MediaRouteButton} react to discovery events.
     * Must be run on UiThread.
     *
     * @param mediaRouteButton Button to be set up
     */
    @UiThread
    public void setUpMediaRouteButton(@NonNull MediaRouteButton mediaRouteButton) {
        CastButtonFactory.setUpMediaRouteButton(activity, mediaRouteButton);
    }

    /**
     * Adds the Mini Controller at the bottom of Activity's layout.
     * Must be run on UiThread.
     *
     * @return the Casty instance
     */
    @UiThread
    public Casty withMiniController() {
        addMiniController();
        return this;
    }

    /**
     * Adds the Mini Controller at the bottom of Activity's layout
     * Must be run on UiThread.
     */
    @UiThread
    public void addMiniController() {
        ViewGroup contentView = activity.findViewById(android.R.id.content);
        View rootView = contentView.getChildAt(0);
        LinearLayout linearLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(linearLayoutParams);

        contentView.removeView(rootView);

        ViewGroup.LayoutParams oldRootParams = rootView.getLayoutParams();
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(oldRootParams.width, 0, 1f);
        rootView.setLayoutParams(rootParams);

        linearLayout.addView(rootView);
        activity.getLayoutInflater().inflate(R.layout.mini_controller, linearLayout, true);
        activity.setContentView(linearLayout);
    }

    /**
     * Sets {@link OnConnectChangeListener}
     *
     * @param onConnectChangeListener Connect change callback
     */
    public void setOnConnectChangeListener(@Nullable OnConnectChangeListener onConnectChangeListener) {
        this.onConnectChangeListener = onConnectChangeListener;
    }

    /**
     * Sets {@link OnCastSessionUpdatedListener}
     *
     * @param onCastSessionUpdatedListener Cast session updated callback
     */
    public void setOnCastSessionUpdatedListener(@Nullable OnCastSessionUpdatedListener onCastSessionUpdatedListener) {
        this.onCastSessionUpdatedListener = onCastSessionUpdatedListener;
    }

    private void setUpMediaRouteMenuItem(Menu menu) {
        CastButtonFactory.setUpMediaRouteButton(activity, menu, R.id.casty_media_route_menu_item);
    }

    @NonNull
    private CastStateListener createCastStateListener() {
        return state -> {
            if (state != CastState.NO_DEVICES_AVAILABLE && introductionOverlay != null) {
                showIntroductionOverlay();
            }
        };
    }

    private void showIntroductionOverlay() {
        introductionOverlay.show();
    }

    private SessionManagerListener<CastSession> createSessionManagerListener() {
        return new SessionManagerListener<CastSession>() {
            @Override
            public void onSessionStarted(CastSession castSession, String s) {
                Timber.d("onSessionStarted %s", s);
                activity.invalidateOptionsMenu();
                onConnected(castSession);
            }

            @Override
            public void onSessionEnded(CastSession castSession, int i) {
                Timber.d("onSessionEnded");
                activity.invalidateOptionsMenu();
                onDisconnected();
            }

            @Override
            public void onSessionResumed(CastSession castSession, boolean b) {
                Timber.d("onSessionResumed");
                activity.invalidateOptionsMenu();
                onConnected(castSession);
            }

            @Override
            public void onSessionStarting(CastSession castSession) {
                Timber.d("onSessionStarting");
            }

            @Override
            public void onSessionStartFailed(CastSession castSession, int i) {
                Timber.d("onSessionStartFailed");
            }

            @Override
            public void onSessionEnding(CastSession castSession) {
                Timber.d("onSessionEnding");
            }

            @Override
            public void onSessionResuming(CastSession castSession, String s) {
                Timber.d("onSessionResuming");
            }

            @Override
            public void onSessionResumeFailed(CastSession castSession, int i) {
                Timber.d("onSessionResumeFailed");
            }

            @Override
            public void onSessionSuspended(CastSession castSession, int i) {
                Timber.d("onSessionSuspended");
            }
        };
    }

    private void onConnected(CastSession castSession) {
        this.castSession = castSession;
        castyPlayer.setRemoteMediaClient(castSession.getRemoteMediaClient());
        if (onConnectChangeListener != null) onConnectChangeListener.onConnected();
        if (onCastSessionUpdatedListener != null)
            onCastSessionUpdatedListener.onCastSessionUpdated(castSession);
    }

    private void onDisconnected() {
        this.castSession = null;
        if (onConnectChangeListener != null) onConnectChangeListener.onDisconnected();
        if (onCastSessionUpdatedListener != null)
            onCastSessionUpdatedListener.onCastSessionUpdated(null);
    }

    private Application.ActivityLifecycleCallbacks createActivityCallbacks() {
        return new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                //no-op
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (Casty.this.activity == activity) {
                    handleCurrentCastSession();
                    registerSessionManagerListener();
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                if (Casty.this.activity == activity) unregisterSessionManagerListener();
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                //no-op
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @Nullable Bundle outState) {
                //no-op
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (Casty.this.activity == activity) {
                    activity.getApplication().unregisterActivityLifecycleCallbacks(this);
                }
            }
        };
    }

    private IntroductoryOverlay createIntroductionOverlay(MenuItem menuItem) {
        return new IntroductoryOverlay.Builder(activity, menuItem)
                .setTitleText(R.string.casty_introduction_text)
                .setSingleTime()
                .build();
    }

    private void registerSessionManagerListener() {
        CastContext.getSharedInstance(activity).getSessionManager().addSessionManagerListener(sessionManagerListener, CastSession.class);
    }

    private void unregisterSessionManagerListener() {
        CastContext.getSharedInstance(activity).getSessionManager().removeSessionManagerListener(sessionManagerListener, CastSession.class);
    }

    private void handleCurrentCastSession() {
        CastSession newCastSession = CastContext.getSharedInstance(activity).getSessionManager().getCurrentCastSession();
        if (castSession == null) {
            if (newCastSession != null) {
                onConnected(newCastSession);
            }
        } else {
            if (newCastSession == null) {
                onDisconnected();
            } else if (newCastSession != castSession) {
                onConnected(newCastSession);
            }
        }
    }

    //get volume ở cast player
    public double getVolume() {
        CastSession newCastSession = CastContext.getSharedInstance(activity).getSessionManager().getCurrentCastSession();
        return newCastSession.getVolume();
    }

    //set volume ở cast player
    public void setVolume(double volume) {
        try {
            CastSession newCastSession = CastContext.getSharedInstance(activity).getSessionManager().getCurrentCastSession();
            if (newCastSession.isMute()) {
                Timber.d("setVolume %d", volume);
                newCastSession.setVolume(volume);
            }
        } catch (IOException e) {
            Timber.e(e, "IOException setVolume");
        }
    }

    //bật volume ở cast player if mute
    public void turnOnVolume() {
        try {
            CastSession newCastSession = CastContext.getSharedInstance(activity).getSessionManager().getCurrentCastSession();
            if (newCastSession.isMute()) {
                Timber.d("turnOnVolume isMute -> setMute false");
                newCastSession.setMute(false);
            }
        } catch (IOException e) {
            Timber.e(e, "IOException turnOnVolume");
        }
    }

    //toggle mute volume in cast player
    //return true if toggle on
    //return false if toggle off or error
    public boolean toggleMuteVolume() {
        try {
            CastSession newCastSession = CastContext.getSharedInstance(activity).getSessionManager().getCurrentCastSession();
            if (newCastSession.isMute()) {
                Timber.d("toggleMuteVolume isMute -> setMute false");
                newCastSession.setMute(false);
                return false;
            } else {
                Timber.d("toggleMuteVolume !isMute -> setMute true");
                newCastSession.setMute(true);
                return true;
            }
        } catch (IOException e) {
            Timber.e(e, "IOException setMute");
            return false;
        }
    }

    @Override
    public void onMediaLoaded() {
        startExpandedControlsActivity();
    }

    private void startExpandedControlsActivity() {
        Intent intent = new Intent(activity, ExpandedControlsActivity.class);
        activity.startActivity(intent);
    }

    public void disconnectChromeCast() {
        castSession.getRemoteMediaClient().stop(); // stop remote media
        CastContext castContext = CastContext.getSharedInstance(activity);
        SessionManager mSessionManager = castContext.getSessionManager();
        mSessionManager.endCurrentSession(true);
    }

    public interface OnConnectChangeListener {
        void onConnected();

        void onDisconnected();
    }

    public interface OnCastSessionUpdatedListener {
        void onCastSessionUpdated(CastSession castSession);
    }
}
