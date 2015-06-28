package com.smona.app.preinstallclient;

import com.smona.app.preinstallclient.control.StaticticsControl;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatisticsDatas();
    }

    private void initStatisticsDatas() {
        StaticticsControl.getInstance().init(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StaticticsControl.getInstance().onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StaticticsControl.getInstance().onPause(this);
    }
}
