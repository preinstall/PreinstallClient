package com.smona.app.preinstallclient.view;

import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.control.DragSource;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class DeleteDropTarget extends ActionDropTarget {

    public DeleteDropTarget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteDropTarget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    protected void initViews(Context context) {
        super.initViews(context);
        super.initViews(R.color.delete_target_hover_tint,
                R.string.delete_target_label, R.drawable.remove_target_selector);
    }

    protected void onDragStartForChild(DragSource source, Object info,
            int dragAction) {

    }

    protected void onDropEndForChild(DragObject d) {
    }

    public void setVisibility(DragObject d) {
        boolean isVisible = false;
        mActive = isVisible;
        resetHoverColor();
        ((ViewGroup) getParent()).setVisibility(isVisible ? View.VISIBLE
                : View.GONE);
    }
}
