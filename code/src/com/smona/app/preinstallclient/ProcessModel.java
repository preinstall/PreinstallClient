package com.smona.app.preinstallclient;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smona.app.preinstallclient.data.IDataSource;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.data.cache.CacheDataSource;
import com.smona.app.preinstallclient.data.db.ClientSettings;
import com.smona.app.preinstallclient.data.db.MainDataSource;
import com.smona.app.preinstallclient.control.DeferredHandler;
import com.smona.app.preinstallclient.control.RequestDataStategy;
import com.smona.app.preinstallclient.util.Constant;
import com.smona.app.preinstallclient.util.HttpUtils;
import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.util.ParseJsonString;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

public class ProcessModel extends BroadcastReceiver {

    private static final String TAG = "ProcessModel";
    public static final boolean DEBUG = true;
    public static final boolean DEBUG_DATA = false;
    public static final boolean TEST_EVN_DEBUG = false;
    private Context mApp;
    private DeferredHandler mHandler = new DeferredHandler();
    private static final HandlerThread WORKER_THREAD = new HandlerThread(
            "loader");
    static {
        WORKER_THREAD.start();
    }
    private static final Handler WORKER = new Handler(WORKER_THREAD.getLooper());

    private LoadTask mLoadTask;

    private WeakReference<Callbacks> mCallbacks;
    private final Object mLock = new Object();

    ProcessModel(Context app) {
        mApp = app;
    }

    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
    }

    private void runOnMainThread(Runnable r) {
        runOnMainThread(r, 0);
    }

    private void runOnMainThread(Runnable r, int type) {
        if (WORKER_THREAD.getThreadId() == Process.myTid()) {
            // If we are on the worker thread, post onto the main handler
            mHandler.post(r, type);
        } else {
            r.run();
        }
    }

    public static void runOnWorkerThread(Runnable r) {
        if (WORKER_THREAD.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            WORKER.post(r);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) {
            LogUtil.d(TAG, "onReceive intent=" + intent);
        }
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            int result = deleteDB(context, packageName);
            startLoadTask();
            LogUtil.d(TAG, "onReceive result: " + result);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {

        }
    }

    public void startLoadTask() {
        synchronized (mLock) {
            if (noRecyleCallback()) {
                mLoadTask = new LoadTask();
                WORKER_THREAD.setPriority(Thread.NORM_PRIORITY);
                WORKER.post(mLoadTask);
            }
        }
    }

    class LoadTask implements Runnable {
        public void run() {
            loadData();
            bindData();
        }

        private void loadData() {
            LogUtil.d(TAG, "loadData start: ");
            requestHttpDatas();
            LogUtil.d(TAG, "loadData end! ");
        }

        private void bindData() {
            LogUtil.d(TAG, "bindData start: ");
            if (noRecyleCallback()) {
                final IDataSource dataSource = createDataSource(mApp);
                filterDulicateMemory(mApp, dataSource.getMdatas());
                Runnable r = new Runnable() {
                    public void run() {
                        mCallbacks.get().bindItems(dataSource);
                    }
                };
                runOnMainThread(r);
            }
            LogUtil.d(TAG, "bindData end!");
        }
    }

    public static IDataSource createDataSource(Context context) {
        IDataSource dataSource = null;
        if (DEBUG_DATA) {
            dataSource = createCacheDataSource(context);
        } else {
            dataSource = createDBDataSource(context);
        }
        return dataSource;
    }

    private static IDataSource createCacheDataSource(Context context) {
        IDataSource dataSource = new CacheDataSource(context);
        dataSource.init();
        return dataSource;
    }

    private static IDataSource createDBDataSource(Context context) {
        IDataSource dataSource = new MainDataSource(context);
        dataSource.init();
        return dataSource;
    }

    public void requestHttpDatas() {
        boolean needRequestData = RequestDataStategy.INSTANCE
                .isNeedRetryQuestData(mApp);
        LogUtil.d(TAG, "requestData: needRequestData: " + needRequestData);
        if (!needRequestData) {
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("version", "-1");
        String jsonString = HttpUtils.postData(getUrl(), map);
        boolean success = HttpUtils.isRequestDataSuccess(jsonString);
        LogUtil.d(TAG, "requestData: success: " + success + ", jsonString: "
                + jsonString);
        if (success) {
            List<ItemInfo> datas = ParseJsonString.parseJsonToItems(jsonString);
            filterDulicateMemory(mApp, datas);
            filterDulicateDB(mApp, datas);
            saveToDB(mApp, datas);
            RequestDataStategy.INSTANCE.saveLastRequestDataTime(mApp);
        } else {

        }
    }

    public static synchronized void saveToDB(Context mApp,
            List<ItemInfo> datas) {
        ContentResolver contentResolver = mApp.getContentResolver();
        LogUtil.d(TAG, "datas: " + datas.size());
        Cursor c;
        for (int i = 0; i < datas.size(); i++) {
            ItemInfo info = datas.get(i);
            c = contentResolver.query(ClientSettings.ItemColumns.CONTENT_URI,
                    null, ClientSettings.ItemColumns.PACKAGENAME + " = '"
                            + info.packageName + "'", null, null);
            if (c == null || c.getCount() == 0) {
                info.appindex = RequestDataStategy.INSTANCE.getLastDataIndex(mApp);
                insertDB(contentResolver, info);
            } else {
                updatetDB(contentResolver, info);
                c.close();
            }
        }
    }

    public static void filterDulicateMemory(Context context,
            List<ItemInfo> datas) {
        PackageManager manager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> memoryDatas = manager
                .queryIntentActivities(intent, 0);
        int count = datas.size();
        ItemInfo info = null;
        String packageName = null;
        for (int index = count - 1; index >= 0; index--) {
            info = datas.get(index);
            for (ResolveInfo resolveInfo : memoryDatas) {
                packageName = resolveInfo.activityInfo.packageName;
                if (info.packageName.equals(packageName)) {
                    datas.remove(index);
                    break;
                }
            }
        }
    }

    public static void filterDulicateDB(Context context,
            List<ItemInfo> datas) {
        List<ItemInfo> dbDatas = MainDataSource.queryDBDatas(context);
        ItemInfo info = null;
        String packageName = null;
        int count = datas.size();
        for (int index = count - 1; index >= 0; index--) {
            info = datas.get(index);
            for (ItemInfo item : dbDatas) {
                packageName = item.packageName;
                if (info.packageName.equals(packageName)) {
                    datas.remove(index);
                    break;
                }
            }
        }
    }

    public static void insertDB(ContentResolver contentResolver, ItemInfo info) {
        final ContentValues values = new ContentValues();
        values.put(ClientSettings.ItemColumns.APPID, info.appid);
        values.put(ClientSettings.ItemColumns.PACKAGENAME, info.packageName);
        values.put(ClientSettings.ItemColumns.APPCLASS, info.className);
        values.put(ClientSettings.ItemColumns.APPURL, info.appUrl);
        values.put(ClientSettings.ItemColumns.APPICONURL, info.appIconUrl);
        values.put(ClientSettings.ItemColumns.APPNAME, info.appName);
        values.put(ClientSettings.ItemColumns.APPSIZE, info.appSize);
        values.put(ClientSettings.ItemColumns.SDKVERSION, info.sdkVersion);
        values.put(ClientSettings.ItemColumns.DOWNLOADSTATUS,
                info.downloadStatus);
        values.put(ClientSettings.ItemColumns.ISNEW, info.isnew);
        values.put(ClientSettings.ItemColumns.INDEX, info.appindex);
        contentResolver.insert(ClientSettings.ItemColumns.CONTENT_URI, values);
    }

    public static void updatetDB(ContentResolver contentResolver, ItemInfo info) {
        final ContentValues values = new ContentValues();
        values.put(ClientSettings.ItemColumns.APPCLASS, info.className);
        values.put(ClientSettings.ItemColumns.APPURL, info.appUrl);
        values.put(ClientSettings.ItemColumns.APPICONURL, info.appIconUrl);
        values.put(ClientSettings.ItemColumns.APPNAME, info.appName);
        values.put(ClientSettings.ItemColumns.APPSIZE, info.appSize);
        values.put(ClientSettings.ItemColumns.SDKVERSION, info.sdkVersion);
        String where = ClientSettings.ItemColumns.PACKAGENAME + "='"
                + info.packageName + "'";
        int result = contentResolver.update(
                ClientSettings.ItemColumns.CONTENT_URI_NO_NOTIFICATION, values,
                where, null);
        LogUtil.d(TAG, "updatetDB: " + result);
    }

    public static int updateDB(Context context, String packageName,
            ContentValues values) {
        ContentResolver resolver = context.getContentResolver();
        String where = ClientSettings.ItemColumns.PACKAGENAME + "='"
                + packageName + "'";
        int result = resolver.update(
                ClientSettings.ItemColumns.CONTENT_URI_NO_NOTIFICATION, values,
                where, null);
        return result;
    }

    public static int deleteDB(Context context, String packageName) {
        ContentResolver resolver = context.getContentResolver();
        String where = ClientSettings.ItemColumns.PACKAGENAME + "='"
                + packageName + "'";
        int result = resolver.delete(ClientSettings.ItemColumns.CONTENT_URI,
                where, null);
        return result;
    }

    private boolean noRecyleCallback() {
        return mCallbacks != null && mCallbacks.get() != null;
    }

    public static String getUrl() {
        return Constant.APPINFO_URL_TEST;
    }

    interface Callbacks {
        public void bindItems(IDataSource dataSource);
    }
}
