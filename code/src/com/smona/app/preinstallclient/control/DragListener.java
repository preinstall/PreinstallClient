package com.smona.app.preinstallclient.control;

public interface DragListener {

    void onDragStart(DragSource source, Object info, int dragAction);

    void onDragEnd();
}
