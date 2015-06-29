package com.smona.app.preinstallclient;

import java.util.HashMap;

import com.smona.app.preinstallclient.ProcessModel.Callbacks;
import com.smona.app.preinstallclient.control.DragController;
import com.smona.app.preinstallclient.control.DropTarget;
import com.smona.app.preinstallclient.data.IDataSource;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.download.DownloadHandler;
import com.smona.app.preinstallclient.download.DownloadInfo;
import com.smona.app.preinstallclient.download.DownloadObsever;
import com.smona.app.preinstallclient.download.DownloadProxy;
import com.smona.app.preinstallclient.download.IDownloadCallback;
import com.smona.app.preinstallclient.util.CommonUtil;
import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.view.ContainerSpace;
import com.smona.app.preinstallclient.view.DragLayer;
import com.smona.app.preinstallclient.view.DropTargetBar;
import com.smona.app.preinstallclient.view.PageControlView;
import com.smona.app.preinstallclient.view.ScrollLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.HandlerThread;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class MainActivity extends BaseActivity implements OnItemClickListener, OnItemLongClickListener, Callbacks, OnClickListener, IDownloadCallback {

	private static final String TAG = "MainActivity";

	// layout
	private DragLayer mDragLayer = null;
	private ContainerSpace mContainer = null;
	private View mNetworkInfo;
	private NetworkReceiver mReceiver;

	// work thread and download
	private DownloadObsever mObserver;
	private DownloadHandler mWorkHandler = null;
	private HandlerThread mWorkThread = null;
	private HashMap<String, DownloadInfo> downloadInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initView();
		registerNetwork();
		initDownload();
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
		getContentResolver().registerContentObserver(DownloadHandler.DOWNLOAD_URI, true, mObserver);
	}

	private void destroyDownload() {
		if (mWorkThread != null) {
			mWorkThread.quit();
			mWorkThread = null;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
		LogUtil.d(TAG, "onClick: " + view + ", parent: " + parent + ", position: " + position + ", tag: " + view.getTag());
		onActionClickYes(view);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (downloadInfo != null && downloadInfo.size() != 0) {
			ItemInfo tag = (ItemInfo) view.getTag();
			if (tag != null) {
				DownloadInfo info = downloadInfo.get(tag.appUrl);
				if (info != null && info.status == DownloadProxy.STATUS_RUNNING) {
					return false;
				}
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
		// autoWifiDownload(dataSource);
	}

	private void onActionClickYes(View view) {
		Object obj = view.getTag();
		LogUtil.d(TAG, "view: " + view + ", obj: " + obj);
		if (obj instanceof ItemInfo) {
			DownloadProxy.getInstance().equeue((ItemInfo) obj);
		}
	}

	private void autoWifiDownload(IDataSource dataSource) {
		if (CommonUtil.checkWifiInfo(this)) {
			int size = dataSource.getCount(true);
			for (int i = 0; i < size; i++) {
				ItemInfo ifo = (ItemInfo) dataSource.getInfo(i);
				DownloadProxy.getInstance().equeue(ifo);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		actionNetwork();
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
		destroyDownload();
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
		downloadInfo = values;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mContainer != null) {
					mContainer.refreshUI(values);
				}
			}
		});
	}
}
