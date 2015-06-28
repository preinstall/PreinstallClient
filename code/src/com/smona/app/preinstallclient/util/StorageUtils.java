package com.smona.app.preinstallclient.util;

import java.io.File;

import com.smona.app.preinstallclient.util.LogUtil;

import android.os.Environment;

public class StorageUtils {
    private static final String TAG = "StorageUtils";
    private static final String PREINSTALLATION_FOLDER = "preinstallation";
    private static final String APPICON_FOLDER = "app_icon";
    private static final String SDCARD_DIR = Environment
            .getExternalStorageDirectory().getAbsolutePath();

    private StorageUtils() {

    }

    public static void init() {
        File file = new File(SDCARD_DIR + File.separator
                + PREINSTALLATION_FOLDER + File.separator + APPICON_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private static String sAPKDownloadDir = null;

    private static boolean hasAPKDownloadDir() {
        return sAPKDownloadDir != null;
    }

    public static String getAPKDownloadDir() {
        return hasAPKDownloadDir() ? sAPKDownloadDir : getHomeDirAbsolute();
    }

    public static String getAPKDownloadCard() {
        String apkDir = getAPKDownloadDir();
        if (apkDir.contains(PREINSTALLATION_FOLDER)) {
            int end = apkDir.indexOf(PREINSTALLATION_FOLDER) - 1;
            return apkDir.substring(0, end);
        }
        return null;
    }

    public static void setAPKDownloadDir(String downloadDir) {
        LogUtil.d(TAG, "setAPKDownloadDir: " + downloadDir);
        sAPKDownloadDir = downloadDir;
    }

    public static boolean isMutilCacheDir() {
        String homeDirAbsolute = getHomeDirAbsolute();
        if (homeDirAbsolute == null) {
            return true;
        }
        return !homeDirAbsolute.equals(sAPKDownloadDir);
    }

    public static boolean isSDCardMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    public static String getSDCardDir() {
        if (isSDCardMounted()) {
            return SDCARD_DIR;
        }
        return null;
    }

    public static String getHomeDir() {
        if (isSDCardMounted()) {
            return PREINSTALLATION_FOLDER;
        }
        return null;
    }

    public static String getAppIconDirAbsolute() {
        return SDCARD_DIR + File.separator + PREINSTALLATION_FOLDER
                + File.separator + APPICON_FOLDER;
    }

    public static String getHomeDirAbsolute() {
        if (getHomeDir() != null) {
            return getSDCardDir() + File.separator + PREINSTALLATION_FOLDER;
        }
        return null;
    }

}
