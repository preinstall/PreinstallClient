package com.smona.app.preinstallclient.statictics;

import java.util.Map;

import android.content.Context;

import com.smona.app.preinstallclient.control.IProxy;

public abstract class StaticticsProxy implements IProxy {
    public void init(Context context) {

    }

    public void onResume(Context context) {

    }

    public void onPause(Context context) {

    }

    public void associateUser(Context context) {

    }

    public void onEventForStatistics(Context context, String eventId,
            String eventLabel, long id) {
    }

    public void onEventForStatistics(Context context, String eventId,
            String eventLabel, Map<String, Object> map) {
    }
}
