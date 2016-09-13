package com.controller.android.model;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

public interface HomeModel {

    boolean isBluetoothEnable(BluetoothAdapter bluetoothAdapter, Activity activity);
}
