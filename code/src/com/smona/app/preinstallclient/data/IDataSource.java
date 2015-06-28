package com.smona.app.preinstallclient.data;

public interface IDataSource {
    int getCount(boolean isRealCount);

    Object getInfo(int position);

    boolean remove(Object object);

    void init();

    void copy(IDataSource datasource);
}
