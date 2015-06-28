package com.smona.app.preinstallclient.control;

import java.util.HashMap;

import com.smona.app.preinstallclient.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class IconCache {
    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
    private Context mContext;

    private Object mLock = new Object();
    private final HashMap<String, Bitmap> mCache = new HashMap<String, Bitmap>(
            INITIAL_ICON_CACHE_CAPACITY);

    public IconCache(Context context) {
        mContext = context;
    }

    public Bitmap cacheBitmap(String fileName) {
        synchronized (mLock) {
            Bitmap icon = mCache.get(fileName);
            if (icon == null) {
                icon = ImageCacheStrategy.getInstance().getBitmap(fileName);
                mCache.put(fileName, icon);
            }
            if (icon == null) {
                Drawable drawable = mContext.getResources().getDrawable(
                        R.drawable.appicon);
                icon = ((BitmapDrawable) drawable).getBitmap();
                mCache.put(fileName, icon);
            }
            return icon;
        }
    }
}
