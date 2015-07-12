package com.smona.app.preinstallclient.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.smona.app.preinstallclient.data.ItemInfo;

@SuppressLint("NewApi")
public class ParseJsonString {
    private static final String TAG = "ParseJsonString";
    private static final String DATA = "data";
    private static final String ITEM = "items";
    private static final String APP_ID = "appId";
    private static final String PACKAGE_NAME = "packageName";
    private static final String APP_NAME = "appName";
    private static final String APP_URL = "appUrl";
    private static final String APP_SIZE = "appSize";
    private static final String APPICON_URL = "appIconUrl";
    private static final String APP_CLASS = "appClass";
    private static final String SDK_VERDION = "sdkVersion";

    public static List<ItemInfo> parseJsonToItems(String jsonString) {
        if (!jsonString.isEmpty()) {
            try {
                String data = new JSONObject(jsonString).getString(DATA);
                return ParseJsonString.parseJsonToItem(data);
            } catch (JSONException e) {
                LogUtil.d(TAG, "parseJsonToItems e: " + e);
                e.printStackTrace();
            }
        }
        return null;
    }

    private static List<ItemInfo> parseJsonToItem(String jsonString) {
        List<ItemInfo> values = new ArrayList<ItemInfo>();
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray jsonArray = json.getJSONArray(ITEM);
            String size;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject js = jsonArray.getJSONObject(i);
                ItemInfo info = new ItemInfo();
                info.appid = js.getString(APP_ID);
                info.packageName = js.getString(PACKAGE_NAME);
                info.appName = js.getString(APP_NAME);
                info.appUrl = js.getString(APP_URL);
                size = js.getString(APP_SIZE);
                info.appSize = Float.valueOf(size.substring(0,
                        size.length() - 2));
                info.appIconUrl = js.getString(APPICON_URL);
                info.className = js.getString(APP_CLASS);
                info.sdkVersion = js.getString(SDK_VERDION);
                info.downloadStatus = ItemInfo.STATUS_INIT;
                values.add(info);
            }
        } catch (Exception e) {
            LogUtil.d(TAG, "parseJsonToItem jsonString: " + jsonString
                    + ", exception" + e);
            e.printStackTrace();
        }
        return values;
    }
}
