package com.smona.app.preinstallclient.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
//import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class Utils {
    private static final String TAG = "Preinstallation";

    public static final int KB = 1024;
    public static final int MB = KB * KB;

    public static final int NETWORK_NO_NET = -1;
    public static final int NETWORK_WIFI = 0;
    public static final int NETWORK_MOBILE = 1;

    public static final int SD_NOT_MOUNTED = -1;
    public static final int SD_LOW_SPACE = 0;
    public static final int SD_ENOUGH_ROOM = 1;

    private static final String PROP_BRAND = "ro.product.brand";
    private static final String PROP_MODEL = "ro.product.model";
    private static final String PROP_GNROMVER = "ro.gn.gnromvernumber";
    private static final String PROP_ANDROIDVER = "ro.build.version.release";

    private static final String NULL = "null";
    private static final int MIN_SPACE_REQUIRED = 15 * MB;

    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    private static final boolean IS_GIONEE_BRAND;

    static {
        String brand = getSystemProp(PROP_BRAND, null);
        if (brand != null && brand.trim().equalsIgnoreCase("GiONEE")) {
            IS_GIONEE_BRAND = true;
        } else {
            IS_GIONEE_BRAND = false;
        }
    }

    public static boolean isGioneeBrand() {
        return IS_GIONEE_BRAND;
    }

    public static boolean deleteFile(String fileName) {
        if (deleteFile(StorageUtils.getHomeDirAbsolute(), fileName)) {
            return true;
        }
        if (StorageUtils.isMutilCacheDir()
                && deleteFile(StorageUtils.getAPKDownloadDir(), fileName)) {
            return true;
        }
        return false;
    }

    private static boolean deleteFile(String parentFolder, String fileName) {
        File file = new File(parentFolder + File.separator + fileName);
        if (!file.exists()) {
            return false;
        }
        if (file.delete()) {
            return true;
        }
        return false;
    }

    public static boolean renameFile(String srcName, String destName) {
        boolean succeeded = false;
        File srcFile = new File(StorageUtils.getAPKDownloadDir()
                + File.separator + srcName);
        if (srcFile.exists()) {
            File destFile = new File(StorageUtils.getAPKDownloadDir()
                    + File.separator + destName);
            if (srcFile.renameTo(destFile)) {
                succeeded = true;
            }
            LogUtil.d(TAG, "renameFile file exist,succeeded=" + succeeded);
            return succeeded;
        }
        LogUtil.d(TAG, "renameFile file not exist");
        return succeeded;
    }

    public static boolean hasNetwork(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return (null == mConnectivityManager.getActiveNetworkInfo()) ? false
                : true;
    }

    public static int getNetworkType(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return NETWORK_WIFI;
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return NETWORK_MOBILE;
            }
        }
        return NETWORK_NO_NET;
    }

    public static int checkSDCard(String fileSize) { // fileSize:MB
        if (!StorageUtils.isSDCardMounted()) {
            return SD_NOT_MOUNTED;
        }
        float size = 0;
        try {
            Matcher matcher = Pattern.compile("[M*m*B*b*]").matcher(fileSize);
            if (matcher.find()) {
                fileSize = fileSize.substring(0, matcher.start());
            }
            LogUtil.d(TAG, "checkSDCard fileSize=" + fileSize);
            size = Float.parseFloat(fileSize);
        } catch (NumberFormatException e) {
            LogUtil.d(TAG, "checkSDCard e: " + e.getMessage());
        }
        StatFs stat = new StatFs(StorageUtils.getAPKDownloadCard());
        if ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize() > MIN_SPACE_REQUIRED
                + (int) size * MB) {
            return SD_ENOUGH_ROOM;
        }
        return SD_LOW_SPACE;
    }

    @SuppressLint("NewApi")
    public static boolean checkSdkVersion(String sdkName) {
        boolean match = false;
        String sdkVersion = getAndroidVersion();
        LogUtil.d(TAG, "checkSdkVersion sdkName=" + sdkName + " sdkVersion="
                + sdkVersion);
        if (sdkVersion == null || TextUtils.isEmpty(sdkName)) {
            return true;
        }
        if (sdkVersion.compareTo(sdkName) >= 0) {
            match = true;
        }
        return match;
    }

    public static String getDeviceModel() {
        String model = getSystemProp(PROP_MODEL, NULL);
        if (!NULL.equals(model)) {
            model = model.trim().replaceAll(" ", "+");
        }
        return model;
    }

    @SuppressLint("DefaultLocale")
    public static String getClientVersion(Context context) {
        try {
            String packageName = context.getPackageName();
            String versionName = context.getPackageManager().getPackageInfo(
                    packageName, 0).versionName;
            versionName = versionName.toLowerCase();
            if (versionName.startsWith("v")) {
                versionName = versionName.substring(1);
            }
            return versionName;
        } catch (NameNotFoundException e) {
            return NULL;
        }
    }

    public static String getGioneeRomVersion() {
        String version = getSystemProp(PROP_GNROMVER, NULL);
        LogUtil.d(TAG, "getGioneeRomVersion gionee romnum=" + version);
        if (!NULL.equals(version)) {
            Matcher matcher = Pattern.compile("[0-9]").matcher(version);
            if (matcher.find()) {
                version = version.substring(matcher.start());
            }
        }
        return version;
    }

    public static String getAndroidVersion() {
        String version = getSystemProp(PROP_ANDROIDVER, NULL);
        return version;
    }

    public static int[] getPhonePixels(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return new int[] { metrics.widthPixels, metrics.heightPixels };
    }

    public static String getSubNetwork(Context context) {
        NetworkParser parser = new NetworkParser(context);
        return parser.getNetworkType();
    }

    public static String getEncodeIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Service.TELEPHONY_SERVICE);
        return GNEncodeIMEIUtils.get(tm.getDeviceId());
    }

    public static boolean isSamePackage(Context context, String fileName,
            String gamePackage) {
        boolean match = false;
        String filePath = StorageUtils.getAPKDownloadDir() + File.separator
                + fileName;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(filePath,
                    PackageManager.GET_ACTIVITIES);
            if (info != null && info.packageName != null
                    && info.packageName.equals(gamePackage)) {
                match = true;
            }
        } catch (Exception e) {
            match = true;
        }
        LogUtil.d(TAG, "isSamePackage match=" + match);
        return match;
    }

    public static String getUrlArg(String url, String key) {
        String value = "";
        if (url == null) {
            return value;
        }
        String[] strings = url.split("&");
        for (String string : strings) {
            if (string.contains(key + "=")) {
                value = string.substring(string.indexOf("=") + 1);
                break;
            }
        }
        LogUtil.d(TAG, "getUrlArg value=" + value);
        return value;
    }

    @SuppressLint("NewApi")
    public static void launchGame(Context context, String packageName) {
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.setPackage(packageName);
        List<ResolveInfo> list = context.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        Iterator<ResolveInfo> iterator = list.iterator();
        if (iterator.hasNext()) {
            ResolveInfo ri = iterator.next();
            String className = ri.activityInfo.name;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static String getSystemProp(String key, String defVal) {
        return "test";// SystemProperties.get(key, defVal);
    }

    public static String getFileMd5(File file) {
        byte[] digest = null;
        FileInputStream in = null;
        if (file == null) {
            return NULL;
        }
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] bytes = new byte[8192];
            in = new FileInputStream(file);
            int byteCount;
            while ((byteCount = in.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }
            digest = digester.digest();
        } catch (Exception cause) {
            throw new RuntimeException("Unable to compute MD5 of \"" + file
                    + "\"", cause);
        } finally {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (Exception e) {
                }

            }
        }
        return (digest == null) ? null : byteArrayToString(digest);
    }

    private static String byteArrayToString(byte[] bytes) {
        StringBuilder ret = new StringBuilder(bytes.length << 1);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(Character.forDigit((bytes[i] >> 4) & 0xf, 16));
            ret.append(Character.forDigit(bytes[i] & 0xf, 16));
        }
        return ret.toString();
    }

    public static boolean isUrlInvalid(String url) {
        if (url.startsWith(HTTP) || url.startsWith(HTTPS)) {
            return false;
        }
        return true;
    }

    public static boolean isTestEnv() {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + Constant.TEST_ENV_FILE);
        return file.exists();
    }

    public static boolean needSilentInstall() {
        // return false;
        return IS_GIONEE_BRAND;
    }

    // Gionee <tangjing><2014-04-01> add for CR01160903 begin
    public static long getRandomTimeMs() {
        long max = 5 * 60 * 60 * 1000;
        return getRandomValue(max, 0);
    }

    public static long getRandomValue(long max, long min) {
        return ((long) (Math.random() * (max - min))) + min;
    }
    // Gionee <tangjing><2014-04-01> add for CR01160903 end
}
