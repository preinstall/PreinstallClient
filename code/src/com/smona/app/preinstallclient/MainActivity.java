package com.smona.app.preinstallclient;

import com.smona.app.preinstallclient.ProcessModel.Callbacks;
import com.smona.app.preinstallclient.control.DragController;
import com.smona.app.preinstallclient.control.DropTarget;
import com.smona.app.preinstallclient.data.IDataSource;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.data.db.ClientSettings;
import com.smona.app.preinstallclient.download.DownloadObsever;
import com.smona.app.preinstallclient.download.DownloadProxy;
import com.smona.app.preinstallclient.download_ex.PreInstallAppManager;
import com.smona.app.preinstallclient.util.CommonUtil;
import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.view.CommonConfirmDialog;
import com.smona.app.preinstallclient.view.ContainerSpace;
import com.smona.app.preinstallclient.view.DragLayer;
import com.smona.app.preinstallclient.view.DropTargetBar;
import com.smona.app.preinstallclient.view.Element;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class MainActivity extends BaseActivity implements OnItemClickListener,
        OnItemLongClickListener, Callbacks, OnClickListener {
    private static final String SP_NAME = "preinstallclient";
    private static final String BIG_APP_DOWNLOAD_NAME = "preinstallclient_big_app_download";
    private static final String TAG = "MainActivity";
    // layout
    private DragLayer mDragLayer = null;
    private ContainerSpace mContainer = null;
    private View mNetworkInfo;
    private NetworkReceiver mReceiver;

    // work thread and download
    private SharedPreferences mSharePer;
    private CommonConfirmDialog mConfirmDialog;
    private ItemInfo mItemIfo;
    private DownloadObsever mDataChangeObserver;
    private boolean mIsFirstIn = true;

    @SuppressLint("HandlerLeak")
    private Handler mDataChangedHandler = new Handler() {
        public void handleMessage(Message msg) {
            initData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initView();
        registerNetwork();
        initData();
        
        initDownload();

        startupService();
    }

    private void startupService() {
        Intent autoRefreshDataIntent = new Intent(getApplicationContext(),
                AutoRefreshDataService.class);
        startService(autoRefreshDataIntent);

        Intent autoDownloadIntent = new Intent(getApplicationContext(),
                AutoDownloadInWifiService.class);
        startService(autoDownloadIntent);
    }

    private void initView() {
        mDragLayer = (DragLayer) findViewById(R.id.draglayer);
        DragLayer dragLayer = mDragLayer;
        DragController dragController = new DragController(this);
        dragLayer.setup(this, dragController);

        mNetworkInfo = findViewById(R.id.no_network);
        mNetworkInfo.findViewById(R.id.no_network_set).setOnClickListener(this);
        mNetworkInfo.findViewById(R.id.no_network_try).setOnClickListener(this);

        mContainer = (ContainerSpace) findViewById(R.id.container);
        mContainer.setup(this, dragController);

        DropTargetBar dropTarget = (DropTargetBar) findViewById(R.id.droptarget);
        dropTarget.setup(this, dragController);

        mSharePer = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    private void initData() {
        LogUtil.d(TAG, "motianhu  initData ");
        ClientApplication app = (ClientApplication) getApplication();
        ProcessModel model = app.getProcessModel();
        model.initialize(this);
        model.startLoadTask();

        mIsFirstIn = true;
    }

    private void registerNetwork() {
        mReceiver = new NetworkReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void unRegisterNetwork() {
        unregisterReceiver(mReceiver);
    }

    private void initDownload() {
        mDataChangeObserver = new DownloadObsever(mDataChangedHandler);
        getContentResolver().registerContentObserver(
                ClientSettings.ItemColumns.CONTENT_URI, true,
                mDataChangeObserver);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view,
            int position, long id) {
        LogUtil.d(TAG, "onClick: " + view + ", parent: " + parent
                + ", position: " + position + ", tag: " + view.getTag());
        Object obj = view.getTag();
        LogUtil.d(TAG, "view: " + view + ", obj: " + obj);
        if (obj instanceof ItemInfo) {
            mItemIfo = (ItemInfo) obj;
        }
        if (mItemIfo.isnew == ItemInfo.NEW_FLAG) {
            mItemIfo.isnew = ItemInfo.UNNEW_FLAG;
            view.findViewById(R.id.new_flag).setVisibility(View.GONE);
            updateNewNewFalg(mItemIfo);
        }
        PreInstallAppManager.getPreInstallAppManager(this).execute(
                (Element) view, this);

    }

    private void updateNewNewFalg(ItemInfo itemInfo) {
        ContentValues values = new ContentValues();
        values.put(ClientSettings.ItemColumns.ISNEW, ItemInfo.UNNEW_FLAG);
        ProcessModel.updateDB(this, itemInfo.packageName, values);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        Object obj = view.getTag();
        LogUtil.d(TAG, "view: " + view + ", obj: " + obj);
        if (obj instanceof ItemInfo) {
            mItemIfo = (ItemInfo) obj;
            if (mItemIfo.downloadStatus == Element.State.DOWNLOADING) {
                return false;
            }
        }
        mContainer.startDrag(view, position);
        return false;
    }

    public DragLayer getDragLayer() {
        return mDragLayer;
    }

    public DropTarget getDefaultDropTarget() {
        return mContainer;
    }

    @Override
    public void bindItems(IDataSource dataSource) {
        mContainer.setDataSource(dataSource);
        if (mIsFirstIn) {
            mIsFirstIn = false;
            autoWifiDownload(dataSource);
        }
    }

    private void autoWifiDownload(IDataSource dataSource) {
        if (CommonUtil.checkWifiInfo(this)) {
            int size = dataSource.getCount(true);
            for (int i = 0; i < size; i++) {
                ItemInfo info = (ItemInfo) dataSource.getInfo(i);
                if (info.downloadStatus == Element.State.DOWNLOADING) {
                    DownloadProxy.getInstance().equeue(info);
                }
            }
        }
    }

    private void actionNetwork() {
        boolean hasNetwork = CommonUtil.hasNetworkInfo(this);
        if (hasNetwork) {
            mNetworkInfo.setVisibility(View.GONE);
            mContainer.setNetworkStatus(true);
            initData();
        } else {
            mNetworkInfo.setVisibility(View.VISIBLE);
            mContainer.setNetworkStatus(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterNetwork();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.no_network_set:
            setNetwork();
            break;
        case R.id.no_network_try:
            tryNetwork();
            break;
        }
    }

    private void tryNetwork() {
        actionNetwork();
    }

    private void setNetwork() {
        Intent intent = new Intent("android.net.wifi.PICK_WIFI_NETWORK");
        startActivity(intent);
    }

    private class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "NetworkReceiver onReceive: intent=" + intent);
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                actionNetwork();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private void showBigApkConfirmDialogAndChangeToWifi() {
        boolean bNeedShowSizeConfirm = mSharePer.getBoolean(
                BIG_APP_DOWNLOAD_NAME, true);
        if (bNeedShowSizeConfirm == true) {
            String title = getString(R.string.check_download_title);
            String showMsg = getString(R.string.check_size_message);
            mConfirmDialog = new CommonConfirmDialog(this,
                    R.layout.common_confirm_dialog, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1 || msg.what == 3) {
                                if (msg.what == 1) {
                                    mSharePer
                                            .edit()
                                            .putBoolean(BIG_APP_DOWNLOAD_NAME,
                                                    false).commit();
                                }
                                if (mConfirmDialog != null) {
                                    mConfirmDialog.dismiss();
                                    mConfirmDialog = null;
                                }
                                changeToWifi();
                            } else {
                                if (mConfirmDialog != null) {
                                    mConfirmDialog.dismiss();
                                    mConfirmDialog = null;
                                }
                                downloadData();
                            }
                        }
                    }, title, showMsg, getString(R.string.turn_on_wifi),
                    getString(R.string.continue_download));
            mConfirmDialog.setCanceledOnTouchOutside(false);
            mConfirmDialog.show();
            Window window = mConfirmDialog.getWindow();
            window.setGravity(Gravity.CENTER);
        }
    }

    private void downloadData() {
        DownloadProxy.getInstance().equeue(mItemIfo);
    }

    private void changeToWifi() {
        setNetwork();
    }

}
