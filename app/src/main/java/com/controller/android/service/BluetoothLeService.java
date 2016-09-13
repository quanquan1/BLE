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

package com.controller.android.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA_RAW = "com.example.bluetooth.le.EXTRA_DATA_RAW";
    public final static String EXTRA_UUID_CHAR = "com.example.bluetooth.le.EXTRA_UUID_CHAR";
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private final IBinder mBinder = new LocalBinder();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;


    //To tell the onCharacteristicWrite call back function that this is a new characteristic,
    //not the Write Characteristic to the device successfully.
    private static final int WRITE_NEW_CHARACTERISTIC = -1;
    //define the limited length of the characteristic.
    private static final int MAX_CHARACTERISTIC_LENGTH = 17;
    //Show that Characteristic is writing or not.
    private boolean mIsWritingCharacteristic = false;

    //class to store the Characteristic and content string push into the ring buffer.
    private class BluetoothGattCharacteristicHelper {
        BluetoothGattCharacteristic mCharacteristic;
        String mCharacteristicValue;

        BluetoothGattCharacteristicHelper(BluetoothGattCharacteristic characteristic, String characteristicValue) {
            mCharacteristic = characteristic;
            mCharacteristicValue = characteristicValue;
        }
    }

    //ring buffer
    private RingBuffer<BluetoothGattCharacteristicHelper> mCharacteristicRingBuffer = new RingBuffer<BluetoothGattCharacteristicHelper>(8);


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "onCharacteristicChanged");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            Log.e(TAG, "onCharacteristicRead" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            Log.e(TAG, "onCharacteristicWrite" + status);
            //this block should be synchronized to prevent the function overloading
            synchronized (this) {
                //CharacteristicWrite success
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG, "onCharacteristicWrite success:" + new String(characteristic.getValue()));
                    if (mCharacteristicRingBuffer.isEmpty()) {
                        mIsWritingCharacteristic = false;
                    } else {
                        BluetoothGattCharacteristicHelper bluetoothGattCharacteristicHelper = mCharacteristicRingBuffer.next();
                        if (bluetoothGattCharacteristicHelper.mCharacteristicValue.length() > MAX_CHARACTERISTIC_LENGTH) {
                            try {
                                bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(0, MAX_CHARACTERISTIC_LENGTH).getBytes("ISO-8859-1"));

                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }


                            if (mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                                Log.e(TAG, "writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
                            } else {
                                Log.e(TAG, "writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                            }
                            bluetoothGattCharacteristicHelper.mCharacteristicValue = bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(MAX_CHARACTERISTIC_LENGTH);
                        } else {
                            try {
                                bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }

                            if (mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                                Log.e(TAG, "writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
                            } else {
                                Log.e(TAG, "writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                            }
                            bluetoothGattCharacteristicHelper.mCharacteristicValue = "";
                            mCharacteristicRingBuffer.pop();
                        }
                    }
                }
                //WRITE a NEW CHARACTERISTIC
                else if (status == WRITE_NEW_CHARACTERISTIC) {
                    Log.e(TAG, "WRITE_NEW_CHARACTERISTIC");
                    if ((!mCharacteristicRingBuffer.isEmpty()) && mIsWritingCharacteristic == false) {
                        BluetoothGattCharacteristicHelper bluetoothGattCharacteristicHelper = mCharacteristicRingBuffer.next();
                        if (bluetoothGattCharacteristicHelper.mCharacteristicValue.length() > MAX_CHARACTERISTIC_LENGTH) {

                            try {
                                bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(0, MAX_CHARACTERISTIC_LENGTH).getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }

                            if (mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                                System.out.println("writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
                            } else {
                                System.out.println("writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                            }
                            bluetoothGattCharacteristicHelper.mCharacteristicValue = bluetoothGattCharacteristicHelper.mCharacteristicValue.substring(MAX_CHARACTERISTIC_LENGTH);
                        } else {
                            try {
                                bluetoothGattCharacteristicHelper.mCharacteristic.setValue(bluetoothGattCharacteristicHelper.mCharacteristicValue.getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }


                            if (mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristicHelper.mCharacteristic)) {
                                Log.e(TAG, "writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":success");
                            } else {
                                Log.e(TAG, "writeCharacteristic init " + new String(bluetoothGattCharacteristicHelper.mCharacteristic.getValue()) + ":failure");
                            }
                            bluetoothGattCharacteristicHelper.mCharacteristicValue = "";
                            mCharacteristicRingBuffer.pop();
                        }
                    }

                    mIsWritingCharacteristic = true;

                    //clear the buffer to prevent the lock of the mIsWritingCharacteristic
                    if (mCharacteristicRingBuffer.isFull()) {
                        mCharacteristicRingBuffer.clear();
                        mIsWritingCharacteristic = false;
                    }
                } else {
                    // //CharacteristicWrite fail
                    Log.e(TAG, "onCharacteristicWrite fail:" + new String(characteristic.getValue()));
                    mCharacteristicRingBuffer.clear();
                }
            }
        }


        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            final String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.e(TAG, "onServicesDiscovered received: " + status);
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_UUID_CHAR, characteristic.getUuid().toString());

        // Always try to add the RAW value
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA_RAW, data);
        }

        sendBroadcast(intent);
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        Log.w(TAG, "start connect: " + address);
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {

            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }


    /**
     * Write information to the device on a given {@code BluetoothGattCharacteristic}. The content string and characteristic is
     * only pushed into a ring buffer. All the transmission is based on the {@code onCharacteristicWrite} call back function,
     * which is called directly in this function
     *
     * @param characteristic The characteristic to write to.
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        //The character size of TI CC2540 is limited to 17 bytes, otherwise characteristic can not be sent properly,
        //so String should be cut to comply this restriction. And something should be done here:
        String writeCharacteristicString;
        try {
            writeCharacteristicString = new String(characteristic.getValue(), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            // this should never happen because "US-ASCII" is hard-coded.
            throw new IllegalStateException(e);
        }
        Log.e(TAG, "allwriteCharacteristicString:" + writeCharacteristicString);

        //As the communication is asynchronous content string and characteristic should be pushed into an ring buffer for further transmission
        mCharacteristicRingBuffer.push(new BluetoothGattCharacteristicHelper(characteristic, writeCharacteristicString));

        Log.e(TAG, "mCharacteristicRingBufferlength:" + mCharacteristicRingBuffer.size());


        //The progress of onCharacteristicWrite and writeCharacteristic is almost the same. So callback function is called directly here
        //for details see the onCharacteristicWrite function
        mGattCallback.onCharacteristicWrite(mBluetoothGatt, characteristic, WRITE_NEW_CHARACTERISTIC);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(final BluetoothGattCharacteristic characteristic, final boolean enabled) {
        Log.e(TAG, "setCharacteristicNotification " + enabled);
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor : descriptors) {
            descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    public void writeCharacteristic(BluetoothGattCharacteristic notifyCharacteristic, String value) {
        notifyCharacteristic.setValue(value.getBytes());
        mBluetoothGatt.writeCharacteristic(notifyCharacteristic);
    }
}