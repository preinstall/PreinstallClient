package com.smona.app.preinstallclient.data.db;

import com.smona.app.preinstallclient.util.LogUtil;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ClientProvider extends ContentProvider {
    private static final String TAG = "ClientProvider";
    private static final boolean DEBUG = true;

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        if (DEBUG) {
            LogUtil.d(TAG, "onCreate()");
        }
        mOpenHelper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.mTable);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = qb.query(db, projection, args.mWhere, args.mArgs, null,
                null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.mWhere)) {
            return "vnd.android.cursor.dir/" + args.mTable;
        } else {
            return "vnd.android.cursor.item/" + args.mTable;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SqlArguments args = new SqlArguments(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert(args.mTable, null, values);
        if (rowId <= 0) {
            LogUtil.d(TAG, "insert() uri=" + uri + ", values: " + values
                    + ", rowId: " + rowId);
            return null;
        }
        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(ClientSettings.ItemColumns.TABLE_NAME, where,
                whereArgs);
        if (count > 0) {
            sendNotify(uri);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.mTable, values, args.mWhere, args.mArgs);
        if (count > 0) {
            sendNotify(uri);
        }
        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(ClientSettings.PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    static class SqlArguments {
        public final String mTable;
        public final String mWhere;
        public final String[] mArgs;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.mTable = url.getPathSegments().get(0);
                this.mWhere = where;
                this.mArgs = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException(
                        "WHERE clause not supported: " + url);
            } else {
                this.mTable = url.getPathSegments().get(0);
                this.mWhere = "_id=" + ContentUris.parseId(url);
                this.mArgs = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                mTable = url.getPathSegments().get(0);
                mWhere = null;
                mArgs = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
}
