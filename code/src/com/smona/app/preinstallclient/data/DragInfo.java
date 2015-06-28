package com.smona.app.preinstallclient.data;

import android.view.View;

public class DragInfo {
    public View cell;
    public int pos = -1;

    @Override
    public String toString() {
        return "Cell[view="
                + (cell == null ? "null" : cell + ", position: " + pos);
    }
}
