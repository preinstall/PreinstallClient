package com.smona.app.preinstallclient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.smona.app.preinstallclient.control.IconCache;
import com.smona.app.preinstallclient.control.ImageCacheStrategy;
import com.smona.app.preinstallclient.control.RequestDataStategy;
import com.smona.app.preinstallclient.data.IDataSource;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.data.db.ClientSettings;
import com.smona.app.preinstallclient.data.db.MainDataSource;
import com.smona.app.preinstallclient.image.BitmapProcess;
import com.smona.app.preinstallclient.util.Constant;
import com.smona.app.preinstallclient.util.HttpUtils;
import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.util.ParseJsonString;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class UpdateInfoService extends Service {
	private static final String TAG = "UpdateInfoService";
	private static final int UPDATAWEATHER = 0X10;
	private ClientApplication mApp;
	private final int GOTOBROADCAST = 0X20;

	public static final String BROADCASTACTION = "com.jone.broad";

	Timer timer;
	private IconCache mIconCache;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() {
		mApp = (ClientApplication) getApplication();
		mIconCache = mApp.getIconCache();
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// 定时更新
				loadData();

			}
		}, 0, RequestDataStategy.WAIT_TIME);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {



		return super.onStartCommand(intent, flags, startId);
	}

	private void loadData() {
		requestHttpDatas();
	}

	private void requestHttpDatas() {
		boolean needRequestData = RequestDataStategy.INSTANCE.isNeedRetryQuestData(mApp);
		LogUtil.d(TAG, "requestData: needRequestData: " + needRequestData);
		if (!needRequestData) {
			return;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("version", "2.0");
		String jsonString = HttpUtils.postData(ProcessModel.getUrl(), map);
		boolean success = HttpUtils.isRequestDataSuccess(jsonString);
		LogUtil.d(TAG, "requestData: success: " + success + ", jsonString: " + jsonString);
		if (success) {
			List<ItemInfo> datas = ParseJsonString.parseJsonToItems(jsonString);
			ProcessModel.filterDulicateMemory(mApp,datas);
			ProcessModel.requestIcons(mApp, mIconCache,datas);
			ProcessModel.filterDulicateDB(mApp,datas);
			ProcessModel.saveToDB(mApp,datas);
			RequestDataStategy.INSTANCE.saveLastRequestDataTime(mApp);
		} else {

		}
	}


	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
		}
	}

}
