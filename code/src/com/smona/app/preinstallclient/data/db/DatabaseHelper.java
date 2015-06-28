package com.smona.app.preinstallclient.data.db;

import com.smona.app.preinstallclient.util.LogUtil;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final boolean DEBUG = true;
    private static DatabaseHelper sInstance = null;

    static DatabaseHelper getInstance(Context context) {
        if (isDatabaseHelperNull()) {
            synchronized (DatabaseHelper.class) {
                if (sInstance == null) {
                    sInstance = new DatabaseHelper(context);
                }
            }
        }
        return sInstance;
    }

    private static boolean isDatabaseHelperNull() {
        return sInstance == null ? true : false;
    }

    DatabaseHelper(Context context) {
        super(context, ClientSettings.DATABASE_NAME, null,
                ClientSettings.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (DEBUG) {
            LogUtil.d(TAG, "create new preinstallclient database");
        }
        db.execSQL(ClientSettings.ItemColumns.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean isPreInstallClientExist = tabIsExist(db,
                ClientSettings.ItemColumns.TABLE_NAME);
        LogUtil.d(TAG, "isPreInstallClientExist " + isPreInstallClientExist
                + ", newVersion=" + ", oldVersion=" + oldVersion);
    }

    private boolean tabIsExist(SQLiteDatabase db, String tabName) {
        boolean result = false;
        if (tabName == null) {
            return false;
        }
        Cursor c = null;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"
                    + tabName.trim() + "' ";
            c = db.rawQuery(sql, null);
            if (c.moveToNext()) {
                int count = c.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
            LogUtil.d(TAG, "table " + tabName + " not exist exception! e=" + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

}
