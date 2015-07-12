package com.smona.app.preinstallclient;

import com.smona.app.preinstallclient.util.PackageUtils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

public class DownloadCompleteReceiver extends BroadcastReceiver {

    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {

            Toast.makeText(context, R.string.download_completed,
                    Toast.LENGTH_LONG).show();

            String fileName = "";

            /**
             * The download manager is a system service that handles
             * long-running HTTP downloads.
             */
            DownloadManager downloadManager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);// �����ط����ȡ���ع�����

            DownloadManager.Query query = new DownloadManager.Query();

            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);// ���ù���״̬���ɹ�

            Cursor c = downloadManager.query(query);// ��ѯ��ǰ���ع�ġ��ɹ��ļ���

            if (c.moveToFirst()) {// �ƶ����������ص��ļ�
                fileName = c.getString(c
                        .getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
            }
            c.close();
            System.out.println("======�ļ����=====" + fileName);

            final String localfileName = fileName;// ����·��

            new Thread() {
                public void run() {
                    PackageUtils.install(context, localfileName);
                }
            }.start();
        }
    }
}