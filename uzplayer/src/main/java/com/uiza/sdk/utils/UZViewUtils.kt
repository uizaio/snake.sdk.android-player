package com.uiza.sdk.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.*
import androidx.annotation.ColorInt
import com.google.android.exoplayer2.ui.PlayerView
import com.uiza.sdk.R
import com.uiza.sdk.utils.ConvertUtils.dp2px
import com.uiza.sdk.widget.UZImageButton
import java.util.*
import kotlin.math.max

object UZViewUtils {
    @JvmStatic
    fun isFullScreen(context: Context): Boolean {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        if (windowManager != null) {
            return when (windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_0, Surface.ROTATION_180 -> false
                Surface.ROTATION_90, Surface.ROTATION_270 -> true
                else -> true
            }
        }
        return false
    }

    //return true if device is set auto switch rotation on
    //return false if device is set auto switch rotation off
    @JvmStatic
    fun isRotationPossible(context: Context): Boolean {
        val hasAccelerometer =
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)
        return hasAccelerometer && Settings.System.getInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION,
            0
        ) == 1
    }

    @JvmStatic
    fun getScreenHeightIncludeNavigationBar(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        if (windowManager != null) {
            val display = windowManager.defaultDisplay
            val outPoint = Point()
            // include navigation bar
            display.getRealSize(outPoint)
            return max(a = outPoint.y, outPoint.x)
        }
        return 0
    }

    @JvmStatic
    val screenHeight: Int
        get() = Resources.getSystem().displayMetrics.heightPixels

    @JvmStatic
    val screenWidth: Int
        get() = Resources.getSystem().displayMetrics.widthPixels

    @JvmStatic
    fun visibleViews(vararg views: View?) {
        for (v in views) {
            if (v != null && v.visibility != View.VISIBLE) {
                v.visibility = View.VISIBLE
            }
        }
    }

    @JvmStatic
    fun goneViews(vararg views: View?) {
        for (v in views) {
            if (v != null && v.visibility != View.GONE) {
                v.visibility = View.GONE
            }
        }
    }

    @JvmStatic
    fun setVisibilityViews(visibility: Int, vararg views: View?) {
        for (v in views) {
            if (v != null && v.visibility != visibility) {
                v.visibility = visibility
            }
        }
    }

    //return pixel
    @JvmStatic
    fun heightOfView(view: View): Int {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        return view.measuredHeight
    }

    @JvmStatic
    @SuppressLint("SourceLockedOrientationActivity")
    fun changeScreenPortrait(activity: Activity) {
        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun changeScreenLandscape(activity: Activity) {
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    @JvmStatic
    @SuppressLint("SourceLockedOrientationActivity")
    fun changeScreenLandscape(activity: Activity, orientation: Int) {
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (orientation == 90) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            } else if (orientation == 270) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    private val screenOrientation: Int
        get() = Resources.getSystem().configuration.orientation

    @JvmStatic
    @SuppressLint("SourceLockedOrientationActivity")
    fun toggleScreenOrientation(activity: Activity) {
        val s = screenOrientation
        if (s == Configuration.ORIENTATION_LANDSCAPE) activity.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else if (s == Configuration.ORIENTATION_PORTRAIT) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
    }

    @JvmStatic
    fun hideSystemUiFullScreen(playerView: PlayerView) {
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    @JvmStatic
    fun hideSystemUi(playerView: PlayerView) {
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                and View.SYSTEM_UI_FLAG_LAYOUT_STABLE.inv()
                and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
                and View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION.inv()
                and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv())
    }

    @JvmStatic
    fun setColorProgressBar(progressBar: ProgressBar, @ColorInt color: Int) {
        progressBar.indeterminateDrawable.colorFilter =
            PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }

    @JvmStatic
    fun setTextShadow(textView: TextView, @ColorInt color: Int) {
        textView.setShadowLayer(
            1f,  // radius
            1f,  // dx
            1f,  // dy
            color // shadow color
        )
    }

    @JvmStatic
    fun setMarginPx(view: View, l: Int, t: Int, r: Int, b: Int) {
        val mlp = view.layoutParams as MarginLayoutParams?
        mlp?.setMargins(l, t, r, b)
        view.requestLayout()
    }

    @JvmStatic
    fun setMarginDimen(view: View, dpL: Int, dpT: Int, dpR: Int, dpB: Int) {
        val mlp = view.layoutParams as MarginLayoutParams?
        mlp?.setMargins(
            dp2px(dpL.toFloat()),
            dp2px(dpT.toFloat()),
            dp2px(dpR.toFloat()),
            dp2px(dpB.toFloat())
        )
        view.requestLayout()
    }

    @JvmStatic
    fun setFocusableViews(focusable: Boolean, vararg views: View?) {
        for (v in views) {
            if (v != null && !v.isFocusable) {
                v.isFocusable = focusable
            }
        }
    }

    @JvmStatic
    fun setSrcDrawableEnabledForViews(vararg views: UZImageButton?) {
        for (v in views) {
            if (v != null && !v.isFocused) {
                v.setSrcDrawableEnabled()
            }
        }
    }

    @JvmStatic
    fun setClickableForViews(able: Boolean, vararg views: View?) {
        for (v in views) {
            if (v != null) {
                v.isClickable = able
                v.isFocusable = able
            }
        }
    }

    @JvmStatic
    fun setUIFullScreenIcon(imageButton: ImageButton, isFullScreen: Boolean) {
        imageButton.setImageResource(
            if (isFullScreen) {
                R.drawable.ic_fullscreen_exit_white_48_uz
            } else {
                R.drawable.ic_fullscreen_white_48_uz
            }
        )
    }

    @JvmStatic
    fun resizeLayout(
        viewGroup: ViewGroup,
        videoW: Int,
        videoH: Int,
        isFreeSize: Boolean
    ) {
        val widthSurfaceView: Int
        val heightSurfaceView: Int
        val isFullScreen = isFullScreen(viewGroup.context)
        if (isFullScreen) { //landscape
            widthSurfaceView = getScreenHeightIncludeNavigationBar(viewGroup.context)
            heightSurfaceView = screenHeight
        } else { //portrait
            widthSurfaceView = screenWidth
            heightSurfaceView = if (videoW == 0 || videoH == 0) {
                (widthSurfaceView * Constants.RATIO_9_16).toInt()
            } else {
                if (isFreeSize) {
                    widthSurfaceView * videoH / videoW
                } else {
                    (widthSurfaceView * Constants.RATIO_9_16).toInt()
                }
            }
        }
        viewGroup.layoutParams.width = widthSurfaceView
        viewGroup.layoutParams.height = heightSurfaceView
        viewGroup.requestLayout()
        //set size of parent view group of viewGroup
        val parentViewGroup = viewGroup.parent as RelativeLayout?
        parentViewGroup?.let {
            it.layoutParams.width = widthSurfaceView
            it.layoutParams.height = heightSurfaceView
            it.requestLayout()
        }
        //edit size of imageview thumnail
        val flImgThumnailPreviewSeekbar =
            viewGroup.findViewById<FrameLayout>(R.id.layoutPreviewUZ)
        flImgThumnailPreviewSeekbar?.let {
            if (isFullScreen) {
                it.layoutParams.width = widthSurfaceView / 4
                it.layoutParams.height =
                    (widthSurfaceView / 4 * Constants.RATIO_9_16).toInt()
            } else {
                it.layoutParams.width = widthSurfaceView / 5
                it.layoutParams.height =
                    (widthSurfaceView / 5 * Constants.RATIO_9_16).toInt()
            }
            it.requestLayout()
        }
    }

    @JvmStatic
    fun showDialog(dialog: Dialog) {
        val isFullScreen = isFullScreen(dialog.context)
        val window = dialog.window ?: return
        if (isFullScreen) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                )
                window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            } else {
                //TODO cần làm ở sdk thấp, thanh navigation ko chịu ẩn
            }
        }
        dialog.show()
        try {
            window.attributes.windowAnimations = R.style.uiza_dialog_animation
            window.setBackgroundDrawableResource(R.drawable.background_dialog_uz)
            //set dialog position
            val wlp = window.attributes
            wlp.gravity = Gravity.BOTTOM
            //wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.dimAmount = 0.65f
            wlp.width = ViewGroup.LayoutParams.MATCH_PARENT
            wlp.height =
                ViewGroup.LayoutParams.WRAP_CONTENT // (int) (getScreenHeight() * (isFullScreen ? 0.6 : 0.4));
            window.attributes = wlp
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isFullScreen) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        }
    }

    @JvmStatic
    fun setTextDuration(textView: TextView, duration: String) {
        if (TextUtils.isEmpty(duration)) return
        try {
            val min = duration.toDouble().toInt() + 1
            var minutes = (min % 60).toString()
            minutes = if (minutes.length == 1) "0$minutes" else minutes
            textView.text = String.format(Locale.getDefault(), "%d:%s", min / 60, minutes)
        } catch (e: Exception) {
            e.printStackTrace()
            textView.text = " - "
        }
    }

    @JvmStatic
    fun updateUIFocusChange(view: View, isFocus: Boolean, resHasFocus: Int, resNoFocus: Int) {
        view.setBackgroundResource(if (isFocus) resHasFocus else resNoFocus)
    }
}
