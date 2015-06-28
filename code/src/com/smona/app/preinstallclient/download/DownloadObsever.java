package com.smona.app.preinstallclient.download;

import com.smona.app.preinstallclient.util.LogUtil;

import android.database.ContentObserver;
import android.os.Handler;

public class DownloadObsever extends ContentObserver {
    private static final String TAG = "DownloadObsever";
    private Handler mWorkHandler;

    public DownloadObsever(Handler handler) {
        super(handler);
        mWorkHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        LogUtil.d(TAG, "onChange: Thread_ID=" + Thread.currentThread().getId()
                + ", change=" + selfChange);
        mWorkHandler.sendEmptyMessage(0);
    }
}
