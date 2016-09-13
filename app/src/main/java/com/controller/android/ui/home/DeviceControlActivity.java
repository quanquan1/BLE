/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.controller.android.ui.home;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.controller.android.R;
import com.controller.android.bluetoothlelib.device.BluetoothLeDevice;
import com.controller.android.object.Message;
import com.controller.android.service.BluetoothLeService;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends AppCompatActivity implements DeviceControlFragment.ControlCallBack {
    public static final String EXTRA_DEVICE = "extra_device";
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    @Bind(R.id.connection_state)
    protected TextView mConnectionState;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;
    private String mDeviceName;

    public final static UUID UUID_NOTIFY = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            mConnectState = ConnectState.CONNECTING;
            updateConnectionState(R.string.menu_connecting);
            invalidateOptionsMenu();
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.e(TAG, "ble connected");
                mConnectState = ConnectState.CONNECTED;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.e(TAG, "ble disconnected");
                mConnectState = ConnectState.DISCONNECTED;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.e(TAG, "ble gatt services discovered");
                onServicesDiscovered(mBluetoothLeService.getSupportedGattServices());

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.e(TAG, "ble data available");
                final byte[] txValue = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA_RAW);
                try {
                    String text = new String(txValue, "UTF-8");
                    Log.e(TAG, " receive data : " + text);

                    mDeviceControlFragment.addMessage(new Message(text, Message.MessageType.RECEIVE));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "UnsupportedEncodingException", e);
                }
            }
        }
    };

    private void onServicesDiscovered(List<BluetoothGattService> gattServices) {
        Log.e(TAG, "gattServices count is:" + gattServices.size());

        for (BluetoothGattService service : gattServices) {

            Log.i(TAG, "service uuid : " + service.getUuid().toString());

            if (service.getUuid().toString().equalsIgnoreCase(UUID_SERVICE.toString())) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                Log.e(TAG, "characteristics count is:" + gattServices.size());

                for (BluetoothGattCharacteristic characteristic : characteristics) {

                    if (characteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY.toString())) {

                        Log.e(TAG, "Assigning notify characteristic : " + characteristic.getUuid());

                        mNotifyCharacteristic = characteristic;
                        mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                        mDeviceControlFragment.enableSendButton(true);
                    }
                }
            }
        }
    }

    protected DeviceControlFragment mDeviceControlFragment;

    private ConnectState mConnectState = ConnectState.OTHER;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gatt_services);

        mDeviceControlFragment = (DeviceControlFragment) getSupportFragmentManager().findFragmentById(R.id.device_control_fragment);

        mDeviceControlFragment.setControlCallBack(this);

        final Intent intent = getIntent();
        final BluetoothLeDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
        mDeviceName = device.getName();
        mDeviceAddress = device.getAddress();

        mDeviceControlFragment.setDeviceName(TextUtils.isEmpty(mDeviceName) ? getString(R.string.unknown_device) : mDeviceName);
        ButterKnife.bind(this);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);

        getSupportActionBar().setTitle(TextUtils.isEmpty(mDeviceName) ? getString(R.string.unknown_device) : mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);

        switch (mConnectState) {
            case CONNECTED:
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(true);
                menu.findItem(R.id.menu_connecting).setVisible(false);
                menu.findItem(R.id.menu_disconnecting).setVisible(false);
                break;
            case DISCONNECTED:
                menu.findItem(R.id.menu_connect).setVisible(true);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_connecting).setVisible(false);
                menu.findItem(R.id.menu_disconnecting).setVisible(false);
                break;
            case CONNECTING:
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_connecting).setVisible(true);
                menu.findItem(R.id.menu_disconnecting).setVisible(false);
                break;
            case DISCONNECTING:
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_connecting).setVisible(false);
                menu.findItem(R.id.menu_disconnecting).setVisible(true);
                break;
            default:
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_connecting).setVisible(false);
                menu.findItem(R.id.menu_disconnecting).setVisible(false);
                break;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                mConnectState = ConnectState.CONNECTING;
                updateConnectionState(R.string.menu_connecting);
                invalidateOptionsMenu();
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                mConnectState = ConnectState.DISCONNECTING;
                updateConnectionState(R.string.menu_disconnecting);
                invalidateOptionsMenu();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            mConnectState = ConnectState.CONNECTING;
            invalidateOptionsMenu();
            updateConnectionState(R.string.menu_connecting);
            Log.e(TAG, "Connect request result=" + result);
        }
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int colourId;

                switch (resourceId) {
                    case R.string.connected:
                        colourId = android.R.color.holo_green_dark;
                        break;
                    case R.string.disconnected:
                        colourId = android.R.color.holo_red_dark;
                        break;
                    default:
                        colourId = android.R.color.black;
                        break;
                }

                mConnectionState.setText(resourceId);
                mConnectionState.setTextColor(getResources().getColor(colourId));
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void send(String character) {
        Log.e(TAG, "send character: " + character);

        if (character.length() == 0) {
            Toast.makeText(this, R.string.text_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBluetoothLeService != null && mNotifyCharacteristic != null) {
            mBluetoothLeService.writeCharacteristic(mNotifyCharacteristic, character);

            mDeviceControlFragment.clearEditText();
            mDeviceControlFragment.addMessage(new Message(character, Message.MessageType.SEND));
        }
    }

}