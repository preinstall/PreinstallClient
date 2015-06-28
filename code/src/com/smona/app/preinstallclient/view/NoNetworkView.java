package com.smona.app.preinstallclient.view;

import com.smona.app.preinstallclient.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class NoNetworkView extends RelativeLayout {

    public NoNetworkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        NoWorkAction action = new NoWorkAction();
        findViewById(R.id.no_network_set).setOnClickListener(action);
        findViewById(R.id.no_network_try).setOnClickListener(action);
    }

    class NoWorkAction implements OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
            case R.id.no_network_set:
                setNetwork();
                break;
            case R.id.no_network_try:
                tryNewWork();
                break;
            }
        }
    }

    private void tryNewWork() {

    }

    private void setNetwork() {

    }
}
