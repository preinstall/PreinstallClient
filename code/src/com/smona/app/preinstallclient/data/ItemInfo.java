package com.smona.app.preinstallclient.data;

import com.smona.app.preinstallclient.view.Element;

public class ItemInfo {
    public static final int NEW_FLAG = 1;
    public static final int UNNEW_FLAG = 0;
    
    public static int STATUS_INIT = Element.State.NONE.ordinal();
    public static int STATUS_RUNNING = Element.State.DOWNLOADING.ordinal();
    public static int STATUS_PAUSED = Element.State.DOWNLOADING.ordinal();
    public static int STATUS_FAILED = Element.State.NONE.ordinal();
    public static int STATUS_SUCCESSFUL = Element.State.DOWNLOADED.ordinal();

    public String appid;
    public String appName;
    public String appUrl;
    public float appSize;
    public String appIconUrl;
    public String sdkVersion;
    public String packageName;
    public String className;
    public Element.State downloadStatus;
    public float downloadPercent;
    public int isnew = NEW_FLAG;
    public int appindex = 0;
    public String downloadFilePath;

    public String toString() {
        return "packageName: " + packageName + ", className: " + className
                + ", appid: " + appid + ", appName: " + appName + ", appUrl: "
                + appUrl + ", appIconUrl: " + appIconUrl + ", sdkVersion: "
                + sdkVersion + "========downloadStatus: " + downloadStatus
                + ", downloadPercent: " + downloadPercent;
    }

    public interface OnDownListener {
        public void onProgress(int progressTotal, int progress);

        void onStatusChange(int status);
    }
}
