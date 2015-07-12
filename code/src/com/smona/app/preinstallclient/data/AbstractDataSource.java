package com.smona.app.preinstallclient.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public abstract class AbstractDataSource implements IDataSource {
    private static final int DEFAULT_COLUMNS = 3;
    private static final int DEFAULT_ROWS = 3;
    private static final int DEFAULT_COUNT = DEFAULT_ROWS * DEFAULT_COLUMNS;
    protected Context mContext;

    protected List<ItemInfo> mDatas;

    public AbstractDataSource(Context context) {
        mContext = context;
        mDatas = new ArrayList<ItemInfo>();
    }

    public void init() {
        initDatas();
    }

    private int getLimitCount() {
        int count = getRealCount();
        return count > DEFAULT_COUNT ? DEFAULT_COUNT : count;
    }

    private int getRealCount() {
        return mDatas.size();
    }

    public Object getInfo(int position) {
        return getItemInfo(position);
    }

    public ItemInfo getItemInfo(int position) {
        return mDatas.get(position);
    }

    public int getCount(boolean isRealCount) {
        if (isRealCount) {
            return getRealCount();
        } else {
            return getLimitCount();
        }
    }

    public boolean remove(Object object) {
        return mDatas.remove(object);
    }

    public void copy(IDataSource datasource) {
        mDatas.clear();
        mDatas.addAll(((AbstractDataSource) datasource).mDatas);
    }

    public void copy(IDataSource datasource, int start, int end) {
        mDatas.clear();
        for (int i = start; i < ((AbstractDataSource) datasource).mDatas.size()
                && (i < end); i++) {
            mDatas.add(((AbstractDataSource) datasource).mDatas.get(i));
        }
    }

    abstract protected void initDatas();
}
