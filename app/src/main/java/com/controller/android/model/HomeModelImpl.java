package com.controller.android.model;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

public class HomeModelImpl implements HomeModel {

    public static final int REQUEST_ENABLE_BT = 1000;

    @Override
    public boolean isBluetoothEnable(BluetoothAdapter bluetoothAdapter, Activity activity) {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        boolean enable = bluetoothAdapter.isEnabled();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }

        return enable;
    }
}
