package com.controller.android.ui.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.widget.TextView;

import com.controller.android.BuildConfig;
import com.controller.android.R;
import com.controller.android.ui.BaseActivity;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.about);
        }

        TextView versionText = (TextView) findViewById(R.id.version);
        versionText.setText(BuildConfig.VERSION_NAME);
    }
}
