package com.smona.app.preinstallclient.control;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.smona.app.preinstallclient.util.LogUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageCacheStrategy {

    private static final String TAG = "ImageCacheStrategy";
    private static ImageCacheStrategy sInstance = null;

    private static final String CACHE_DIR = "/images/";
    private static final String IMAGE_SUBBFIX = ".png";
    private static final int TIME_OUT_MS = 5000;

    public enum ReturnImageType {
        DOWNLOAD, EXIST, FAILED
    }

    private Context mAppContext;
    private String mCacheRootPath;

    private ImageCacheStrategy() {

    }

    public static ImageCacheStrategy getInstance() {
        if (sInstance == null) {
            sInstance = new ImageCacheStrategy();
        }
        return sInstance;
    }

    public void initCacheStrategy(Context appContext) {
        mAppContext = appContext;
        mCacheRootPath = mAppContext.getCacheDir().getPath();
        File createImage = new File(mCacheRootPath + CACHE_DIR);
        if (!createImage.exists()) {
            boolean success = createImage.mkdir();
            LogUtil.d(TAG, "Create cache dir result: " + success);
        }
    }

    public Bitmap getBitmap(String fileName) {
        File file = new File(mCacheRootPath + CACHE_DIR + fileName
                + IMAGE_SUBBFIX);
        if (file.exists()) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), opts);
            return bitmap;
        }
        return null;
    }

    public void saveBitmap(Bitmap bitmap, String fileName) {
        String path = getCacheImagePath(fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "saveBitmap exception: " + e);
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

    private String getCacheImagePath(String fileName) {
        return mCacheRootPath + CACHE_DIR + fileName + IMAGE_SUBBFIX;
    }

    public ReturnImageType downloadImage(String fileName, String url) {
        File dir = new File(mCacheRootPath + CACHE_DIR);
        if (!dir.exists()) {
            LogUtil.d(TAG, "no exist! dir: " + dir);
            return ReturnImageType.FAILED;
        }

        String filePath = getCacheImagePath(fileName);
        File image = new File(filePath);
        if (image.exists()) {
            return ReturnImageType.EXIST;
        }

        HttpURLConnection con = null;
        BufferedInputStream bis = null;
        FileOutputStream ops = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(TIME_OUT_MS);
            bis = new BufferedInputStream(con.getInputStream());
            ops = new FileOutputStream(image);
            byte[] buffer = new byte[512];
            int size;
            while ((size = bis.read(buffer)) != -1) {
                ops.write(buffer, 0, size);
            }
            ops.flush();
            return ReturnImageType.DOWNLOAD;
        } catch (Exception e) {
            LogUtil.e(TAG, "download image error: url=" + url);
            return ReturnImageType.FAILED;
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (ops != null) {
                    ops.close();
                }
                if (con != null) {
                    con.disconnect();
                }
            } catch (IOException ioe) {
            }
        }
    }
}
