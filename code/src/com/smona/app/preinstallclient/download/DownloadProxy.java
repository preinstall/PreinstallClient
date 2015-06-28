package com.smona.app.preinstallclient.download;

import com.smona.app.preinstallclient.data.ItemInfo;

import android.app.Application;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.view.View;

public class DownloadProxy {
    private DownloadManager mDownloadMgr;
    private volatile static DownloadProxy sInstance = null;
    private static Context sAppContext;
    
    public static final int STATUS_PENDING = DownloadManager.STATUS_PENDING;
    public static final int STATUS_FAILED = DownloadManager.STATUS_FAILED;
    public static final int STATUS_PAUSED = DownloadManager.STATUS_PAUSED;
    public static final int STATUS_RUNNING = DownloadManager.STATUS_RUNNING;
    public static final int STATUS_SUCCESSFUL = DownloadManager.STATUS_SUCCESSFUL;

    public static void setAppContext(Context appContext) {
        if (!(appContext instanceof Application)) {
            throw new RuntimeException("appContext not Application Context!");
        }
        sAppContext = appContext;
    }

    private DownloadProxy() {
        mDownloadMgr = (DownloadManager) sAppContext
                .getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public static synchronized DownloadProxy getInstance() {
        if (sInstance == null) {
            sInstance = new DownloadProxy();
        }
        return sInstance;
    }

    public long equeue(ItemInfo info) {
        // WallpaperUtil.initAppEnvironment();
        Request r = new Request(Uri.parse(info.appUrl));
        r.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                | DownloadManager.Request.NETWORK_WIFI);
        r.setNotificationVisibility(View.VISIBLE);
        r.setTitle(info.appName);
        long downloadid = mDownloadMgr.enqueue(r);
        return downloadid;
    }

    public int remove(long downId) {
        return mDownloadMgr.remove(downId);
    }
}
