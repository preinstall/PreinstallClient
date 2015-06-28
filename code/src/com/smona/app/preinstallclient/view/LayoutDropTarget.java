package com.smona.app.preinstallclient.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smona.app.preinstallclient.MainActivity;
import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.control.DragSource;
import com.smona.app.preinstallclient.control.DropTarget;
import com.smona.app.preinstallclient.control.DragListener;

/**
 * Implements a DropTarget.
 */
public class LayoutDropTarget extends RelativeLayout implements DropTarget,
        DragListener {

    protected int mTransitionDuration;

    protected MainActivity mMain;
    private int mBottomDragPadding;
    protected TextView mText;
    protected DropTargetBar mDropTargetBar;

    /** Whether this drop target is active for the current drag */
    protected boolean mActive;

    /** The paint applied to the drag view on hover */
    protected int mHoverColor = 0;
    protected ColorStateList mOriginalTextColor;

    public LayoutDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LayoutDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    void setMain(MainActivity main) {
        mMain = main;
    }

    public boolean acceptDrop(DragObject d) {
        return true;
    }

    public void setDropTargetBar(DropTargetBar dropTargetBar) {
        mDropTargetBar = dropTargetBar;
    }

    protected Drawable getCurrentDrawable() {
        Drawable[] drawables = mText.getCompoundDrawables();
        for (int i = 0; i < drawables.length; ++i) {
            if (drawables[i] != null) {
                return drawables[i];
            }
        }
        return null;
    }

    public void onDrop(DragObject d) {
    }

    public void onFlingToTarget(DragObject d, int x, int y, PointF vec) {
        // Do nothing
    }

    public void onDragEnter(DragObject d) {
        d.dragView.setColor(mHoverColor);
    }

    public void onDragOver(DragObject d) {
        // Do nothing
    }

    public void onDragExit(DragObject d) {
        d.dragView.setColor(0);
    }

    public boolean isDropEnabled() {
        return true;
    }

    public void setDropEnabled(boolean enable) {
        mActive = enable;
    }

    public void onDragEnd() {
        // Do nothing
    }

    @Override
    public void getHitRect(android.graphics.Rect outRect) {
        super.getHitRect(outRect);
        outRect.bottom += mBottomDragPadding;
    }

    Rect getIconRect(int itemWidth, int itemHeight, int drawableWidth,
            int drawableHeight) {
        DragLayer dragLayer = mMain.getDragLayer();
        // Find the rect to animate to (the view is center aligned)
        Rect to = new Rect();
        dragLayer.getViewRectRelativeToSelf(this, to);
        int width = drawableWidth;
        int height = drawableHeight;
        int left = to.left + getPaddingLeft();
        int top = to.top + (getMeasuredHeight() - height) / 2;
        to.set(left, top, left + width, top + height);

        // Center the destination rect about the trash icon
        int xOffset = (int) -(itemWidth - width) / 2;
        int yOffset = (int) -(itemHeight - height) / 2;
        to.offset(xOffset, yOffset);

        return to;
    }

    @Override
    public DropTarget getDropTargetDelegate(DragObject d) {
        return null;
    }

    public void getLocationInDragLayer(int[] loc) {
        mMain.getDragLayer().getLocationInDragLayer(this, loc);
    }

    protected ColorStateList getTextColors() {
        return mText.getTextColors();
    }

    protected void setText(String text) {
        mText.setText(text);
    }

    protected void setText(int resid) {
        mText.setText(resid);
    }

    protected void setTextColor(ColorStateList color) {
        mText.setTextColor(getResources()
                .getColor(R.color.title_bar_text_color));
    }

    protected CharSequence getText() {
        return mText.getText();
    }

    protected void setTextDrawableLeft(int resid) {
        mText.setCompoundDrawablePadding(this.getResources()
                .getDimensionPixelSize(R.dimen.drop_target_left_padding));
        mText.setCompoundDrawablesWithIntrinsicBounds(this.getResources()
                .getDrawable(resid), null, null, null);
    }

    protected void setParentBackgroundColor(int color) {
        ViewParent parent = getParent();
        if (parent instanceof FrameLayout) {
            ((FrameLayout) parent).setBackgroundColor(color);
        }
    }

    protected void setParentBackgroundResource(int resid) {
        ViewParent parent = getParent();
        if (parent instanceof FrameLayout) {
            ((FrameLayout) parent).setBackgroundResource(resid);
        }
    }

    protected void initViews(Context context) {
        Resources r = getResources();
        mTransitionDuration = r
                .getInteger(R.integer.config_dropTargetBgTransitionDuration);
        mBottomDragPadding = r
                .getDimensionPixelSize(R.dimen.drop_target_drag_padding);
        mText = new TextView(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mText.setGravity(Gravity.CENTER);
        addView(mText, params);
        mOriginalTextColor = getTextColors();
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {

    }

    @Override
    public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {

    }
}
