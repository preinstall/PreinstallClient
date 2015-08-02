package com.smona.app.preinstallclient.data.cache;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.smona.app.preinstallclient.data.AbstractDataSource;
import com.smona.app.preinstallclient.data.ItemInfo;

public class CacheDataSource extends AbstractDataSource {

    public CacheDataSource(Context context) {
        super(context);
    }

    public void initDatas() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager manager = mContext.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);
        for (ResolveInfo info : list) {
            ItemInfo item = new ItemInfo();
            item.packageName = info.activityInfo.packageName;
            item.className = info.activityInfo.applicationInfo.className;
            item.appName = (String) info.loadLabel(manager);
            mDatas.add(item);
        }
    }
}
