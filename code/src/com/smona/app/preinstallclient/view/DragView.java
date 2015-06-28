package com.smona.app.preinstallclient.view;

import com.smona.app.preinstallclient.MainActivity;
import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.util.ClientAnimUtils;
import com.smona.app.preinstallclient.util.LogUtil;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

@SuppressLint("NewApi")
public class DragView extends View {
    private static final String TAG = "DragView";
    private static float sDragAlpha = 0.9f;

    private Bitmap mBitmap;
    private Bitmap mCrossFadeBitmap;
    private Paint mPaint;
    private int mRegistrationX;
    private int mRegistrationY;

    private Point mDragVisualizeOffset = null;
    private Rect mDragRegion = null;
    private DragLayer mDragLayer = null;
    private boolean mHasDrawn = false;
    private float mCrossFadeProgress = 0f;

    private ValueAnimator mAnim;
    private float mOffsetX = 0.0f;
    private float mOffsetY = 0.0f;
    private float mInitialScale = 1f;

    public enum ZoomStatus {
        ZOOMOUT, NORMAL, ZOOMIN
    }

    private ZoomStatus mZoom = ZoomStatus.NORMAL;
    private int mZoomTargetPosition;
    private int[] mDownLoc = new int[2];

    /**
     * Construct the drag view.
     * <p>
     * The registration point is the point inside our view that the touch events
     * should be centered upon.
     * 
     * @param launcher
     *            The Launcher instance
     * @param bitmap
     *            The view that we're dragging around. We scale it up when we
     *            draw it.
     * @param registrationX
     *            The x coordinate of the registration point.
     * @param registrationY
     *            The y coordinate of the registration point.
     */
    public DragView(MainActivity main, Bitmap bitmap, int registrationX,
            int registrationY, int left, int top, int width, int height,
            final float initialScale) {
        super(main);
        mDragLayer = main.getDragLayer();
        mInitialScale = initialScale;

        final Resources res = getResources();
        final float offsetX = res
                .getDimensionPixelSize(R.dimen.dragViewOffsetX);
        final float offsetY = res
                .getDimensionPixelSize(R.dimen.dragViewOffsetY);
        final float scaleDps = res.getDimensionPixelSize(R.dimen.dragViewScale);
        final float scale = (width + scaleDps) / width;

        // Set the initial scale to avoid any jumps
        setScaleX(initialScale);
        setScaleY(initialScale);

        // Animate the view into the correct position
        mAnim = ClientAnimUtils.ofFloat(0.0f, 1.0f);
        mAnim.setDuration(10);
        mAnim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float value = (Float) animation.getAnimatedValue();

                final int deltaX = (int) ((value * offsetX) - mOffsetX);
                final int deltaY = (int) ((value * offsetY) - mOffsetY);

                mOffsetX += deltaX;
                mOffsetY += deltaY;
                setScaleX(initialScale + (value * (scale - initialScale)));
                setScaleY(initialScale + (value * (scale - initialScale)));
                if (sDragAlpha != 1f) {
                    setAlpha(sDragAlpha * value + (1f - value));
                }

                if (getParent() == null) {
                    animation.cancel();
                } else {
                    setTranslationX(getTranslationX() + deltaX);
                    setTranslationY(getTranslationY() + deltaY);
                }
            }
        });

        mBitmap = Bitmap.createBitmap(bitmap, left, top, width, height);
        setDragRegion(new Rect(0, 0, width, height));

        // The point in our scaled bitmap that the touch events are located
        mRegistrationX = registrationX;
        mRegistrationY = registrationY;

        // Force a measure, because Workspace uses getMeasuredHeight() before
        // the layout pass
        int ms = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        measure(ms, ms);
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    }

    public void setZoomParams(ZoomStatus zoom, int position) {
        this.mZoom = zoom;
        this.mZoomTargetPosition = position;
    }

    public float getOffsetY() {
        return mOffsetY;
    }

    public int getDragRegionLeft() {
        return mDragRegion.left;
    }

    public int getDragRegionTop() {
        return mDragRegion.top;
    }

    public int getDragRegionWidth() {
        return mDragRegion.width();
    }

    public int getDragRegionHeight() {
        return mDragRegion.height();
    }

    public void setDragVisualizeOffset(Point p) {
        mDragVisualizeOffset = p;
    }

    public Point getDragVisualizeOffset() {
        return mDragVisualizeOffset;
    }

    public void setDragRegion(Rect r) {
        mDragRegion = r;
    }

    public Rect getDragRegion() {
        return mDragRegion;
    }

    public float getInitialScale() {
        return mInitialScale;
    }

    public void updateInitialScaleToCurrentScale() {
        mInitialScale = getScaleX();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mHasDrawn = true;
        boolean crossFade = mCrossFadeProgress > 0 && mCrossFadeBitmap != null;
        if (crossFade) {
            int alpha = crossFade ? (int) (255 * (1 - mCrossFadeProgress))
                    : 255;
            mPaint.setAlpha(alpha);
        }
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
        if (crossFade) {
            mPaint.setAlpha((int) (255 * mCrossFadeProgress));
            canvas.save();
            float sX = (mBitmap.getWidth() * 1.0f)
                    / mCrossFadeBitmap.getWidth();
            float sY = (mBitmap.getHeight() * 1.0f)
                    / mCrossFadeBitmap.getHeight();
            canvas.scale(sX, sY);
            canvas.drawBitmap(mCrossFadeBitmap, 0.0f, 0.0f, mPaint);
            canvas.restore();
        }
    }

    public void setCrossFadeBitmap(Bitmap crossFadeBitmap) {
        mCrossFadeBitmap = crossFadeBitmap;
    }

    public void crossFade(int duration) {
        ValueAnimator va = ClientAnimUtils.ofFloat(0f, 1f);
        va.setDuration(duration);
        va.setInterpolator(new DecelerateInterpolator(1.5f));
        va.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCrossFadeProgress = animation.getAnimatedFraction();
            }
        });
        va.start();
    }

    public void setColor(int color) {
        if (mPaint == null) {
            mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        }
        if (color != 0) {
            mPaint.setColorFilter(new PorterDuffColorFilter(color,
                    PorterDuff.Mode.SRC_ATOP));
        } else {
            mPaint.setColorFilter(null);
        }
        invalidate();
    }

    public boolean hasDrawn() {
        return mHasDrawn;
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        mPaint.setAlpha((int) (255 * alpha));
        invalidate();
    }

    /**
     * Create a window containing this view and show it.
     * 
     * @param windowToken
     *            obtained from v.getWindowToken() from one of your views
     * @param touchX
     *            the x coordinate the user touched in DragLayer coordinates
     * @param touchY
     *            the y coordinate the user touched in DragLayer coordinates
     */
    public void show(int touchX, int touchY) {
        mDragLayer.addView(this);
        mDownLoc[0] = touchX;
        mDownLoc[1] = touchY;
        // Start the pick-up animation
        DragLayer.LayoutParams lp = new DragLayer.LayoutParams(0, 0);
        lp.width = mBitmap.getWidth();
        lp.height = mBitmap.getHeight();
        // lp.customPosition = true;
        setLayoutParams(lp);
        setTranslationX(touchX - mRegistrationX);
        setTranslationY(touchY - mRegistrationY);
        // Post the animation to skip other expensive work happening on the
        // first frame
        post(new Runnable() {
            public void run() {
                mAnim.start();
            }
        });
    }

    public void cancelAnimation() {
        if (mAnim != null && mAnim.isRunning()) {
            mAnim.cancel();
        }
    }

    public void resetLayoutParams() {
        mOffsetX = mOffsetY = 0;
        requestLayout();
    }

    /**
     * Move the window containing this view.
     * 
     * @param touchX
     *            the x coordinate the user touched in DragLayer coordinates
     * @param touchY
     *            the y coordinate the user touched in DragLayer coordinates
     */
    public void move(int touchX, int touchY) {
        setTranslationX(touchX - mRegistrationX + (int) mOffsetX);
        setTranslationY(touchY - mRegistrationY + (int) mOffsetY);
        LogUtil.d(TAG, "move TouchX: " + touchX + ", TouchY: " + touchY
                + ", mRegistrationX: " + mRegistrationX + ", mRegistrationY: "
                + mRegistrationY + ", mOffsetX: " + mOffsetX);
        if (mZoom != ZoomStatus.NORMAL) {
            double distance = mDownLoc[1] - mZoomTargetPosition;
            int delta = (touchY - mZoomTargetPosition);
            double scale = delta / distance;
            if (scale < 0.6) {
                scale = 0.6;
            }
            if (scale > 1.0) {
                scale = 1.0;
            }
            LogUtil.d(TAG, "move TouchX: " + touchX + ", TouchY: " + touchY
                    + ", scale: " + scale);
            setScaleX((float) scale);
            setScaleY((float) scale);
        }
    }

    public void remove() {
        LogUtil.d(TAG, "DragViewDebug : remove  getparent = " + getParent());
        if (getParent() != null) {
            mDragLayer.removeView(DragView.this);
        }
    }

}
