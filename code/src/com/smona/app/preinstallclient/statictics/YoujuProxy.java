package com.smona.app.preinstallclient.statictics;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Environment;

import com.smona.app.preinstallclient.util.Config;
import com.youju.statistics.YouJuAgent;

public class YoujuProxy extends StaticticsProxy {
    private static final String STATISTICS_ID = "statisticsId";
    private Map<String, Object> mDataMap = null;
    static final Object S_LOCK = new Object();
    private static boolean sSupportYouju = Config.STATICTICS_YOUJU;
    private static final String YOUJU_LOCK_PATH = Environment
            .getExternalStorageDirectory().toString() + "/youju1234567890";

    public YoujuProxy() {
        synchronized (S_LOCK) {
            if (sSupportYouju) {
                initYoujuLock();
                mDataMap = new HashMap<String, Object>(1);
            }
        }
    }

    private void initYoujuLock() {
        File file = new File(YOUJU_LOCK_PATH);
        if (file.exists()) {
            sSupportYouju = false;
        } else {
            sSupportYouju = true;
        }
    }

    public void init(Context context) {
        synchronized (S_LOCK) {
            if (sSupportYouju) {
                YouJuAgent.init(context);
                setAssociateUserImprovementPlanForStatistics(context);
            }
        }
    }

    public void onResume(Context context) {
        synchronized (S_LOCK) {
            if (sSupportYouju) {
                YouJuAgent.onResume(context);
            }
        }
    }

    public void onPause(Context context) {
        synchronized (S_LOCK) {
            if (sSupportYouju) {
                YouJuAgent.onPause(context);
            }
        }
    }

    public void setReportUncaughtExceptionsForStatistics() {
        synchronized (S_LOCK) {
            if (sSupportYouju) {
                YouJuAgent.setReportUncaughtExceptions(false);
            }
        }
    }

    public void onErrorForStatistics(Context context, Throwable throwable) {
        synchronized (S_LOCK) {
            if (sSupportYouju) {
                YouJuAgent.onError(context, throwable);
            }
        }
    }

    private void setAssociateUserImprovementPlanForStatistics(Context context) {
        synchronized (S_LOCK) {
            if (sSupportYouju) {
                YouJuAgent.setAssociateUserImprovementPlan(context, false);
            }
        }
    }

    public void onEventForStatistics(Context context, String eventId,
            String eventLabel, long id) {
        synchronized (S_LOCK) {
            if (sSupportYouju) {
                if (null == mDataMap) {
                    mDataMap = new HashMap<String, Object>(1);
                }
                if (!mDataMap.isEmpty()) {
                    mDataMap.clear();
                }
                mDataMap.put(STATISTICS_ID, id);
                YouJuAgent.onEvent(context, eventId, eventLabel, mDataMap);
            }
        }
    }

    public void onEventForStatistics(Context context, String eventId,
            String eventLabel, Map<String, Object> map) {
        synchronized (S_LOCK) {
            if (sSupportYouju) {
                YouJuAgent.onEvent(context, eventId, eventLabel, map);
            }
        }
    }
}
