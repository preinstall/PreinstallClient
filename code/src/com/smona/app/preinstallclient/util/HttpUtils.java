package com.smona.app.preinstallclient.util;

import android.annotation.SuppressLint;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import com.smona.app.preinstallclient.util.LogUtil;

public class HttpUtils {

    private static final String TAG = "HttpUtils";
    private static final int CONN_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 8000;
    public static final String JSON_SIGN = "GioneePreinstallation";

    public static String post(String actionUrl) {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL url = new URL(actionUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", "UTF-8");

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                int ch;
                StringBuilder res = new StringBuilder();
                while ((ch = is.read()) != -1) {
                    res.append((char) ch);
                }
                return res.toString();
            }
        } catch (Exception e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return "fail";
    }

    public static String postData(String actionUrl, Map<String, String> params) {
        HttpURLConnection conn = null;
        InputStream is = null;
        OutputStream op = null;
       // byte[] data = getRequestData(params, "UTF-8").getBytes();
        try {
            URL url = new URL(actionUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);

            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

//            conn.setRequestProperty("Content-Length",
//                    String.valueOf(data.length));

            op = conn.getOutputStream();
            //op.write(data);

            int response = conn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                int ch;
                StringBuilder res = new StringBuilder();
                while ((ch = is.read()) != -1) {
                    res.append((char) ch);
                }
                return res.toString();
            }
        } catch (IOException e) {
            LogUtil.d(TAG, "postData actionUrl=" + actionUrl + ", map: "
                    + params + ", e: " + e);
            e.printStackTrace();
        } finally {
            if (op != null) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
                try {
                    op.close();
                } catch (IOException e) {
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return "fail";
    }

    public static String getRequestData(Map<String, String> params,
            String encode) {
        StringBuffer stringBuffer = new StringBuffer();
        params.put("ptype", Utils.getDeviceModel());
        /*
         * params.put("imei", GnPreinstallationImpl.getImei());
         * params.put("client", GnPreinstallationImpl.getClientVersion());
         */
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    @SuppressLint("NewApi")
    public static boolean isRequestDataSuccess(String data) {
        if (data == null) {
            return false;
        }
        if (data.isEmpty() || HttpUtils.hasGioneeSign(data)) {
            return true;
        }
        return false;
    }

    private static boolean hasGioneeSign(String jsonInfo) {
        return jsonInfo != null && jsonInfo.contains(HttpUtils.JSON_SIGN);
    }

}
