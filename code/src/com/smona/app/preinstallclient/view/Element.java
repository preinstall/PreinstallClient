package com.smona.app.preinstallclient.view;

import java.util.HashMap;

import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.control.ImageLoaderManager;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.download_ex.PreInstallAppManager.DownloadListener;
import com.smona.app.preinstallclient.util.LogUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Element extends LinearLayout implements DownloadListener {
    private static final String TAG = "Element";

    public enum State {
        NONE, DOWNLOADING, DOWNLOADED, DWONLOADED_NOT_INSTALL, INSTALLING, INSTALLED
    }

    protected State mState = State.NONE;

    private ProgressView mProgress;
    private TextView mTitle;
    private TextView mStatus;
    private ImageView mImage;
    private RelativeLayout relayoutDownstatue;

    @SuppressLint("UseSparseArrays")
    private static final HashMap<State, Integer> STATUS_MAPS = new HashMap<State, Integer>();

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
        relayoutDownstatue = (RelativeLayout) findViewById(R.id.relayoutDownstatue);
    }

    private void initDatas() {
        STATUS_MAPS.put(State.NONE, -1);
        STATUS_MAPS.put(State.DOWNLOADING, R.string.download_downloading);
        STATUS_MAPS.put(State.DOWNLOADED, R.string.download_finish);
        STATUS_MAPS.put(State.DWONLOADED_NOT_INSTALL, R.string.download_finish);
        STATUS_MAPS.put(State.INSTALLING, R.string.download_install);
        STATUS_MAPS.put(State.INSTALLED, R.string.download_installed);
    }

    public void initUI(ItemInfo info) {
        ImageLoaderManager.getInstance().loadImage(info.appIconUrl, mImage);
        mTitle.setText(info.appName);
        LogUtil.d(TAG, "motinahu initUI info: " + info);
        setViewStatus(info.downloadStatus);
        setTag(info);
    }

    @Override
    public void onDownloadProgress(int progress, int total) {
        LogUtil.d(TAG, "onProgress onDownloadProgress [" + progress + ","
                + total + "]");
        mProgress.setProgressTotal(total);
        mProgress.updateProgress(progress);
        if (mState != State.NONE) {
            relayoutDownstatue.setVisibility(View.GONE);
        } else {
            relayoutDownstatue.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDownloadstateChanged(String appPkg, State state) {
        LogUtil.d(TAG, "onProgress onDownloadstateChanged [" + state + "]");
        setViewStatus(state);
    }

    private void setViewStatus(State state) {
        Integer resid = STATUS_MAPS.get(state);
        if (resid != null && resid > 0) {
            mStatus.setBackgroundDrawable(null);
            mStatus.setText(resid);
        }
        if (state != State.NONE) {
            relayoutDownstatue.setVisibility(View.GONE);
        } else {
            relayoutDownstatue.setVisibility(View.VISIBLE);
        }
        if (state == State.DOWNLOADED || state == State.DWONLOADED_NOT_INSTALL
                || state == State.INSTALLED || state == State.INSTALLING) {
            mProgress.setProgressTotal(1);
            mProgress.updateProgress(1);
            mStatus.setTextColor(Color.WHITE);
        }
        changeTagStatus(state);
    }

    @Override
    public void onInstallStateChanged(String appPkg, State state) {
        LogUtil.d(TAG, "onProgress onInstallStateChanged [" + state + "], is: "
                + state.ordinal());
        changeTagStatus(state);
    }

    private void changeTagStatus(State state) {
        Object obj =  (ItemInfo) getTag();
        mState = state;
        if(obj instanceof ItemInfo) {
            ((ItemInfo)obj).downloadStatus = state;
        }
    }
}
