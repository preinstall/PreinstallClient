package com.smona.app.preinstallclient.data.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.smona.app.preinstallclient.data.AbstractDataSource;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.view.Element;

public class MainDataSource extends AbstractDataSource {

    private static final String TAG = "MainDataSource";

    public MainDataSource(Context context) {
        super(context);
    }

    @Override
    protected void initDatas() {
        List<ItemInfo> datas = queryDBDatas(mContext,
                ClientSettings.ItemColumns.ISDELETE + "=?",
                new String[] { ClientSettings.ItemColumns.DELETE_NO + "" });
        if (datas.size() > 0) {
            mDatas.addAll(datas);
        }
    }

    public static List<ItemInfo> queryDBDatas(Context context) {
        return queryDBDatas(context, null, null);
    }

    public static List<ItemInfo> queryDBDatas(Context context,
            String conditions, String[] selectArgs) {
        List<ItemInfo> values = new ArrayList<ItemInfo>();
        ContentResolver resolver = context.getContentResolver();
        Cursor c = null;
        try {
            c = resolver.query(ClientSettings.ItemColumns.CONTENT_URI_NO_NOTIFICATION, null,
                    conditions, selectArgs, ClientSettings.ItemColumns.INDEX
                            + " ASC ");
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
                int indexIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.INDEX);
                int downloadPathPathsIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.DOWNLOADFILEPATH);
                int isNewIndex = c
                        .getColumnIndex(ClientSettings.ItemColumns.ISNEW);
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    ItemInfo info = new ItemInfo();
                    info.appid = c.getString(appidIndex);
                    info.className = c.getString(appClassIndex);
                    info.appIconUrl = c.getString(appIconUrlIndex);
                    info.appName = c.getString(appNameIndex);
                    info.appSize = c.getFloat(appSizeIndex);
                    info.appUrl = c.getString(appUrlIndex);
                    info.packageName = c.getString(appPackageIndex);
                    info.sdkVersion = c.getString(appSdkVersionIndex);
                    info.downloadStatus = Element.State.values()[c
                            .getInt(downloadStatusIndex)];
                    info.appindex = c.getInt(indexIndex);
                    info.downloadFilePath = c.getString(downloadPathPathsIndex);
                    info.isnew = c.getInt(isNewIndex);
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

    private boolean updateDBData(Object object) {
        if (!(object instanceof ItemInfo)) {
            return false;
        }
        ItemInfo info = (ItemInfo) object;
        ContentResolver resolver = mContext.getContentResolver();
        try {
            ContentValues values = new ContentValues();
            values.put(ClientSettings.ItemColumns.ISDELETE, "1");
            int count = resolver.update(ClientSettings.ItemColumns.CONTENT_URI_NO_NOTIFICATION,
                    values, ClientSettings.ItemColumns.PACKAGENAME + "=?",
                    new String[] { info.packageName });
            return count > 0;
        } catch (Exception e) {
            LogUtil.d(TAG, "queryDBDatas e: " + e);
            e.printStackTrace();
            return false;
        }
    }

    public boolean remove(Object object) {

        boolean memActionSuccess = super.remove(object);
        boolean dbActionSuccess = updateDBData(object);
        LogUtil.d(TAG, "remove memActionSuccess: " + memActionSuccess
                + ", dbActionSuccess: " + dbActionSuccess);
        return memActionSuccess && dbActionSuccess;
    }
}
