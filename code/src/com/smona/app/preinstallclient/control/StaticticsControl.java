package com.smona.app.preinstallclient.control;

import java.util.Map;

import android.content.Context;

import com.smona.app.preinstallclient.statictics.StaticticsProxy;
import com.smona.app.preinstallclient.statictics.YoujuProxy;

public class StaticticsControl {
    private static StaticticsControl sInstance;
    private StaticticsProxy mProxy;

    private StaticticsControl() {
        mProxy = new YoujuProxy();
    }

    public static StaticticsControl getInstance() {
        if (sInstance == null) {
            sInstance = new StaticticsControl();
        }
        return sInstance;
    }

    public void init(Context context) {
        mProxy.init(context);
    }

    public void onResume(Context context) {
        mProxy.onPause(context);
    }

    public void onPause(Context context) {
        mProxy.onPause(context);
    }

    public void onEventForStatistics(Context context, String eventId,
            String eventLabel, long id) {
        mProxy.onEventForStatistics(context, eventId, eventLabel, id);
    }

    public void onEventForStatistics(Context context, String eventId,
            String eventLabel, Map<String, Object> map) {
        mProxy.onEventForStatistics(context, eventId, eventLabel, map);
    }
}
