package com.smona.app.preinstallclient.download;

import android.database.ContentObserver;
import android.os.Handler;

public class DownloadObsever extends ContentObserver {
    private Handler mWorkHandler;

    public DownloadObsever(Handler handler) {
        super(handler);
        mWorkHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        mWorkHandler.sendEmptyMessage(0);
    }
}
