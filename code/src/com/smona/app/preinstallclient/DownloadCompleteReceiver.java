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

			Toast.makeText(context, "下载完成！", Toast.LENGTH_LONG).show();

			String fileName = "";

			/**
			 * The download manager is a system service that handles
			 * long-running HTTP downloads.
			 */
			DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);// 从下载服务获取下载管理器

			DownloadManager.Query query = new DownloadManager.Query();

			query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);// 设置过滤状态：成功

			Cursor c = downloadManager.query(query);// 查询以前下载过的‘成功文件’

			if (c.moveToFirst()) {// 移动到最新下载的文件
				fileName = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
			}
			c.close();
			System.out.println("======文件名称=====" + fileName);

			final String localfileName = fileName;// 过滤路径

			new Thread(){
				public void run(){
					PackageUtils.install(context,localfileName);
				}
			}.start();

		}
	}
}