package com.smona.app.preinstallclient.view;

import java.util.HashMap;

import com.smona.app.preinstallclient.ClientApplication;
import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.control.IconCache;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.data.ItemInfo.OnDownListener;
import com.smona.app.preinstallclient.download.DownloadProxy;
import com.smona.app.preinstallclient.util.LogUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Element extends LinearLayout implements OnDownListener {
    private static final String TAG = "Element";

    private ProgressView mProgress;
    private TextView mTitle;
    private TextView mStatus;
    private ImageView mImage;

    @SuppressLint("UseSparseArrays")
    private static final HashMap<Integer, Integer> STATUS_MAPS = new HashMap<Integer, Integer>();

    public Element(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDatas();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProgress = (ProgressView) findViewById(R.id.download_progress);
        mStatus = (TextView) findViewById(R.id.download_status);
        mTitle = (TextView) findViewById(R.id.title);
        mImage = (ImageView) findViewById(R.id.image);
    }

    private void initDatas() {
        STATUS_MAPS.put(DownloadProxy.STATUS_PENDING, -1);
        STATUS_MAPS.put(DownloadProxy.STATUS_RUNNING, R.string.download_pause);
        STATUS_MAPS
                .put(DownloadProxy.STATUS_PAUSED, R.string.download_continue);
        STATUS_MAPS.put(DownloadProxy.STATUS_FAILED, R.string.download_failed);
        STATUS_MAPS.put(DownloadProxy.STATUS_SUCCESSFUL,
                R.string.download_install);
    }

    @Override
    public void onProgress(int progress) {
        mProgress.updateProgress(progress);
        LogUtil.d(TAG, "onProgress progress:  " + progress);
    }

    @SuppressLint("NewApi")
    @Override
    public void onStatusChange(int status) {
        LogUtil.d(TAG, "onStatusChange status:  " + status);
        Integer resid = STATUS_MAPS.get(status);
        if (resid != null && resid > 0) {
            mStatus.setBackground(null);
            mStatus.setText(resid);
        }
    }

    public void initUI(ItemInfo info) {
        Context context = getContext();
        IconCache iconCache = ((ClientApplication) context
                .getApplicationContext()).getIconCache();
        Bitmap bg = iconCache.cacheBitmap(info.packageName);
        mImage.setBackground(new BitmapDrawable(context.getResources(), bg));
        mTitle.setText(info.appName);
        mProgress.setProgressTotal(info.appSize);
        setTag(info);
    }
}
