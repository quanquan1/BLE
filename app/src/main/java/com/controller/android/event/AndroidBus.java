package com.controller.android.event;

import android.os.Handler;
import android.os.Looper;

import com.controller.android.Application;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class AndroidBus extends Bus {
    private final Handler mMainThread = Application.getInstance().getHandler();

    public AndroidBus() {
        super(ThreadEnforcer.ANY);
    }

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mMainThread.post(new Runnable() {
                @Override
                public void run() {
                    post(event);
                }
            });
        }
    }

    @Override
    public void register(Object object) {
        super.register(object);
    }

    @Override
    public void unregister(Object object) {
        super.unregister(object);
    }
}
