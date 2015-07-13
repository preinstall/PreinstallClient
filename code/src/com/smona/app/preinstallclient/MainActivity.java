package com.smona.app.preinstallclient;

import java.util.HashMap;

import com.smona.app.preinstallclient.ProcessModel.Callbacks;
import com.smona.app.preinstallclient.control.DragController;
import com.smona.app.preinstallclient.control.DropTarget;
import com.smona.app.preinstallclient.data.IDataSource;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.data.db.ClientSettings;
import com.smona.app.preinstallclient.download.DownloadHandler;
import com.smona.app.preinstallclient.download.DownloadInfo;
import com.smona.app.preinstallclient.download.DownloadObsever;
import com.smona.app.preinstallclient.download.DownloadProxy;
import com.smona.app.preinstallclient.download.IDownloadCallback;
import com.smona.app.preinstallclient.util.CommonUtil;
import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.util.PackageUtils;
import com.smona.app.preinstallclient.view.CommonConfirmDialog;
import com.smona.app.preinstallclient.view.ContainerSpace;
import com.smona.app.preinstallclient.view.DragLayer;
import com.smona.app.preinstallclient.view.DropTargetBar;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class MainActivity extends BaseActivity implements OnItemClickListener,
        OnItemLongClickListener, Callbacks, OnClickListener, IDownloadCallback {
    private static final float APK_MAX_SIZE = 15;
    private static final String SP_NAME = "preinstallclient";
    private static final String INIT_NAME = "preinstallclient_init";
    private static final String BIG_APP_DOWNLOAD_NAME = "preinstallclient_big_app_download";
    private static final String TAG = "MainActivity";
    private MainActivity _this;
    // layout
    private DragLayer mDragLayer = null;
    private ContainerSpace mContainer = null;
    private View mNetworkInfo;
    private NetworkReceiver mReceiver;

    // work thread and download
    private DownloadObsever mObserver;
    private DownloadHandler mWorkHandler = null;
    private HandlerThread mWorkThread = null;
    private DownloadCompleteReceiver downloadCompleteReceiver = new DownloadCompleteReceiver();
    private SharedPreferences sp;
    private CommonConfirmDialog confirmDialog;
    private ItemInfo itemIfo;
    private DownloadObsever mdbObserver;
    private boolean bFirstIn = true;
    private Handler dbChangedHandler = new Handler() {
        public void handleMessage(Message msg) {
            initData();
        }
    };

    ServiceConnection conn = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        bFirstIn = true;
        _this = this;
        initView();
        registerNetwork();
        initData();
        initDownload();
        //showFirstConfirmDialog();

        Intent intent = new Intent(getApplicationContext(),
                UpdateInfoService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
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

        sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    private void initData() {
        ClientApplication app = (ClientApplication) getApplication();
        ProcessModel model = app.getProcessModel();
        model.initialize(this);
        model.startLoadTask();
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
        destroyDownload();
        mWorkThread = new HandlerThread("Downloading");
        mWorkThread.start();
        mWorkHandler = new DownloadHandler(mWorkThread.getLooper(), this);
        mWorkHandler.setCallback(this);
        mObserver = new DownloadObsever(mWorkHandler);
        mdbObserver = new DownloadObsever(dbChangedHandler);
        getContentResolver().registerContentObserver(
                DownloadHandler.DOWNLOAD_URI, true, mObserver);
        getContentResolver().registerContentObserver(
                ClientSettings.ItemColumns.CONTENT_URI, true, mdbObserver);
    }

    private void destroyDownload() {
        if (mWorkThread != null) {
            mWorkThread.quit();
            mWorkThread = null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view,
            int position, long id) {
        LogUtil.d(TAG, "onClick: " + view + ", parent: " + parent
                + ", position: " + position + ", tag: " + view.getTag());
        Object obj = view.getTag();
        LogUtil.d(TAG, "view: " + view + ", obj: " + obj);
        if (obj instanceof ItemInfo) {
            itemIfo = (ItemInfo) obj;
            if (itemIfo == null) {
                return;
            }
            if (itemIfo.downloadStatus == ItemInfo.STATUS_SUCCESSFUL) {
                new Thread() {
                    public void run() {
                        PackageUtils.install(_this, itemIfo.downloadFilePath);
                    }
                }.start();
                return;
            }
            if (itemIfo.isnew == ItemInfo.NEW_FLAG) {
                view.findViewById(R.id.new_flag).setVisibility(View.GONE);
                updateNewNewFalg(itemIfo);
            }
            boolean bNeedShowSizeConfirm = sp.getBoolean(BIG_APP_DOWNLOAD_NAME,
                    true);
            if (itemIfo.appSize >= APK_MAX_SIZE && bNeedShowSizeConfirm
                    && CommonUtil.checkMobileNetworkInfo(_this)) {
                showBigApkConfirmDialogAndChangeToWifi();
            } else {
                downloadDate();
            }
        }

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
            itemIfo = (ItemInfo) obj;
            if (itemIfo == null) {
                return false;
            }
            if (itemIfo.downloadStatus == ItemInfo.STATUS_RUNNING) {
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
        if (bFirstIn) {
            bFirstIn = false;
            autoWifiDownload(dataSource);
        }
    }

    private void onActionClickYes(View view) {

    }

    private void autoWifiDownload(IDataSource dataSource) {
        if (CommonUtil.checkWifiInfo(this)) {
            int size = dataSource.getCount(true);
            for (int i = 0; i < size; i++) {
                ItemInfo info = (ItemInfo) dataSource.getInfo(i);
                if (info.downloadStatus == ItemInfo.STATUS_PAUSED) {
                    DownloadProxy.getInstance().equeue(info);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        actionNetwork();
        registerReceiver(downloadCompleteReceiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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
        unregisterReceiver(downloadCompleteReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterNetwork();
        destroyDownload();
        unbindService(conn);
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

    @Override
    public void refreshUI(final HashMap<String, DownloadInfo> values) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mContainer != null) {
                    mContainer.refreshUI(values);
                }
            }
        });
    }

    private void showFirstConfirmDialog() {
        boolean bNeedShowFirstConfirm = sp.getBoolean(INIT_NAME, true);
        if (bNeedShowFirstConfirm == true) {
            String title = getString(R.string.check_download_title);
            String showMsg = getString(R.string.check_download_message);
            confirmDialog = new CommonConfirmDialog(
                    this,
                    R.layout.common_confirm_dialog,
                    new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                sp.edit().putBoolean(INIT_NAME, false).commit();
                                if (confirmDialog != null) {
                                    confirmDialog.dismiss();
                                    confirmDialog = null;
                                }
                            } else {
                                if (confirmDialog != null) {
                                    confirmDialog.dismiss();
                                    confirmDialog = null;
                                }
                                if (msg.what == 0 || msg.what == 2) {
                                    finish();
                                }
                            }
                        }
                    }, title, showMsg, getString(R.string.check_data_yes),
                    getString(R.string.check_download_no));
            confirmDialog.setCanceledOnTouchOutside(false);
            confirmDialog.show();
            Window window = confirmDialog.getWindow();
            window.setGravity(Gravity.CENTER);
        }
    }

    private void showBigApkConfirmDialogAndChangeToWifi() {
        boolean bNeedShowSizeConfirm = sp.getBoolean(BIG_APP_DOWNLOAD_NAME,
                true);
        if (bNeedShowSizeConfirm == true) {
            String title = getString(R.string.check_download_title);
            String showMsg = getString(R.string.check_size_message);
            confirmDialog = new CommonConfirmDialog(this,
                    R.layout.common_confirm_dialog, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1 || msg.what == 3) {
                                if (msg.what == 1) {
                                    sp.edit()
                                            .putBoolean(BIG_APP_DOWNLOAD_NAME,
                                                    false).commit();
                                }
                                if (confirmDialog != null) {
                                    confirmDialog.dismiss();
                                    confirmDialog = null;
                                }
                                changeToWifi();
                            } else {
                                if (confirmDialog != null) {
                                    confirmDialog.dismiss();
                                    confirmDialog = null;
                                }
                                downloadDate();
                            }
                        }
                    }, title, showMsg, getString(R.string.turn_on_wifi),
                    getString(R.string.continue_download));
            confirmDialog.setCanceledOnTouchOutside(false);
            confirmDialog.show();
            Window window = confirmDialog.getWindow();
            window.setGravity(Gravity.CENTER);
        }
    }

    private void downloadDate() {
        DownloadProxy.getInstance().equeue(itemIfo);
    }

    private void changeToWifi() {
        setNetwork();
    }

}
