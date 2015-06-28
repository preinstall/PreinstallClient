package com.smona.app.preinstallclient;

import com.smona.app.preinstallclient.control.IconCache;
import com.smona.app.preinstallclient.control.ImageCacheStrategy;
import com.smona.app.preinstallclient.download.DownloadProxy;
import com.smona.app.preinstallclient.util.LogUtil;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

public class ClientApplication extends Application {
    private static final String TAG = "ClientApplication";

    private ProcessModel mModel;
    private IconCache mIconCache;

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate");
        init();
    }

    private void init() {
        ImageCacheStrategy.getInstance().initCacheStrategy(this);
        DownloadProxy.setAppContext(this);
        mIconCache = new IconCache(this);
        mModel = new ProcessModel(this, mIconCache);
        reigster();
    }

    private void reigster() {
        registerBroadcast();
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        this.registerReceiver(mModel, filter);
    }

    public ProcessModel getProcessModel() {
        return mModel;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }
}
