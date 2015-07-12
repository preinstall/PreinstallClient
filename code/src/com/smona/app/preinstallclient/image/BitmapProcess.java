package com.smona.app.preinstallclient.image;

import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.control.ImageCacheStrategy;
import com.smona.app.preinstallclient.util.LogUtil;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class BitmapProcess {
    private static final String TAG = "BitmapProcess";

    public static void processBitmap(Context context, int resid, String fileName) {

        int sourceSize = context.getResources().getDimensionPixelSize(
                R.dimen.source_bitmap_size);
        Bitmap source = ImageCacheStrategy.getInstance().getBitmap(fileName);
        if (source == null) {
            return;
        }
        Bitmap standerd = resizeBitmap(source, sourceSize, sourceSize);
        Drawable template = context.getResources().getDrawable(resid);
        Bitmap collet = ((BitmapDrawable) template).getBitmap();

        int targetSize = collet.getHeight();
        int top = (targetSize - sourceSize) / 2;
        int left = (targetSize - sourceSize) / 2;

        LogUtil.d(TAG, "bitmapPath: " + fileName + ", targetH: " + targetSize
                + ", sourceH: " + sourceSize);

        Bitmap result = Bitmap.createBitmap(targetSize, targetSize,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        canvas.save();
        canvas.drawBitmap(collet, 0, 0, paint);
        canvas.drawBitmap(standerd, left, top, paint);
        canvas.restore();

        ImageCacheStrategy.getInstance().saveBitmap(result, fileName);

        recycleBitmap(source);
        recycleBitmap(standerd);
        recycleBitmap(result);
    }

    private static Bitmap resizeBitmap(Bitmap srcBitmap, int newHeight,
            int newWidth) {
        if (srcBitmap == null) {
            return null;
        }
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        float scaleWidth = ((float) newWidth) / srcWidth;
        float scaleHeight = ((float) newHeight) / srcHeight;
        if (isNotNeedScale(scaleWidth, scaleHeight)) {
            return srcBitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap result = Bitmap.createBitmap(srcBitmap, 0, 0, srcWidth,
                srcHeight, matrix, true);
        if (result != null) {
            return result;
        } else {
            LogUtil.e(TAG, "theme scale bitmap fail.");
            return srcBitmap;
        }
    }

    private static boolean isNotNeedScale(float scaleWidth, float scaleHeight) {
        return scaleWidth == 1.0f && scaleHeight == 1.0f;
    }

    private static void recycleBitmap(Bitmap recycle) {
        recycleBitmap(recycle, null);
    }

    @SuppressLint("NewApi")
    private static void recycleBitmap(Bitmap recycle, Bitmap source) {
        if (recycle != null) {
            if (recycle.sameAs(source) || recycle.isRecycled()) {
                return;
            }
            recycle.recycle();
        }
    }
}
