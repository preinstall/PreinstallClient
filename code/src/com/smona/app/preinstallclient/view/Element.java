package com.smona.app.preinstallclient.view;

import java.util.HashMap;

import com.smona.app.preinstallclient.ProcessModel;
import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.control.ImageLoaderManager;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.data.db.ClientSettings;
import com.smona.app.preinstallclient.download_ex.PreInstallAppManager.DownloadListener;
import com.smona.app.preinstallclient.util.LogUtil;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Element extends LinearLayout implements DownloadListener {
    private static final String TAG = "Element";

    public enum State {
        NONE, ON_DWONLOAD, DOWNLOAD_FINISH, DWONLOADED_NOT_INSTALL, ON_INSTALL, INSTALLED
    }

    protected State mState = State.NONE;

    private ProgressView mProgress;
    private TextView mTitle;
    private TextView mStatus;
    private ImageView mImage;
    private RelativeLayout relayoutDownstatue;
    private ImageView new_flag;

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
        new_flag = (ImageView) findViewById(R.id.new_flag);
        relayoutDownstatue = (RelativeLayout) findViewById(R.id.relayoutDownstatue);
    }

    private void initDatas() {
        STATUS_MAPS.put(State.NONE, -1);
        STATUS_MAPS.put(State.ON_DWONLOAD, R.string.download_downloading);
        STATUS_MAPS.put(State.DOWNLOAD_FINISH, R.string.download_pause);
        STATUS_MAPS.put(State.DWONLOADED_NOT_INSTALL, R.string.download_failed);
        STATUS_MAPS.put(State.ON_INSTALL, R.string.download_install);
        STATUS_MAPS.put(State.INSTALLED, R.string.download_installed);
    }

    private void onStatusChange(int status) {
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

    @Override
    public void onDownloadProgress(int progress, int total) {
        LogUtil.d(TAG, "onProgress onDownloadProgress [" + progress + ","
                + total + "]");
        mProgress.setProgressTotal(total);
        mProgress.updateProgress(progress);
        if (progress == 0) {
            relayoutDownstatue.setVisibility(View.VISIBLE);
        } else {
            relayoutDownstatue.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDownloadstateChanged(String appPkg, State state) {
        LogUtil.d(TAG, "onProgress onDownloadstateChanged [" + state + "]");
        mState = state;
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
        if (state == State.DOWNLOAD_FINISH) {
            mProgress.setProgressTotal(1);
            mProgress.updateProgress(1);
        }
        
        ContentValues contentValues = new ContentValues();
        contentValues.put(ClientSettings.ItemColumns.DOWNLOADSTATUS,
                state.ordinal());
        ProcessModel.updateDB(getContext(), appPkg, contentValues);
    }

    @Override
    public void onInstallStateChanged(String appPkg, State state) {
        LogUtil.d(TAG, "onProgress onInstallStateChanged [" + state + "], is: "
                + state.ordinal());
        mState = state;
        ContentValues contentValues = new ContentValues();
        contentValues.put(ClientSettings.ItemColumns.DOWNLOADSTATUS,
                state.ordinal());
        ProcessModel.updateDB(getContext(), appPkg, contentValues);
    }

    public State getPreAppState() {
        return mState;
    }
}
