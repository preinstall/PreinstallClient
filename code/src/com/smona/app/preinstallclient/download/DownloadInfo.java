package com.smona.app.preinstallclient.download;

public class DownloadInfo {
    public long downloadID;
    public String downloadPath;
    public String url;
    public int total;
    public int process;
    public int status;

    public String toString() {
        return "DownloadInfo[downloadID: " + downloadID + ", url: " + url
                + ", path: " + downloadPath + ", total: " + total
                + ", process: " + process + ", status: " + status + "]";
    }
}
