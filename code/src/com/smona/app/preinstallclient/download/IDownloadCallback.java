package com.smona.app.preinstallclient.download;

import java.util.HashMap;

public interface IDownloadCallback {
    void refreshUI(HashMap<String, DownloadInfo> values);
}
