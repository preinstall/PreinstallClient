package com.smona.app.preinstallclient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.smona.app.preinstallclient.control.RequestDataStategy;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.util.HttpUtils;
import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.util.ParseJsonString;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class AutoRefreshDataService extends Service {
    private static final String TAG = "AutoRefreshDataService";
    private ClientApplication mApp;

    @SuppressLint("HandlerLeak")
    private Handler mRefreshHandler = new Handler();

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        mApp = (ClientApplication) getApplication();
        mRefreshHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, RequestDataStategy.WAIT_TIME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    private void loadData() {
        requestHttpDatas();
    }

    private void requestHttpDatas() {
        boolean needRequestData = RequestDataStategy.INSTANCE
                .isNeedRetryQuestData(mApp);
        LogUtil.d(TAG, "requestData: needRequestData: " + needRequestData);
        if (!needRequestData) {
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("version", "-1");
        String jsonString = HttpUtils.postData(ProcessModel.getUrl(), map);
        boolean success = HttpUtils.isRequestDataSuccess(jsonString);
        LogUtil.d(TAG, "requestData: success: " + success + ", jsonString: "
                + jsonString);
        if (success) {
            List<ItemInfo> datas = ParseJsonString.parseJsonToItems(jsonString);
            ProcessModel.filterDulicateMemory(mApp, datas);
            ProcessModel.filterDulicateDB(mApp, datas);
            ProcessModel.saveToDB(mApp, datas);
            RequestDataStategy.INSTANCE.saveLastRequestDataTime(mApp);
        } else {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
