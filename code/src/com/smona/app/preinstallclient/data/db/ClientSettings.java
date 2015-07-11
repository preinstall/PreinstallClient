package com.smona.app.preinstallclient.data.db;

import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.download.DownloadProxy;

import android.net.Uri;
import android.provider.BaseColumns;

public class ClientSettings {
    public static final String DATABASE_NAME = "preinstallclient.db";
    public static final int DATABASE_VERSION = 1;

    private static final String AUTHORITY = "com.smona.app.preinstallclient.clientsettings";
    static final String PARAMETER_NOTIFY = "notify";

    private static final String TEXT = " text, ";
    private static final String INTEGER = " Integer";
    private static final String FLOAT = " Float, ";
    private static final String PRIMARY_KEY = " text primary key, ";

    public static class ItemColumns implements BaseColumns {
        public static final String TABLE_NAME = "preinstall";

        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + TABLE_NAME);
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri
                .parse("content://" + AUTHORITY + "/" + TABLE_NAME + "?"
                        + PARAMETER_NOTIFY + "=false");

        public static final String APPID = "appid";
        public static final String PACKAGENAME = "packageName";
        public static final String APPCLASS = "appClass";
        public static final String APPNAME = "appName";
        public static final String APPURL = "appUrl";
        public static final String APPSIZE = "appSize";
        public static final String APPICONURL = "appIconUrl";
        public static final String SDKVERSION = "sdkVersion";
        public static final String INDEX = "appindex";
        public static final String ISNEW = "isnew";
        public static final String DOWNLOADSTATUS = "downloadStatus";
        public static final String DOWNLOADFILEPATH = "downloadfilepath";

        public static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME + "/"
                    + id + "?" + PARAMETER_NOTIFY + "=" + notify);
        }

        public static final String SQL_CREATE_TABLE = "CREATE TABLE "
                + TABLE_NAME + " ( " + APPID + TEXT + PACKAGENAME + PRIMARY_KEY
                + APPCLASS + TEXT + APPNAME + TEXT + APPURL + TEXT + APPSIZE
                + FLOAT + APPICONURL + TEXT + SDKVERSION + TEXT + DOWNLOADFILEPATH + TEXT + ISNEW + INTEGER +" , " + INDEX + INTEGER +" , "+ DOWNLOADSTATUS + INTEGER 
                + ")";
    }

}
