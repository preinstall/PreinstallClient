package com.smona.app.preinstallclient.control;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;

public enum RequestDataStategy {
	INSTANCE;
	private static final String RequestDataStategy = "RequestDataStategy";
	private static final String DataIndex = "DataIndex";
	private static final String LAST_TIME_FLAG = "last_time";
	private static final long ONE_HOUR = 60 * 60 * 1000;
	public static final long WAIT_TIME = 4 * ONE_HOUR;

	public boolean isNeedRetryQuestData(Context context) {
		long currentTime = System.currentTimeMillis();
		long lastTime = getLastRequestDataTime(context);
		long diff = currentTime - lastTime;
		return currentTime == -1 || diff >= WAIT_TIME;
	}

	public static void saveLastRequestDataTime(Context context) {
		SharedPreferences sp = context.getSharedPreferences(RequestDataStategy, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putLong(LAST_TIME_FLAG, System.currentTimeMillis());
		et.commit();
	}

	private static long getLastRequestDataTime(Context context) {
		SharedPreferences sp = context.getSharedPreferences(RequestDataStategy, Context.MODE_PRIVATE);
		return sp.getLong(LAST_TIME_FLAG, -1);
	}

	public static void saveLastDataIndex(Context context, int index) {
		SharedPreferences sp = context.getSharedPreferences(RequestDataStategy, Context.MODE_PRIVATE);
		Editor et = sp.edit();
		et.putInt(DataIndex, index);
		et.commit();
	}

	public static int getLastDataIndex(Context context) {
		SharedPreferences sp = context.getSharedPreferences(RequestDataStategy, Context.MODE_PRIVATE);
		int index = sp.getInt(DataIndex, 0);
		saveLastDataIndex(context, index + 1);
		return index;
	}
}
