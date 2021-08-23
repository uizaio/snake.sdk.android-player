package com.uiza.sdk.dialog.setting;

public interface OnToggleChangeListener {
    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param isChecked The new checked state of buttonView.
     * @return is checked
     */
    boolean onCheckedChanged(boolean isChecked);
}
