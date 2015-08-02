package com.smona.app.preinstallclient.download_ex;

import java.util.ArrayList;
import java.util.HashMap;

import com.gionee.preinstallation.commom.FactoryPreinstallation;
import com.gionee.preinstallation.commom.GnPreinstallation;
import com.gionee.preinstallation.data.PreinstallationArgs;
import com.gionee.preinstallation.download.DownloadInfo;
import com.smona.app.preinstallclient.ProcessModel;
import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.data.ItemInfo;
import com.smona.app.preinstallclient.data.db.ClientSettings;
import com.smona.app.preinstallclient.util.CommonUtil;
import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.view.Element;

import android.content.ContentValues;
import android.content.Context;
import android.widget.Toast;

public class PreInstallAppManager {

    private static final String TAG = "PreInstallAppManager";

    public static final String KEY_DOWNDLOAD_APP_ID = "appId";
    public static final String KEY_DOWNDLOAD_PACKAGE_NAME = "packageName";
    public static final String KEY_DOWNDLOAD_APP_NAME = "appName";
    public static final String KEY_DOWNDLOAD_APP_URL = "appUrl";
    public static final String KEY_DOWNDLOAD_APP_SIZE = "appSize";
    public static final String KEY_DOWNDLOAD_APP_ICON_URL = "appIconUrl";
    public static final String KEY_LOCAL_ICON_PATH = "localIconPath";
    public static final String KEY_DOWNLOAD_STATE = "downloadState";
    public static final String KEY_DOWNDLOAD_THEME_SINCE = "themeSince";
    public static final boolean SURPPOT_PREINSTALL = true;
    public static final boolean SHOW_PREINSTALL_FOLDER = false;
    public static final String INVENTED_CALSS_NAME = "com.invented.InventedActivity";
    public static final boolean IS_LAUNCHER_INTEGRATE_ON_SYSTEM = true;
    public static final int PREINSTALL_WIDGET_STATISTICS_ID = -1;

    private static PreInstallAppManager sInstance;
    static final Object S_LOCK = new Object();

    private GnPreinstallation mGnPreinstallation;
    private HashMap<String, Object> mDownloadListeners;
    private Context mContext;
    private HashMap<String, ItemInfo> mDownloadItems;

    private long mLastPreInstallShortcutClickTime = 0;

    private PreInstallAppManager(Context context) {
        mContext = context;
        mDownloadListeners = new HashMap<String, Object>();
        mDownloadItems = new HashMap<String, ItemInfo>();
    }

    public static PreInstallAppManager getPreInstallAppManager(Context context) {
        if (sInstance == null) {
            sInstance = new PreInstallAppManager(context);
        }
        return sInstance;
    }

    private PreinstallationArgs getPreinstallationArgs(ItemInfo info) {
        PreinstallationArgs args = null;
        String appId = info.appid;
        String packageName = info.packageName;
        String appName = info.appName;
        String appUrl = info.appUrl;
        String appSize = info.appSize + "";
        String appIconUrl = info.appIconUrl;
        args = new PreinstallationArgs(appId, packageName, appName, appUrl,
                appSize, appIconUrl, null);
        return args;
    }

    public void init(Context context) {
        if (mGnPreinstallation == null) {
            mGnPreinstallation = FactoryPreinstallation.getGnPreinstallation();
        }
        mGnPreinstallation.init(context, new PreInstallCallBack());
    }

    private void startDownload(ItemInfo info, DownloadListener listener) {
        String packageName = info.packageName;

        // add listener for the download item
        setListener(packageName, listener);
        setInfoDownloadListener(packageName, info);

        try {
            LogUtil.d(TAG, "start download preinstall item --> " + packageName);
            mGnPreinstallation.download(getPreinstallationArgs(info));
            listener.onDownloadstateChanged(packageName,
                    Element.State.DOWNLOADING);
            updateContentDB(packageName, Element.State.DOWNLOADING);
        } catch (IllegalArgumentException e) {
            LogUtil.d(TAG,
                    "start download preinstall item, throw exception --> " + e);
            e.printStackTrace();
            removeListener(packageName);
            return;
        } catch (Exception ex) {
            LogUtil.d(TAG,
                    "start download preinstall item, throw exception --> " + ex
                            + packageName);
            ex.printStackTrace();
            removeListener(packageName);
            return;
        }
    }

    private class PreInstallCallBack implements GnPreinstallation.Callback {

        @Override
        public void onDownloadFailed(String packageName, int erroCode) {
            DownloadListener listener = (DownloadListener) mDownloadListeners
                    .get(packageName);
            LogUtil.d(TAG, "download failed, packageName = " + packageName
                    + ", erroCode = " + erroCode + ", listener: " + listener);
            if (listener != null) {
                listener.onDownloadstateChanged(packageName, Element.State.NONE);
            }
            updateContentDB(packageName, Element.State.NONE);
            removeListener(packageName);
            showToast(R.string.dowanload_faile);
        }

        @Override
        public void onDownloadProgress(ArrayList<DownloadInfo> infos) {
            for (DownloadInfo info : infos) {
                if (info != null) {
                    int progress = info.getProgress();
                    int total = info.getTotal();
                    String pkgName = info.getPackageName();
                    DownloadListener listener = (DownloadListener) mDownloadListeners
                            .get(pkgName);
                    LogUtil.d(TAG, "onDownloadProgress getPackageName: "
                            + pkgName + ", listener: " + listener + ", total: "
                            + total + ", progress: " + progress);
                    if (listener != null) {
                        listener.onDownloadProgress(progress, total);
                    }
                }
            }
        }

        @Override
        public void onDownloadSucc(String oldPkg, String newPkg) {
            DownloadListener listener = (DownloadListener) mDownloadListeners
                    .get(oldPkg);

            if (listener != null) {
                listener.onDownloadstateChanged(oldPkg,
                        Element.State.DOWNLOADED);
            }
            removeListener(oldPkg);
            LogUtil.d(TAG, "download success ---> " + oldPkg
                    + ", apkPackageName: " + newPkg + ", listener: " + listener);
            updateContentDB(oldPkg, Element.State.DOWNLOADED);
        }

        @Override
        public void onSilentInstallStart(String oldPackageName,
                String apkPackageName) {
            DownloadListener listener = (DownloadListener) mDownloadListeners
                    .get(oldPackageName);
            LogUtil.d(TAG, "silent install start ---> " + oldPackageName
                    + ", listener: " + listener);
            if (listener != null) {
                listener.onInstallStateChanged(oldPackageName,
                        Element.State.INSTALLING);
            }
            updateContentDB(oldPackageName, Element.State.INSTALLING);
        }

        @Override
        public void onSilentInstallSucc(String packageName) {
            DownloadListener listener = (DownloadListener) mDownloadListeners
                    .get(packageName);
            LogUtil.d(TAG, "silent install sucess ---> " + packageName
                    + ", listener: " + listener);
            if (listener != null) {
                listener.onInstallStateChanged(packageName,
                        Element.State.INSTALLED);
            }
            removeListener(packageName);
            updateContentDB(packageName, Element.State.INSTALLED);
            ItemInfo info = removeInfoDownloadListener(packageName);
            if (info != null) {
                deleteDownloadTask(info);
            }
        }

        @Override
        public void onSilentInstallFailed(String packageName) {
            DownloadListener listener = (DownloadListener) mDownloadListeners
                    .get(packageName);
            LogUtil.d(TAG, "silent install failed : " + packageName
                    + ",listener: " + listener);
            if (listener != null) {
                listener.onInstallStateChanged(packageName,
                        Element.State.DWONLOADED_NOT_INSTALL);
            }
            removeListener(packageName);
            updateContentDB(packageName, Element.State.DWONLOADED_NOT_INSTALL);
        }

        @Override
        public void onSysInstallSucc(String packageName) {
            DownloadListener listener = (DownloadListener) mDownloadListeners
                    .get(packageName);
            ItemInfo info = removeInfoDownloadListener(packageName);
            LogUtil.d(TAG, "system install success---> " + packageName
                    + ",listener: " + listener + ", info: " + info);
            if (listener != null) {
                listener.onInstallStateChanged(packageName,
                        Element.State.INSTALLED);
            }
            updateContentDB(packageName, Element.State.INSTALLED);
            removeListener(packageName);
            if (info != null) {
                deleteDownloadTask(info);
            }
        }

        @Override
        public void onCheckDataFailed(int failReason) {
            LogUtil.d(TAG, "check the server's data failed: " + failReason);
        }

        @Override
        public void onCheckDataSucc(ArrayList<PreinstallationArgs> newData) {
            LogUtil.d(TAG, "check the server's succesfull data count = "
                    + newData);
        }

        @Override
        public void onFileError(String packageName) {
            LogUtil.d(TAG, "the file is broken or loss ---> " + packageName);
            DownloadListener listener = (DownloadListener) mDownloadListeners
                    .get(packageName);
            if (listener != null) {
                listener.onDownloadstateChanged(packageName, Element.State.NONE);
                updateContentDB(packageName, Element.State.NONE);
                Element shortcut = (Element) removeListener(packageName);
                ItemInfo info = (ItemInfo) shortcut.getTag();
                clickPreInstallShortcut(info, shortcut, shortcut.getContext(),
                        false);
            }
        }

        @Override
        public void onDownloadStart(String appName) {
            Toast.makeText(
                    mContext,
                    mContext.getResources().getString(R.string.begin_download)
                            + appName, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartSysInstall(String oldPkg, String newPkg) {
            DownloadListener listener = (DownloadListener) mDownloadListeners
                    .get(oldPkg);
            LogUtil.d(TAG, "start system install ---> " + oldPkg
                    + ",listener: " + listener + ", apkPackageName: " + newPkg);
            if (listener != null) {
                listener.onInstallStateChanged(oldPkg,
                        Element.State.DWONLOADED_NOT_INSTALL);
            }
            updateContentDB(oldPkg, Element.State.DWONLOADED_NOT_INSTALL);
        }
    }

    private void updateContentDB(String packageName, Element.State state) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ClientSettings.ItemColumns.DOWNLOADSTATUS,
                state.ordinal());
        ProcessModel.updateDB(mContext, packageName, contentValues);
    }

    public static interface DownloadListener {
        public void onDownloadProgress(int progress, int total);

        public void onDownloadstateChanged(String appPkg, Element.State state);

        public void onInstallStateChanged(String appPkg, Element.State state);
    }

    public void setListener(String key, DownloadListener listener) {
        mDownloadListeners.put(key, listener);
    }

    private Object removeListener(String key) {
        return mDownloadListeners.remove(key);
    }

    public void setInfoDownloadListener(String key, ItemInfo info) {
        mDownloadItems.put(key, info);
    }

    private ItemInfo removeInfoDownloadListener(String key) {
        ItemInfo info = mDownloadItems.remove(key);
        return info;
    }

    public void destroyGnPreinstallation() {
        FactoryPreinstallation.destoryGnPreinstallation();
    }

    public void execute(Element shortcut, Context context) {
        long time = System.currentTimeMillis();
        long delta = time - mLastPreInstallShortcutClickTime;
        LogUtil.d(TAG, "current state: " + shortcut.getTag() + ", delta: "
                + delta);

        if (delta <= 500) {
            return;
        }

        Object obj = shortcut.getTag();
        if (!(obj instanceof ItemInfo)) {
            return;
        }

        ItemInfo itemInfo = (ItemInfo) (shortcut.getTag());

        if (itemInfo.downloadStatus == Element.State.DOWNLOADING
                || itemInfo.downloadStatus == Element.State.INSTALLED) {

        } else if (itemInfo.downloadStatus == Element.State.DWONLOADED_NOT_INSTALL
                || itemInfo.downloadStatus == Element.State.DOWNLOADED
                || itemInfo.downloadStatus == Element.State.INSTALLING) {
            String packageName = itemInfo.packageName;
            setListener(packageName, shortcut);

            mGnPreinstallation.sysInstall(getPreinstallationArgs(itemInfo));
        } else if (itemInfo.downloadStatus == Element.State.NONE) {
            clickPreInstallShortcut(itemInfo, shortcut, context, true);
        }
        mLastPreInstallShortcutClickTime = System.currentTimeMillis();
    }

    private void clickPreInstallShortcut(final ItemInfo info,
            final Element view, final Context context, final boolean first) {
        if (CommonUtil.hasNetworkInfo(context)) {
            startDownload(info, view);
        } else {
            showToast(R.string.no_network_connection);
        }
    }

    public void deleteDownloadTask(ItemInfo info) {
        if (mGnPreinstallation == null) {
            return;
        }
        PreinstallationArgs args = getPreinstallationArgs(info);
        removeInfoDownloadListener(info.packageName);
        removeListener(info.packageName);
        mGnPreinstallation.deleteDownloadTask(args);
    }

    private void showToast(int resuorceId) {
        Toast.makeText(mContext, mContext.getResources().getString(resuorceId),
                Toast.LENGTH_SHORT).show();
    }
}