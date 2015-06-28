package com.smona.app.preinstallclient.download;

import android.app.DownloadManager.Request;
import android.net.Uri;

public class DownloadRequest extends Request {

    public DownloadRequest(Uri uri) {
        super(uri);
    }

}
