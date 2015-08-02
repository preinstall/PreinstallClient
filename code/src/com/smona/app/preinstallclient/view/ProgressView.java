package com.smona.app.preinstallclient.view;

import com.smona.app.preinstallclient.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ProgressView extends View {
    private Drawable mDownloadedDrawable;
    private float mProgressCompleted = 0;
    private long mProgressTotal = 0;
    private int mProgressWidth = 0;

    public ProgressView(Context context, AttributeSet attr) {
        super(context, attr);
        mDownloadedDrawable = context.getResources().getDrawable(
                R.drawable.download_progress_completed);
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
        if (mProgressWidth == 0) {
            mProgressWidth = r - l;
        }
    }

    public void setProgressTotal(int total) {
        mProgressTotal = total;
    }

    public long getProgressTotal() {
        return mProgressTotal;
    }

    public void updateProgress(long radio) {
        mProgressCompleted = radio;
        invalidate();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mProgressWidth != 0) {
            if (mProgressCompleted == mProgressTotal && mProgressTotal > 0) {
                setBackgroundColor(Color.rgb(0xff, 0x67, 0x02));
            } else if (mProgressCompleted != 0 && mProgressTotal != 0) {
                int width = (int) (mProgressCompleted * mProgressWidth / mProgressTotal);
                mDownloadedDrawable.setBounds(0, 0, width,
                        mDownloadedDrawable.getIntrinsicHeight());
                mDownloadedDrawable.setFilterBitmap(true);
                mDownloadedDrawable.draw(canvas);
                mDownloadedDrawable.clearColorFilter();
                mDownloadedDrawable.setFilterBitmap(false);
            }
        }

    }

}
