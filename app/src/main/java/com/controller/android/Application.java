package com.controller.android;

import android.os.Handler;
import android.os.Looper;

public class Application extends android.app.Application {

    private static final String TAG = "Application";

    private static Application sInstance = null;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * Get Application singleton.
     *
     * @return Application singleton
     */
    public static Application getInstance() {
        return sInstance;
    }

    /**
     * Get delay handler for post and postDelayed
     *
     * @return the handler
     */
    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}

