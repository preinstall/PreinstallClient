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
import com.smona.app.preinstallclient.control.IconCache;
import com.smona.app.preinstallclient.control.ImageCacheStrategy;
import com.smona.app.preinstallclient.control.RequestDataStategy;
import com.smona.app.preinstallclient.image.BitmapProcess;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

public class ProcessModel extends BroadcastReceiver {

    private static final String TAG = "ProcessModel";
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_DATA = false;
    private static final boolean TEST_EVN_DEBUG = false;
    private ClientApplication mApp;
    private IconCache mIconCache;
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

    ProcessModel(ClientApplication app, IconCache iconCache) {
        mApp = app;
        mIconCache = iconCache;
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
            int result = updateDB(context, packageName);
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

    private void requestHttpDatas() {
        boolean needRequestData = RequestDataStategy.INSTANCE
                .isNeedRetryQuestData(mApp);
        LogUtil.d(TAG, "requestData: needRequestData: " + needRequestData);
        if (!needRequestData) {
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("version", "2.0");
        String jsonString = HttpUtils.postData(getUrl(), map);
        boolean success = HttpUtils.isRequestDataSuccess(jsonString);
        LogUtil.d(TAG, "requestData: success: " + success + ", jsonString: "
                + jsonString);
        if (success) {
            List<ItemInfo> datas = ParseJsonString.parseJsonToItems(jsonString);
            filterDulicateMemory(datas);
            requestIcons(datas);
            filterDulicateDB(datas);
            saveToDB(datas);
            RequestDataStategy.INSTANCE.saveLastRequestDataTime(mApp);
        } else {

        }
    }

    private void saveToDB(List<ItemInfo> datas) {
        ContentResolver contentResolver = mApp.getContentResolver();
        LogUtil.d(TAG, "datas: " + datas.size());
        for (ItemInfo info : datas) {
            insertDB(contentResolver, info);
        }
    }

    private void requestIcons(List<ItemInfo> datas) {
        int size = datas.size();
        ItemInfo info = null;
        for (int i = 0; i < size; i++) {
            info = datas.get(i);
            requestIcon(info);
        }
    }

    private void requestIcon(final ItemInfo info) {
        ImageCacheStrategy.ReturnImageType type = ImageCacheStrategy
                .getInstance().downloadImage(info.packageName, info.appIconUrl);
        LogUtil.d(TAG, "requestIcon type=" + type + ", info.appIconUrl: "
                + info.appIconUrl);
        if (ImageCacheStrategy.ReturnImageType.DOWNLOAD == type) {
            BitmapProcess.processBitmap(mApp, R.drawable.template,
                    info.packageName);
        }
        mIconCache.cacheBitmap(info.packageName);
    }

    private void filterDulicateMemory(List<ItemInfo> datas) {
        PackageManager manager = mApp.getPackageManager();
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

    private void filterDulicateDB(List<ItemInfo> datas) {
        List<ItemInfo> dbDatas = MainDataSource.queryDBDatas(mApp);
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

    private void insertDB(ContentResolver contentResolver, ItemInfo info) {
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
        contentResolver.insert(ClientSettings.ItemColumns.CONTENT_URI, values);
    }

    private static int updateDB(Context context, String packageName) {
        int result = updateDB(context, packageName, 1);
        return result;
    }

    public static int updateDB(Context context, String packageName, int status) {
        ContentResolver resolver = context.getContentResolver();
        String where = ClientSettings.ItemColumns.PACKAGENAME + "='"
                + packageName + "'";
        ContentValues values = new ContentValues();
        values.put(ClientSettings.ItemColumns.DOWNLOADSTATUS, status);
        int result = resolver.update(ClientSettings.ItemColumns.CONTENT_URI,
                values, where, null);
        return result;
    }

    private boolean noRecyleCallback() {
        return mCallbacks != null && mCallbacks.get() != null;
    }

    private static String getUrl() {
        if (TEST_EVN_DEBUG) {
            return Constant.APPINFO_URL_TEST;
        } else {
            return Constant.APPINFO_URL;
        }
    }

    interface Callbacks {
        public void bindItems(IDataSource dataSource);
    }
}
