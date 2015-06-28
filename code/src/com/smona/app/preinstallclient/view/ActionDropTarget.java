package com.smona.app.preinstallclient.view;

import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.control.DragSource;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

@SuppressLint("NewApi")
public abstract class ActionDropTarget extends LayoutDropTarget {

    private static final int DELETE_ANIMATION_DURATION = 285;

    public ActionDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    protected void initViews(int color, int text, int drawable) {
        Resources r = getResources();
        mHoverColor = r.getColor(color);
        setText(text);
    }

    private void setHoverColor() {
        setParentBackgroundResource(R.drawable.del_uninst_bg_select);
    }

    protected void resetHoverColor() {
        setTextColor(mOriginalTextColor);
        setParentBackgroundResource(R.drawable.del_uninst_bg);
    }

    @Override
    public boolean acceptDrop(DragObject d) {
        return true;
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        onDragStartForChild(source, info, dragAction);
    }

    protected abstract void onDragStartForChild(DragSource source, Object info,
            int dragAction);

    @Override
    public void onDragEnd() {
        super.onDragEnd();
        mActive = false;
    }

    public void onDragEnter(DragObject d) {
        super.onDragEnter(d);

        setHoverColor();
    }

    public void onDragExit(DragObject d) {
        super.onDragExit(d);

        if (!d.dragComplete) {
            resetHoverColor();
        } else {
            d.dragView.setBackgroundResource(R.drawable.del_uninst_bg_select);
        }
    }

    private void animateToTrashAndCompleteDrop(final DragObject d) {
        DragLayer dragLayer = mMain.getDragLayer();
        Rect from = new Rect();
        dragLayer.getViewRectRelativeToSelf(d.dragView, from);
        Rect to = getIconRect(d.dragView.getMeasuredWidth(),
                d.dragView.getMeasuredHeight(), this.getWidth(),
                this.getHeight());
        float scale = (float) to.width() / from.width();

        Runnable onAnimationEndRunnable = new Runnable() {
            @Override
            public void run() {
                mDropTargetBar.onDragEnd();
                completeDrop(d);
            }
        };
        dragLayer.animateView(d.dragView, from, to, scale, 1f, 1f, 0.1f, 0.1f,
                DELETE_ANIMATION_DURATION, new DecelerateInterpolator(2),
                new LinearInterpolator(), onAnimationEndRunnable,
                DragLayer.ANIMATION_END_DISAPPEAR, null);
    }

    private void completeDrop(DragObject d) {
        onDropEndForChild(d);
    }

    protected abstract void onDropEndForChild(DragObject d);

    public void onDrop(DragObject d) {
        animateToTrashAndCompleteDrop(d);
    }
}