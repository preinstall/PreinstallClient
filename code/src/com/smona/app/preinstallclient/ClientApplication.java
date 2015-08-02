package com.smona.app.preinstallclient;

import com.smona.app.preinstallclient.control.ImageLoaderManager;
import com.smona.app.preinstallclient.download.DownloadProxy;
import com.smona.app.preinstallclient.download_ex.PreInstallAppManager;
import com.smona.app.preinstallclient.util.LogUtil;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

public class ClientApplication extends Application {
    private static final String TAG = "ClientApplication";

    private ProcessModel mModel;

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate");
        init();
    }

    private void init() {
        ImageLoaderManager.getInstance().initImageLoader(this);
        DownloadProxy.setAppContext(this);
        mModel = new ProcessModel(this);
        reigster();
        PreInstallAppManager.getPreInstallAppManager(this).init(this);
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
}
