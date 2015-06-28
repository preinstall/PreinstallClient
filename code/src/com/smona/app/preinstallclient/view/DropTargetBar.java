package com.smona.app.preinstallclient.view;

import com.smona.app.preinstallclient.MainActivity;
import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.control.DragController;
import com.smona.app.preinstallclient.control.DragSource;
import com.smona.app.preinstallclient.control.DragListener;
import com.smona.app.preinstallclient.control.DropTarget.DragObject;
import com.smona.app.preinstallclient.util.ClientAnimUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

@SuppressLint("NewApi")
public class DropTargetBar extends FrameLayout implements DragListener {

    private LayoutDropTarget mDropDelete;

    private static final int TRANSITION_IN_DURATION = 200;

    private ObjectAnimator mShowAnimtor;
    private ObjectAnimator mHideAnimtor;
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    private boolean mEnableDropDownDropTargets;

    private int mBarHeight;
    private boolean mDeferOnDragEnd = false;

    private float mCurrentY;
    private long mDration;
    private boolean misAnimationCancle;

    public DropTargetBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setup(MainActivity main, DragController dragController) {
        dragController.addDragListener(this);
        dragController.addDragListener(mDropDelete);
        dragController.addDropTarget(mDropDelete);
        mDropDelete.setMain(main);
        mDropDelete.setDropTargetBar(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDropDelete = (DeleteDropTarget) findViewById(R.id.droptarget_delete);
        mBarHeight = getResources().getDimensionPixelSize(
                R.dimen.qsb_bar_height);
        mEnableDropDownDropTargets = getResources().getBoolean(
                R.bool.config_useDropTargetDownTransition);

        // Create the various fade animations
        if (mEnableDropDownDropTargets) {
            mDropDelete.setTranslationY(-mBarHeight);
            PropertyValuesHolder showTranslateY = PropertyValuesHolder.ofFloat(
                    "translationY", mCurrentY, 0f);
            PropertyValuesHolder hideTranslateY = PropertyValuesHolder.ofFloat(
                    "translationY", 0f, mCurrentY);
            mShowAnimtor = ClientAnimUtils.ofPropertyValuesHolder(mDropDelete,
                    showTranslateY);
            mHideAnimtor = ClientAnimUtils.ofPropertyValuesHolder(mDropDelete,
                    hideTranslateY);
            mDration = TRANSITION_IN_DURATION;
            mCurrentY = (-mBarHeight);
        } else {
            mDropDelete.setAlpha(0f);
            mShowAnimtor = ClientAnimUtils
                    .ofFloat(mDropDelete, "alpha", 0f, 1f);
        }
        setupShowAnimation(mShowAnimtor, mDropDelete);
        setupHideAnimation(mHideAnimtor, mDropDelete);
    }

    private void prepareStartAnimation(View v) {
        v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        v.buildLayer();
    }

    private void setupShowAnimation(final ObjectAnimator anim, final View v) {
        anim.setInterpolator(ACCELERATE_INTERPOLATOR);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setLayerType(View.LAYER_TYPE_NONE, null);
                if (misAnimationCancle) {
                    misAnimationCancle = false;
                    return;
                }
                mDration = TRANSITION_IN_DURATION;
                mCurrentY = 0f;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                misAnimationCancle = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                misAnimationCancle = true;
                mCurrentY = mDropDelete.getTranslationY();
                mDration = (long) (TRANSITION_IN_DURATION * (1 - Math
                        .abs(mCurrentY) / mBarHeight));
            }
        });
    }

    private void showAnimation() {
        if (mHideAnimtor.isRunning()) {
            mHideAnimtor.cancel();
        }
        PropertyValuesHolder translateY = PropertyValuesHolder.ofFloat(
                "translationY", mCurrentY, 0f);
        mDropDelete.setTranslationY(mCurrentY);
        prepareStartAnimation(mDropDelete);
        mShowAnimtor.setValues(translateY);
        mShowAnimtor.setDuration(mDration);
        mShowAnimtor.start();
    }

    private void setupHideAnimation(final ObjectAnimator anim, final View v) {
        anim.setInterpolator(ACCELERATE_INTERPOLATOR);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setLayerType(View.LAYER_TYPE_NONE, null);
                if (misAnimationCancle) {
                    misAnimationCancle = false;
                    return;
                }
                mDration = TRANSITION_IN_DURATION;
                mCurrentY = (-mBarHeight);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                misAnimationCancle = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                misAnimationCancle = true;
                mCurrentY = mDropDelete.getTranslationY();
                mDration = (long) (TRANSITION_IN_DURATION * (Math
                        .abs(mCurrentY) / mBarHeight));
            }
        });
    }

    private void hideAnimation() {
        if (mShowAnimtor.isRunning()) {
            mShowAnimtor.cancel();
        }
        PropertyValuesHolder translateY = PropertyValuesHolder.ofFloat(
                "translationY", mCurrentY, -mBarHeight);
        mDropDelete.setTranslationY(mCurrentY);
        prepareStartAnimation(mDropDelete);
        mHideAnimtor.setValues(translateY);
        mHideAnimtor.setDuration(mDration);
        mHideAnimtor.start();
    }

    public void finishAnimations() {
        prepareStartAnimation(mDropDelete);
        mShowAnimtor.reverse();
    }

    /*
     * DragListener implementation
     */
    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        prepareStartAnimation(mDropDelete);
        showAnimation();
    }

    @Override
    public void onDragEnd() {
        hideDropTargetBar();
    }

    public void showDropTargetBar(DragObject d) {
        showAnimation();
    }

    public void hideDropTargetBar() {
        if (!mDeferOnDragEnd) {
            hideAnimation();
            mDropDelete.setDropEnabled(false);
        } else {
            mDeferOnDragEnd = false;
        }
    }
}
