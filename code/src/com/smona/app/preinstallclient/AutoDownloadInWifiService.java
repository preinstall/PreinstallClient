package com.smona.app.preinstallclient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.smona.app.preinstallclient.control.RequestDataStategy;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.data.db.ClientSettings;
import com.smona.app.preinstallclient.data.db.MainDataSource;
import com.smona.app.preinstallclient.download.DownloadProxy;
import com.smona.app.preinstallclient.util.CommonUtil;
import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.view.Element;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class AutoDownloadInWifiService extends Service {

    private static final String TAG = "AutoDownloadInWifiService";

    private static final int DOWNLOAD_FETCH_COUNT = 9;

    private static final String LAST_SECOND = " 23:59:59";

    private Handler mRequestDownload = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long delay = getDelayTime();
        if (delay < 0) {
            return super.onStartCommand(intent, flags, startId);
        }
        mRequestDownload.removeCallbacks(mAuto);
        mRequestDownload.postDelayed(mAuto, delay);
        return super.onStartCommand(intent, flags, startId);
    }

    private Runnable mAuto = new Runnable() {
        @Override
        public void run() {
            autoDownload();
        }
    };

    @SuppressLint("SimpleDateFormat")
    private long getDelayTime() {
        // current time
        long currentTime = System.currentTimeMillis();

        // last time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new java.util.Date());
        String lastDate = date + LAST_SECOND;
        long lastSecond = getStringToDate(lastDate);

        // diff
        long delay = lastSecond - currentTime;

        LogUtil.d(TAG, "currentTime: " + currentTime + ", lastSecond: "
                + lastSecond + ", delay: " + delay + ", h: " + delay
                / RequestDataStategy.ONE_HOUR);
        return delay;
    }

    @SuppressLint("SimpleDateFormat")
    public static long getStringToDate(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = new Date();
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    private void autoDownload() {
        if (CommonUtil.checkWifiInfo(this)) {
            String conditions = ClientSettings.ItemColumns.ISDELETE + "=? and "
                    + ClientSettings.ItemColumns.DOWNLOADSTATUS + "=?";
            String[] selectArgs = new String[] {
                    ClientSettings.ItemColumns.DELETE_NO + "",
                    Element.State.NONE.ordinal() + "" };
            List<ItemInfo> datas = MainDataSource.queryDBDatas(this,
                    conditions, selectArgs);

            ProcessModel.filterDulicateMemory(this, datas);
            int count = datas.size();
            int endPos = count > DOWNLOAD_FETCH_COUNT ? DOWNLOAD_FETCH_COUNT
                    : count;
            ItemInfo info = null;
            for (int i = 0; i < endPos; i++) {
                info = datas.get(i);
                LogUtil.d(TAG, "autoDownload info: " + info);
                DownloadProxy.getInstance().equeue(datas.get(i));
                ProcessModel.updateContentDB(this, info.packageName,
                        Element.State.DOWNLOADING);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
