package com.smona.app.preinstallclient.view;

import com.smona.app.preinstallclient.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class HeaderView extends LinearLayout {

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        HeaderAction action = new HeaderAction();
        findViewById(R.id.download).setOnClickListener(action);
    }

    class HeaderAction implements OnClickListener {
        @Override
        public void onClick(View v) {
            startDownloadMgr();
        }

    }

    private void startDownloadMgr() {
    }

}
