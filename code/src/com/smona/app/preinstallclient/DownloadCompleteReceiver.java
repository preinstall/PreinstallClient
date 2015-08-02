package com.smona.app.preinstallclient;

import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.util.PackageUtils;
import com.smona.app.preinstallclient.view.Element;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;

public class DownloadCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "DownloadCompleteReceiver";

    public void onReceive(final Context context, Intent intent) {

        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            DownloadManager downloadManager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            Cursor c = downloadManager.query(query);
            String localfileName = null;

            if (c.moveToFirst()) {
                localfileName = c.getString(c
                        .getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
            }
            if (TextUtils.isEmpty(localfileName)) {
                return;
            }
            boolean isAPk = localfileName.endsWith(".apk");
            if (!isAPk) {
                return;
            }
            String[] splits = localfileName.split("/");
            String packageName = splits[splits.length - 1];
            packageName = packageName.substring(0, packageName.length() - 4);
            if (packageName.contains("-")) {
                packageName = packageName
                        .substring(0, packageName.indexOf("-"));
            }
            LogUtil.d(TAG, "packageName: " + packageName);
            ProcessModel.updateContentDB(context, packageName,
                    Element.State.DOWNLOADED);

            startInstallApp(context, localfileName);
        }
    }

    private void startInstallApp(final Context context, final String filePath) {
        new Thread() {
            public void run() {
                PackageUtils.install(context, filePath);
            }
        }.start();
    }
}