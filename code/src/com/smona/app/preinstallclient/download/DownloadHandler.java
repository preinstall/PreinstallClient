package com.smona.app.preinstallclient.download;

import java.util.ArrayList;
import java.util.HashMap;

import com.smona.app.preinstallclient.util.LogUtil;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class DownloadHandler extends Handler {
    private static final String TAG = "DownloadHandler";
    public static final Uri DOWNLOAD_URI = Uri.parse("content://downloads/my_downloads");

    private IDownloadCallback mDownloadCallback;
    private DownloadManager mDownloadManager;

    public DownloadHandler(Looper looper, Context context) {
        super(looper);
        mDownloadManager = (DownloadManager) context
                .getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void setCallback(IDownloadCallback callback) {
        mDownloadCallback = callback;
    }

    public void handleMessage(Message msg) {
        HashMap<String, DownloadInfo> downloadInfos = query();
        refreshUI(downloadInfos);
    }

    private HashMap<String, DownloadInfo> query() {
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor cursor = null;
        HashMap<String, DownloadInfo> downloadinfos = new HashMap<String, DownloadInfo>();
        try {
            cursor = mDownloadManager.query(query);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                long downId = cursor.getLong(cursor
                        .getColumnIndex(DownloadManager.COLUMN_ID));
                String path = cursor.getString(cursor
                        .getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                String url = cursor.getString(cursor
                        .getColumnIndex(DownloadManager.COLUMN_URI));
                int progress = cursor
                        .getInt(cursor
                                .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                int total = cursor
                        .getInt(cursor
                                .getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                int status = cursor.getInt(cursor
                        .getColumnIndex(DownloadManager.COLUMN_STATUS));
                DownloadInfo downloadInfo = new DownloadInfo();
                downloadInfo.url = url;
                downloadInfo.downloadID = downId;
                downloadInfo.downloadPath = path;
                downloadInfo.total = total;
                downloadInfo.process = progress;
                downloadInfo.status = status;
                LogUtil.d(TAG, Thread.currentThread().getId()
                        + " , DownloadInfo: " + downloadInfo);
                downloadinfos.put(url, downloadInfo);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return downloadinfos;
    }

    private void refreshUI(HashMap<String, DownloadInfo> downloadinfos) {
        if (mDownloadCallback != null) {
            mDownloadCallback.refreshUI(downloadinfos);
        }
    }
}
