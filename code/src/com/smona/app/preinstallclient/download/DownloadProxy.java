package com.smona.app.preinstallclient.download;

import com.gionee.preinstallation.constant.Constant;
import com.gionee.preinstallation.utils.StorageUtils;
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
        Request request = new Request(Uri.parse(info.appUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                | DownloadManager.Request.NETWORK_WIFI);
        request.setDestinationInExternalPublicDir(StorageUtils.getHomeDir(),
                info.packageName + Constant.APK);
        request.setNotificationVisibility(View.VISIBLE);
        request.setDescription(info.appName);
        request.setTitle(info.appName);
        request.setMimeType("application/vnd.android.package-archive");
        
        long downloadid = mDownloadMgr.enqueue(request);
        return downloadid;
    }

    public int remove(long downId) {
        return mDownloadMgr.remove(downId);
    }
}
