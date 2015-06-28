package com.smona.app.preinstallclient.data;

public class ItemInfo {
    public String appid;
    public String appName;
    public String appUrl;
    public float appSize;
    public String appIconUrl;
    public String sdkVersion;
    public String packageName;
    public String className;
    public int downloadStatus;
    public float downloadPercent;

    public String toString() {
        return "packageName: " + packageName + ", className: " + className
                + ", appid: " + appid + ", appName: " + appName + ", appUrl: "
                + appUrl + ", appIconUrl: " + appIconUrl + ", sdkVersion: "
                + sdkVersion + "========downloadStatus: " + downloadStatus
                + ", downloadPercent: " + downloadPercent;
    }

    public interface OnDownListener {
        void onProgress(int progress);

        void onStatusChange(int status);
    }
}
