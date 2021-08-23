package com.uiza.sdk.dialog.setting

interface OnToggleChangeListener {
    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param isChecked The new checked state of buttonView.
     * @return is checked
     */
    fun onCheckedChanged(isChecked: Boolean): Boolean
}
