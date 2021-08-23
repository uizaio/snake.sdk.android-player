package com.uiza.sdk.dialog.setting

import androidx.annotation.Keep

@Keep
class SettingItem @JvmOverloads constructor(
    val title: String,
    val checked: Boolean = false,
    val listener: OnToggleChangeListener? = null
) {
    val isToggle: Boolean = listener != null
}
