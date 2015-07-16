package com.smona.app.preinstallclient.view;

import java.util.HashMap;

import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.control.ImageLoaderManager;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.data.ItemInfo.OnDownListener;
import com.smona.app.preinstallclient.download.DownloadProxy;
import com.smona.app.preinstallclient.util.LogUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Element extends LinearLayout implements OnDownListener {
    private static final String TAG = "Element";

    private ProgressView mProgress;
    private TextView mTitle;
    private TextView mStatus;
    private ImageView mImage;
    private RelativeLayout relayoutDownstatue;
    private ImageView new_flag;

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
        new_flag = (ImageView) findViewById(R.id.new_flag);
        relayoutDownstatue = (RelativeLayout) findViewById(R.id.relayoutDownstatue);
    }

    private void initDatas() {
        STATUS_MAPS.put(DownloadProxy.STATUS_PENDING, -1);
        STATUS_MAPS.put(DownloadProxy.STATUS_RUNNING,
                R.string.download_downloading);
        STATUS_MAPS.put(DownloadProxy.STATUS_PAUSED, R.string.download_pause);
        STATUS_MAPS.put(DownloadProxy.STATUS_FAILED, R.string.download_failed);
        STATUS_MAPS.put(DownloadProxy.STATUS_SUCCESSFUL,
                R.string.download_install);
    }

    @Override
    public void onProgress(int progressTotal, int progress) {
        mProgress.setProgressTotal(progressTotal);
        mProgress.updateProgress(progress);
        if (progress == 0) {
            relayoutDownstatue.setVisibility(View.VISIBLE);
        } else {
            relayoutDownstatue.setVisibility(View.GONE);
        }
        LogUtil.d(TAG,
                "onProgress getProgressTotal:  " + mProgress.getProgressTotal()
                        + "onProgress progress:  " + progress);
    }

    @SuppressLint("NewApi")
    @Override
    public void onStatusChange(int status) {
        Integer resid = STATUS_MAPS.get(status);
        if (resid != null && resid > 0) {
            mStatus.setBackgroundDrawable(null);
            mStatus.setText(resid);
        }
        if (status != ItemInfo.STATUS_INIT) {
            relayoutDownstatue.setVisibility(View.GONE);
        } else {
            relayoutDownstatue.setVisibility(View.VISIBLE);
        }
        if (status == ItemInfo.STATUS_SUCCESSFUL) {
            mProgress.setProgressTotal(1);
            mProgress.updateProgress(1);
        }
    }

    public void initUI(ItemInfo info) {
        ImageLoaderManager.getInstance().loadImage(info.appIconUrl, mImage);
        mTitle.setText(info.appName);
        if (ItemInfo.NEW_FLAG == info.isnew) {
            new_flag.setVisibility(View.VISIBLE);
        } else {
            new_flag.setVisibility(View.GONE);
        }
        onStatusChange(info.downloadStatus);
        setTag(info);
    }
}
