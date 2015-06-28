package com.smona.app.preinstallclient.view;

import java.util.HashMap;

import com.smona.app.preinstallclient.AbstractDataAdapter;
import com.smona.app.preinstallclient.MainDataAdatper;
import com.smona.app.preinstallclient.MainActivity;
import com.smona.app.preinstallclient.ProcessModel;
import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.control.DragController;
import com.smona.app.preinstallclient.control.DragListener;
import com.smona.app.preinstallclient.control.DragSource;
import com.smona.app.preinstallclient.control.DropTarget;
import com.smona.app.preinstallclient.data.DragInfo;
import com.smona.app.preinstallclient.data.IDataSource;
import com.smona.app.preinstallclient.download.DownloadInfo;
import com.smona.app.preinstallclient.util.ClientAnimUtils;
import com.smona.app.preinstallclient.util.ClientViewPropertyAnimator;
import com.smona.app.preinstallclient.util.LogUtil;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

@SuppressLint("NewApi")
public class ContainerSpace extends FrameLayout implements DragSource,
        DropTarget, DragListener {
    private static final String TAG = "ContainerSpace";

    private MainActivity mMain;
    private Button mDownloadCount = null;

    private ContentGrid mContent = null;
    private AbstractDataAdapter mDataAdapter = null;
    private IDataSource mDataSource = null;

    private DragController mDragController;
    private static final int ANIMATION_DELETE_DURATION = 300;
    private static final int ANIMATION_DELETE_TRANSLATION_Y = 50;

    private DragInfo mDragInfo;
    private View mDragView;

    private Bitmap mDragOutline = null;
    private final Rect mTempRect = new Rect();
    private final int[] mTempXY = new int[2];

    private static final float DRAG_END_ALFA = 1.0f;

    private AnimatorSet mAnimateSet;

    public ContainerSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        mContent = (ContentGrid) findViewById(R.id.content);
        mDownloadCount = (Button) findViewById(R.id.download_count);
    }

    public void setup(MainActivity main, DragController dragController) {
        initDrag(main, dragController);
        initData();
    }

    private void initDrag(MainActivity main, DragController dragController) {
        mMain = main;
        mDragController = dragController;
        dragController.addDropTarget(this);
        dragController.addDragListener(this);
    }

    private void initData() {
        mContent.setOnItemClickListener(mMain);
        mContent.setOnItemLongClickListener(mMain);
        mDataSource = ProcessModel.createDataSource(getContext()
                .getApplicationContext());
        mDataAdapter = new MainDataAdatper(mMain, mDataSource);
        mContent.setAdapter(mDataAdapter);
    }

    public void refreshUI(HashMap<String, DownloadInfo> values) {
        mDataAdapter.refreshUI(values);
    }

    public void setNetworkStatus(boolean hasNetwork) {
        int status = hasNetwork ? VISIBLE : INVISIBLE;
        mContent.setVisibility(status);
    }

    public void setDataSource(IDataSource dataSource) {
        mDataSource.copy(dataSource);
    }

    @Override
    public boolean supportsFlingToDelete() {
        LogUtil.d(TAG, "supportsFlingToDelete ");
        return false;
    }

    @Override
    public void onFlingToDeleteCompleted() {
        LogUtil.d(TAG, "onFlingToDeleteCompleted ");
    }

    @SuppressLint("NewApi")
    @Override
    public void onDropCompleted(View target, DragObject dragObject,
            boolean isFlingToDelete, boolean success) {
        LogUtil.d(TAG, "onDropCompleted dragObject=" + dragObject
                + ", isFlingToDelete: " + isFlingToDelete + ", success: "
                + success);
        if (success) {
            if (target != this) {
                dropCompletedSuccess(dragObject, true);
            }
        } else if (mDragInfo != null) {
            mDragInfo.cell.setVisibility(VISIBLE);
        }
        mDragOutline = null;
        mDragInfo = null;
    }

    private void dropCompletedSuccess(DragObject d, boolean animate) {
        if (animate) {
            animateRemove(d);
        } else {
            removeDragItem(d);
        }
    }

    private void removeDragItem(DragObject d) {
        DragInfo dragInfo = (DragInfo) d.dragInfo;
        mDataAdapter.remove(dragInfo.pos);
    }

    @Override
    public boolean isDropEnabled() {
        return true;
    }

    @Override
    public void onDrop(DragObject d) {
        LogUtil.d(TAG, "onDrop dragObject=" + d + ", d.dragView.hasDrawn(): "
                + d.dragView.hasDrawn());
        if (d.dragView.hasDrawn()) {
            mMain.getDragLayer().animateViewIntoPosition(d.dragView,
                    mDragInfo.cell);
        } else {
            d.deferDragViewCleanupPostAnimation = false;
            mDragInfo.cell.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        LogUtil.d(TAG, "onDragEnter dragObject=" + dragObject);
    }

    @Override
    public void onDragOver(DragObject dragObject) {
        LogUtil.d(TAG, "onDragOver dragObject = " + dragObject);
    }

    @Override
    public void onDragExit(DragObject dragObject) {
        LogUtil.d(TAG, "onDragExit dragObject = " + dragObject);
    }

    @Override
    public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {
        LogUtil.d(TAG, "onFlingToDelete dragObject = " + dragObject);
    }

    @Override
    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        LogUtil.d(TAG, "getDropTargetDelegate dragObject = " + dragObject);
        return null;
    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        LogUtil.d(TAG, "acceptDrop dragObject = " + dragObject);
        return true;
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {
        mMain.getDragLayer().getLocationInDragLayer(this, loc);
        LogUtil.d(TAG, "getLocationInDragLayer loc=" + loc);
    }

    public void startDrag(View view, int position) {
        mDragInfo = new DragInfo();
        mDragInfo.pos = position;
        mDragInfo.cell = view;

        beginDragShared(view, this);
    }

    private void beginDragShared(View view, DragSource source) {
        final Canvas canvas = new Canvas();
        mDragOutline = createDragOutline(view, canvas, source);

        final int bmpWidth = mDragOutline.getWidth();
        final int bmpHeight = mDragOutline.getHeight();

        float scale = mMain.getDragLayer()
                .getLocationInDragLayer(view, mTempXY);
        int dragLayerX = Math.round(mTempXY[0]
                - (bmpWidth - scale * view.getWidth()) / 2);
        int dragLayerY = Math.round(mTempXY[1]
                - (bmpHeight - scale * bmpHeight) / 2);
        LogUtil.d(TAG, "view.getTag(): " + view.getTag());
        mDragController.startDrag(mDragOutline, dragLayerX, dragLayerY, source,
                mDragInfo, DragController.DRAG_ACTION_MOVE, null, null, scale);
    }

    @SuppressLint("NewApi")
    private Bitmap createDragOutline(View v, Canvas canvas, DragSource source) {
        int imgW = v.getWidth();
        int imgH = v.getHeight();
        final Bitmap b = Bitmap.createBitmap(imgW, imgH,
                Bitmap.Config.ARGB_8888);

        canvas.setBitmap(b);
        mDragView = v;
        // v.setAlpha(DRAG_BEGIN_ALFA);
        drawDragView(mMain, v, imgW, imgH, canvas, mTempRect);
        canvas.setBitmap(null);
        return b;
    }

    static void drawDragView(Context context, View v, int imgW, int imgH,
            Canvas destCanvas, Rect rect) {
        final Rect clipRect = rect;
        destCanvas.save();
        v.getDrawingRect(clipRect);
        destCanvas.translate(-v.getScrollX(), -v.getScrollY());
        destCanvas.clipRect(clipRect, Op.REPLACE);
        v.draw(destCanvas);
        destCanvas.restore();
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {

    }

    @Override
    public void onDragEnd() {
        mDragView.setAlpha(DRAG_END_ALFA);
    }

    private void cancelAnimatorSet() {
        if (mAnimateSet != null) {
            mAnimateSet.cancel();
            mAnimateSet = null;
        }
    }

    private void animateRemove(final DragObject d) {
        cancelAnimatorSet();
        // init
        mAnimateSet = ClientAnimUtils.createAnimatorSet();
        Element element = (Element) mDragInfo.cell;
        // add image animator
        View image = element.findViewById(R.id.image);
        Animator imageAnim = createImageAnimator(image);
        mAnimateSet.play(imageAnim);
        // add title animator
        View title = element.findViewById(R.id.title);
        Animator titleAnim = createTitleAnimator(title, new RemoveCallback() {
            public void remove() {
                removeDragItem(d);
            }
        });
        mAnimateSet.play(titleAnim);
        // start animator
        mAnimateSet.start();
    }

    private Animator createImageAnimator(final View view) {
        ClientViewPropertyAnimator imageAnim = new ClientViewPropertyAnimator(
                view);
        imageAnim.alpha(0f).translationX(0)
                .translationY(-ANIMATION_DELETE_TRANSLATION_Y)
                .setDuration(ANIMATION_DELETE_DURATION);
        imageAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {

            }
        });
        return imageAnim;
    }

    private Animator createTitleAnimator(View view,
            final RemoveCallback callback) {
        ClientViewPropertyAnimator imageAnim = new ClientViewPropertyAnimator(
                view);
        imageAnim.alpha(0f).translationX(0)
                .translationY(ANIMATION_DELETE_TRANSLATION_Y)
                .setDuration(ANIMATION_DELETE_DURATION);
        imageAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                callback.remove();
            }
        });
        return imageAnim;
    }

    interface RemoveCallback {
        void remove();
    }
}
