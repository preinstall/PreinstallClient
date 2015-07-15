package com.smona.app.preinstallclient.util;

import java.io.FileOutputStream;
import android.graphics.Bitmap;
import android.util.Log;

public final class LogUtil {

    public static final boolean DEBUG = true;

    private static final String MODULE_NAME = "Preinstall";
    private static final LogUtil INSTANCE = new LogUtil();

    /**
     * private constructor here, It is a singleton class.
     */
    private LogUtil() {
    }

    /**
     * The FileManagerLog is a singleton class, this static method can be used
     * to obtain the unique instance of this class.
     * 
     * @return The global unique instance of FileManagerLog.
     */
    public static LogUtil getInstance() {
        return INSTANCE;
    }

    /**
     * The method prints the log, level error.
     * 
     * @param tag
     *            the tag of the class.
     * @param msg
     *            the message to print.
     */
    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(MODULE_NAME, tag + ", " + msg);
        }
    }

    /**
     * The method prints the log, level error.
     * 
     * @param tag
     *            the tag of the class.
     * @param msg
     *            the message to print.
     * @param t
     *            an exception to log.
     */
    public static void e(String tag, String msg, Throwable t) {
        if (DEBUG) {
            Log.e(MODULE_NAME, tag + ", " + msg, t);
        }
    }

    /**
     * The method prints the log, level warning.
     * 
     * @param tag
     *            the tag of the class.
     * @param msg
     *            the message to print.
     */
    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(MODULE_NAME, tag + ", " + msg);
        }
    }

    /**
     * The method prints the log, level warning.
     * 
     * @param tag
     *            the tag of the class.
     * @param msg
     *            the message to print.
     * @param t
     *            an exception to log.
     */
    public static void w(String tag, String msg, Throwable t) {
        if (DEBUG) {
            Log.w(MODULE_NAME, tag + ", " + msg, t);
        }
    }

    /**
     * The method prints the log, level debug.
     * 
     * @param tag
     *            the tag of the class.
     * @param msg
     *            the message to print.
     */
    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(MODULE_NAME, tag + ", " + msg);
        }
    }

    /**
     * The method prints the log, level debug.
     * 
     * @param tag
     *            the tag of the class.
     * @param msg
     *            the message to print.
     * @param t
     *            an exception to log.
     */
    public static void i(String tag, String msg, Throwable t) {
        if (DEBUG) {
            Log.i(MODULE_NAME, tag + ", " + msg, t);
        }
    }

    /**
     * The method prints the log, level debug.
     * 
     * @param tag
     *            the tag of the class.
     * @param msg
     *            the message to print.
     */
    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.e(MODULE_NAME, tag + ", " + msg);
        }
    }

    /**
     * The method prints the log, level debug.
     * 
     * @param tag
     *            the tag of the class.
     * @param msg
     *            the message to print.
     * @param t
     *            An exception to log.
     */
    public static void d(String tag, String msg, Throwable t) {
        if (DEBUG) {
            Log.d(MODULE_NAME, tag + ", " + msg, t);
        }
    }

    /**
     * The method prints the log, level debug.
     * 
     * @param tag
     *            the tag of the class.
     * @param msg
     *            the message to print.
     */
    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(MODULE_NAME, tag + ", " + msg);
        }
    }

    /**
     * The method prints the log, level debug.
     * 
     * @param tag
     *            the tag of the class.
     * @param msg
     *            the message to print.
     * @param t
     *            An exception to log.
     */
    public static void v(String tag, String msg, Throwable t) {
        if (DEBUG) {
            Log.v(MODULE_NAME, tag + ", " + msg, t);
        }
    }

    public static void printTrace(String tag) {
        if (DEBUG) {
            Log.v(MODULE_NAME, tag + ", Trace start");
            Thread.dumpStack();
            Log.v(MODULE_NAME, tag + ", Trace end");
        }
    }

    public static void saveBitmap(Bitmap bitmap, String path) {
        if (DEBUG) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(path);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            } catch (Exception e) {
                e.printStackTrace();
                d("saveBitmap", "exception: " + e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
