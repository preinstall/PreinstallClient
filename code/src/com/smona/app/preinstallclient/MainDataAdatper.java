package com.smona.app.preinstallclient;

import java.util.ArrayList;
import java.util.HashMap;

import com.smona.app.preinstallclient.data.IDataSource;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.download.DownloadInfo;
import com.smona.app.preinstallclient.download_ex.PreInstallAppManager;
import com.smona.app.preinstallclient.view.Element;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint({ "InflateParams", "ViewHolder" })
public class MainDataAdatper extends AbstractDataAdapter {

    private ArrayList<View> mVisibleViews = new ArrayList<View>();

    public MainDataAdatper(Context context, IDataSource datasource) {
        super(context, datasource);
        this.mContext = context;
    }

    @Override
    public View createView(int position, View convertView, ViewGroup parent) {
        ItemInfo info = (ItemInfo) mDataSource.getInfo(position);
        if (convertView == null) {
            convertView = createElement();
        }
        setConvertView(convertView, info);
        return convertView;
    }

    @SuppressLint("NewApi")
    private View createElement() {
        View convertView = mLayoutInflater.inflate(R.layout.item_main, null);
        return convertView;
    }

    private void setConvertView(View convertView, ItemInfo info) {
        Element element = (Element) convertView;
        element.initUI(info);
        PreInstallAppManager.getPreInstallAppManager(mContext).setListener(info.packageName, element);
        PreInstallAppManager.getPreInstallAppManager(mContext).setInfoDownloadListener(info.packageName, info);
    }

    public int getNeedCount() {
        return mDataSource.getCount(false);
    }

    public void refreshUI(HashMap<String, DownloadInfo> values) {
    }

    public ArrayList<View> getmVisibleViews() {
        return mVisibleViews;
    }

    public void setmVisibleViews(ArrayList<View> mVisibleViews) {
        this.mVisibleViews = mVisibleViews;
    }

}
