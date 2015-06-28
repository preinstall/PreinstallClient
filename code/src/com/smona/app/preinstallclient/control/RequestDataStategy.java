package com.smona.app.preinstallclient.control;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;

public enum RequestDataStategy {
    INSTANCE;
    private static final String RequestDataStategy = "RequestDataStategy";
    private static final String LAST_TIME_FLAG = "last_time";
    private static final long ONE_HOUR = 60 * 60 * 1000;

    public boolean isNeedRetryQuestData(Context context) {
        long currentTime = SystemClock.uptimeMillis();
        long lastTime = getLastRequestDataTime(context);
        long diff = currentTime - lastTime;
        return currentTime == -1 || diff >= ONE_HOUR;
    }

    public void saveLastRequestDataTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(RequestDataStategy,
                Context.MODE_PRIVATE);
        Editor et = sp.edit();
        et.putLong(LAST_TIME_FLAG, SystemClock.uptimeMillis());
        et.commit();
    }

    private long getLastRequestDataTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(RequestDataStategy,
                Context.MODE_PRIVATE);
        return sp.getLong(LAST_TIME_FLAG, -1);
    }
}
