package com.controller.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.controller.android.R;
import com.controller.android.event.BusProvider;
import com.controller.android.event.FinishActivityEvent;
import com.squareup.otto.Subscribe;

public class BaseActivity extends AppCompatActivity {

    private Object mLogoutEventListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogoutEventListener = new Object() {
            @Subscribe
            public void onReceiveLogoutEvent(@NonNull FinishActivityEvent event) {
                finish();
            }
        };

        BusProvider.getInstance().register(mLogoutEventListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(mLogoutEventListener);

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivityAnimation(this);
    }

    public static void startActivityAnimation(Activity activity) {
        activity.overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public static void finishActivityAnimation(Activity activity) {
        activity.overridePendingTransition(R.anim.enter_back, R.anim.exit_back);
    }
}
