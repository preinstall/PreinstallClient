package com.smona.app.preinstallclient.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.widget.Toast;

public class CommonUtil {
    public static void showMessge(Context context, int resid) {
        Toast.makeText(context, resid, Toast.LENGTH_SHORT).show();
    }

    public static void showMessge(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static boolean checkWifiInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != info && info.getState().equals(NetworkInfo.State.CONNECTED)) {
            return true;
        }
        return false;
    }

    public static boolean hasNetworkInfo(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .getState();
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState();
        if (wifi == State.CONNECTED || mobile == State.CONNECTED) {
            return true;
        }
        return false;
    }

    public static boolean checkMobileNetworkInfo(Context context) {
        ConnectivityManager connectManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectManager.getActiveNetworkInfo();
        if (null != info && info.getState().equals(NetworkInfo.State.CONNECTED)
                && info.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }
}
