package com.uiza.sdk.dialog.setting;

import java.util.Objects;

public class SettingItem {
    private final String title;
    private final boolean toggle;
    private final boolean checked;
    private final OnToggleChangeListener listener;

    public SettingItem(String title) {
        this(title, false, null);
    }

    public SettingItem(String title, boolean checked, OnToggleChangeListener listener) {
        this.title = title;
        this.toggle = listener != null;
        this.checked = checked;
        this.listener = listener;
    }

    public String getTitle() {
        return title;
    }

    public boolean isToggle() {
        return toggle;
    }

    public boolean isChecked() {
        return checked;
    }

    public OnToggleChangeListener getListener() {
        return listener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingItem that = (SettingItem) o;
        return Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}
