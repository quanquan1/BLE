package com.controller.android.event;

import com.squareup.otto.Bus;

public class BusProvider {

    private static final AndroidBus sAndroidBus = new AndroidBus();

    public static Bus getInstance() {
        return sAndroidBus;
    }

    private BusProvider() {
        // No instances.
    }
}
