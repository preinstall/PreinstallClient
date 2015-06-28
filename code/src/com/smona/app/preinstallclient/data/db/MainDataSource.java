package com.smona.app.preinstallclient.data.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.smona.app.preinstallclient.data.AbstractDataSource;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.util.LogUtil;

public class MainDataSource extends AbstractDataSource {

    private static final String TAG = "DBDataSource";

    public MainDataSource(Context context) {
        super(context);
    }

    @Override
    protected void initDatas() {
        List<ItemInfo> datas = queryDBDatas(mContext);
        if (datas.size() > 0) {
            mDatas.addAll(datas);
        }
    }

    public static List<ItemInfo> queryDBDatas(Context context) {
        List<ItemInfo> values = new ArrayList<ItemInfo>();
        ContentResolver resolver = context.getContentResolver();
        Cursor c = null;
        try {
            c = resolver.query(ClientSettings.ItemColumns.CONTENT_URI, null,
                    null, null, null);
            boolean canRead = c != null;
            if (canRead) {
                int appidIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.APPID);
                int appClassIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.APPCLASS);
                int appIconUrlIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.APPICONURL);
                int appNameIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.APPNAME);
                int appSizeIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.APPSIZE);
                int appUrlIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.APPURL);
                int appPackageIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.PACKAGENAME);
                int appSdkVersionIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.SDKVERSION);
                int downloadStatusIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.DOWNLOADSTATUS);

                while (c.moveToNext()) {
                    ItemInfo info = new ItemInfo();
                    info.appid = c.getString(appidIndex);
                    info.className = c.getString(appClassIndex);
                    info.appIconUrl = c.getString(appIconUrlIndex);
                    info.appName = c.getString(appNameIndex);
                    info.appSize = c.getFloat(appSizeIndex);
                    info.appUrl = c.getString(appUrlIndex);
                    info.packageName = c.getString(appPackageIndex);
                    info.sdkVersion = c.getString(appSdkVersionIndex);
                    info.downloadStatus = c.getInt(downloadStatusIndex);
                    values.add(info);
                }
            }

        } catch (Exception e) {
            LogUtil.d(TAG, "queryDBDatas e: " + e);
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return values;
    }

    public boolean remove(Object object) {
        boolean success = super.remove(object);
        return success;
    }
}
