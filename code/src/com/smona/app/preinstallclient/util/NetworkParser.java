package com.smona.app.preinstallclient.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkParser {
    private Context mContext;
    private TelephonyManager mTm;

    private static final String SUBTYPE_WIFI = "wifi";
    private static final String SUBTYPE_2G = "2g";
    private static final String SUBTYPE_3G = "3g";
    private static final String SUBTYPE_4G = "4g";
    private static final String SUBTYPE_UNKNOWN = "unknown";

    public NetworkParser(Context context) {
        mContext = context;
        mTm = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
    }

    public String getNetworkType() {
        String subType = getSubType(getCurNetworkInfo());
        if (subType != null) {
            if (subType.equals(SUBTYPE_WIFI)) {
                return SUBTYPE_WIFI;
            }
            return getOperator() + subType;
        }
        return null;
    }

    private NetworkInfo getCurNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    private String getOperator() {
        String imsi = mTm.getSubscriberId();
        if (imsi != null) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
                return "cm";
            }
            if (imsi.startsWith("46001")) {
                return "un";
            }
            if (imsi.startsWith("46003")) {
                return "net";
            }
        }
        return null;
    }

    private String getSubType(NetworkInfo info) {
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                return mapNetworkTypeToName(mTm.getNetworkType());
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return SUBTYPE_WIFI;
            }
        }
        return null;
    }

    private String mapNetworkTypeToName(int networkType) {
        switch (networkType) {
        case TelephonyManager.NETWORK_TYPE_CDMA:
        case TelephonyManager.NETWORK_TYPE_EDGE:
        case TelephonyManager.NETWORK_TYPE_GPRS:
        case TelephonyManager.NETWORK_TYPE_1xRTT:
        case TelephonyManager.NETWORK_TYPE_IDEN:
            return SUBTYPE_2G;

        case TelephonyManager.NETWORK_TYPE_UMTS:
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
        case TelephonyManager.NETWORK_TYPE_EVDO_B:
        case TelephonyManager.NETWORK_TYPE_HSDPA:
        case TelephonyManager.NETWORK_TYPE_HSPA:
        case TelephonyManager.NETWORK_TYPE_HSUPA:
        case TelephonyManager.NETWORK_TYPE_EHRPD:
        case TelephonyManager.NETWORK_TYPE_HSPAP:
            return SUBTYPE_3G;

        case TelephonyManager.NETWORK_TYPE_LTE:
            return SUBTYPE_4G;

        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
        default:
            return SUBTYPE_UNKNOWN;
        }
    }

    static final class NetworkTypeList {
        // 2G
        public static final String NETWORK_CDMA = "CDMA"; // 2G;
        public static final String NETWORK_EDGE = "EDGE"; // 2.75G
        public static final String NETWORK_GPRS = "GPRS"; // 2.5G
        public static final String NETWORK_1X_RTT = "1xRTT"; // 2G
        public static final String NETWORK_IDEN = "iDen"; // 2G
        // 3G
        public static final String NETWORK_UMTS = "UMTS"; // 3G
        public static final String NETWORK_EVDO_0 = "EVDO_0"; // 3G
        public static final String NETWORK_EVDO_A = "EVDO_A"; // 3G
        public static final String NETWORK_EVDO_B = "EVDO_B"; // 3G

        public static final String NETWORK_HSDPA = "HSDPA"; // 3G
        public static final String NETWORK_HSUPA = "HSUPA"; // 3G
        public static final String NETWORK_HSPA = "HSPA"; // 3G
        public static final String NETWORK_EHRPD = "EHRPD"; // 3G
        public static final String NETWORK_HSPAP = "HSPAP"; // 3G

        public static final String NETWORK_LTE = "LTE"; // 4G

        public static final String NETWORK_UNKOWN = "Unknown"; // Unknown
    }
}
