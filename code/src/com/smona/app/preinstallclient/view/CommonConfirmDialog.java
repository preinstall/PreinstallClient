package com.smona.app.preinstallclient.view;

import com.smona.app.preinstallclient.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommonConfirmDialog extends Dialog {
    private Handler handler;
    private String buttonOkText;
    private String buttonReturnText;
    private String line1_text;
    private TextView common_dialog_text_line1;
    private String title_text;
    private TextView common_dialog_title;

    private Button buttonOk;
    private Button buttonReturn;
    private CheckBox checkBoxRead;
    private int resId = 0;
    private int width;
    private RelativeLayout absoluteLayoutRoot;

    public void setWidth(int width) {
        this.width = width;
    }

    public CommonConfirmDialog(Context context, Handler handler) {
        super(context, R.style.commonDialog);
        this.handler = handler;
    }

    public CommonConfirmDialog(Context context, int resId, Handler handler) {
        super(context, R.style.commonDialog);
        this.handler = handler;
        this.resId = resId;
    }

    public CommonConfirmDialog(Context context, int resId, Handler handler,
            String title_text, String line1_text, String buttonOkText,
            String buttonReturnText) {
        super(context, R.style.commonDialog);
        this.handler = handler;
        this.resId = resId;
        this.line1_text = line1_text;
        this.title_text = title_text;
        this.buttonOkText = buttonOkText;
        this.buttonReturnText = buttonReturnText;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(resId);
        absoluteLayoutRoot = (RelativeLayout) findViewById(R.id.absoluteLayoutRoot);
        if (width != 0) {
            setLayoutWidth(width);
        }
        buttonOk = (Button) findViewById(R.id.buttonOk);
        this.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode,
                    KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (handler != null && checkBoxRead != null
                            && checkBoxRead.isChecked()) {
                        Message message = new Message();
                        message.what = 4;
                        handler.sendMessage(message);
                    } else {
                        Message message = new Message();
                        message.what = 5;
                        handler.sendMessage(message);
                    }
                }
                return false;
            }
        });

        if (buttonOk != null) {
            if (buttonOkText != null && !buttonOkText.equals("")) {
                buttonOk.setText(buttonOkText);
            }
            buttonOk.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // dismiss();
                    if (handler != null && checkBoxRead != null
                            && checkBoxRead.isChecked()) {
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    } else {
                        Message message = new Message();
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                }
            });
        }

        buttonReturn = (Button) findViewById(R.id.buttonReturn);

        if (buttonReturn != null) {
            if (buttonReturnText != null && !buttonReturnText.equals("")) {
                buttonReturn.setText(buttonReturnText);
            }
            buttonReturn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // dismiss();
                    if (handler != null && checkBoxRead != null
                            && checkBoxRead.isChecked()) {
                        Message message = new Message();
                        message.what = 0;
                        handler.sendMessage(message);
                    } else {
                        Message message = new Message();
                        message.what = 2;
                        handler.sendMessage(message);
                    }

                }
            });
        }

        checkBoxRead = (CheckBox) findViewById(R.id.checkBoxRead);

        common_dialog_text_line1 = (TextView) findViewById(R.id.common_dialog_text_line1);
        if (common_dialog_text_line1 != null && line1_text != null) {
            common_dialog_text_line1.setText(Html.fromHtml(line1_text));
        }

        common_dialog_title = (TextView) findViewById(R.id.common_dialog_title);
        if (common_dialog_title != null && line1_text != null) {
            common_dialog_title.setText(Html.fromHtml(title_text));
        }

    }

    public void setLayoutWidth(int outWidth) {
        if (absoluteLayoutRoot != null) {
            LayoutParams lp = absoluteLayoutRoot.getLayoutParams();
            lp.width = outWidth;
            absoluteLayoutRoot.setLayoutParams(lp);
        }
    }

    // called when this dialog is dismissed
    protected void onStop() {
    }

    @Override
    public void show() {
        try {
            super.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}