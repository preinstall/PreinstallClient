package com.smona.app.preinstallclient.control;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.smona.app.preinstallclient.R;
import com.smona.app.preinstallclient.util.LogUtil;
import com.smona.app.preinstallclient.view.FluxHintDialog;

public class FluxHintControl {
    private static final String TAG = "FluxHintControl";
    private static FluxHintControl sInstance;
    private Dialog mNetworkFluxDialog = null;

    private FluxHintControl() {

    }

    public static FluxHintControl getInstance() {
        if (sInstance == null) {
            sInstance = new FluxHintControl();
        }
        return sInstance;
    }

    @SuppressLint({ "NewApi", "InflateParams" })
    public void showFluxHintDialog(Context context,
            final DialogActionCallback callback) {
        cancelFluxDialog();

        mNetworkFluxDialog = new FluxHintDialog(context,
                R.style.dialog_data_hint);
        mNetworkFluxDialog.setTitle(R.string.check_data_title);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View contentView = layoutInflater.inflate(R.layout.fluxhint_data, null);
        mNetworkFluxDialog.setContentView(contentView);

        // don't hint
        final CheckBox checkbox = (CheckBox) contentView
                .findViewById(R.id.checkBox);
        checkbox.setChecked(true);
        final LinearLayout rlCheckBox = (LinearLayout) contentView
                .findViewById(R.id.dont_hint);
        rlCheckBox.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LogUtil.d(TAG, "CheckBox checked: " + checkbox.isChecked());
                callback.onCheckedAction(!checkbox.isChecked());
                if (checkbox.isChecked()) {
                    checkbox.setChecked(false);
                } else {
                    checkbox.setChecked(true);
                }
            }
        });

        contentView.findViewById(R.id.dialog_yes).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        LogUtil.d(TAG, "dialog_yes");
                        callback.onYesAction();
                        cancelFluxDialog();
                    }

                });
        contentView.findViewById(R.id.dialog_no).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        LogUtil.d(TAG, "dialog_no");
                        callback.onNoAction();
                        cancelFluxDialog();
                    }
                });

        mNetworkFluxDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                LogUtil.d(TAG, "onDismiss");
                callback.onDismissAction();
            }

        });
        mNetworkFluxDialog.show();
    }

    private void cancelFluxDialog() {
        if (mNetworkFluxDialog != null) {
            mNetworkFluxDialog.dismiss();
            mNetworkFluxDialog = null;
        }
    }

    public Dialog createWifiHintDialog() {
        return null;
    }

    public interface DialogActionCallback {
        void onCheckedAction(boolean isChecked);

        void onDismissAction();

        void onYesAction();

        void onNoAction();
    }
}
