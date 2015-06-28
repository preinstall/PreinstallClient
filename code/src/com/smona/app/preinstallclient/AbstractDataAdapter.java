package com.smona.app.preinstallclient;

import java.util.HashMap;

import com.smona.app.preinstallclient.data.IDataSource;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.download.DownloadInfo;
import com.smona.app.preinstallclient.util.LogUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class AbstractDataAdapter extends BaseAdapter {
    private static final String TAG = "ContentDataAdatper";
    protected IDataSource mDataSource;
    protected Context mContext;
    protected LayoutInflater mLayoutInflater;
    protected ICallback mCallback;

    public AbstractDataAdapter(Context context, IDataSource datasource) {
        mContext = context;
        mDataSource = datasource;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void registerCallback(ICallback callback) {
        mCallback = callback;
    }

    @Override
    public int getCount() {
        return getNeedCount();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.getInfo(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    abstract View createView(int position, View convertView, ViewGroup parent);

    abstract int getNeedCount();

    public void remove(int pos) {
        ItemInfo info = (ItemInfo) getItem(pos);
        boolean success = mDataSource.remove(info);
        LogUtil.d(TAG, "AbstractDataAdapter remove success: " + success);
        if (success) {
            notifyDataSetChanged();
        }
    }

    public ItemInfo getItemInfo(int position) {
        return (ItemInfo) mDataSource.getInfo(position);
    }
    
    public void refreshUI(HashMap<String, DownloadInfo> values) {
        
    }
}
