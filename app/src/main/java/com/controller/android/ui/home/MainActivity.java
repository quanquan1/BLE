package com.controller.android.ui.home;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.controller.android.R;
import com.controller.android.bluetoothlelib.device.BluetoothLeDevice;
import com.controller.android.containers.BluetoothLeDeviceStore;
import com.controller.android.ui.setting.SettingActivity;
import com.controller.android.util.BluetoothLeScanner;
import com.controller.android.util.BluetoothUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.alt236.easycursor.objectcursor.EasyObjectCursor;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    @Bind(R.id.tvBluetoothLe)
    protected TextView mTvBluetoothLeStatus;
    @Bind(R.id.tvBluetoothStatus)
    protected TextView mTvBluetoothStatus;
    @Bind(android.R.id.list)
    protected ListView mList;
    @Bind(android.R.id.empty)
    protected View mEmpty;

    private BluetoothUtils mBluetoothUtils;
    private BluetoothLeScanner mScanner;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothLeDeviceStore mDeviceStore;

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

            final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, rssi, scanRecord, System.currentTimeMillis());
            mDeviceStore.addDevice(deviceLe);
            final EasyObjectCursor<BluetoothLeDevice> c = mDeviceStore.getDeviceCursor();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.swapCursor(c);
                }
            });
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mList.setEmptyView(mEmpty);
        mList.setOnItemClickListener(this);
        mDeviceStore = new BluetoothLeDeviceStore();
        mBluetoothUtils = new BluetoothUtils(this);
        mScanner = new BluetoothLeScanner(mLeScanCallback, mBluetoothUtils);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanner.isScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_progress_indeterminate);
        }

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        final BluetoothLeDevice device = mLeDeviceListAdapter.getItem(position);
        if (device == null) return;

        final Intent intent = new Intent(this, DeviceDetailsActivity.class);
        intent.putExtra(DeviceDetailsActivity.EXTRA_DEVICE, device);

        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                startScan();
                break;
            case R.id.menu_stop:
                mScanner.scanLeDevice(-1, false);
                invalidateOptionsMenu();
                break;
            case R.id.setting:
                SettingActivity.startSettingActivity(this);
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScanner.scanLeDevice(-1, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        final boolean mIsBluetoothOn = mBluetoothUtils.isBluetoothOn();
        final boolean mIsBluetoothLePresent = mBluetoothUtils.isBluetoothLeSupported();

        if (mIsBluetoothOn) {
            mTvBluetoothStatus.setText(R.string.on);
        } else {
            mTvBluetoothStatus.setText(R.string.off);
        }

        if (mIsBluetoothLePresent) {
            mTvBluetoothLeStatus.setText(R.string.supported);
        } else {
            mTvBluetoothLeStatus.setText(R.string.not_supported);
        }

        invalidateOptionsMenu();
    }

    private void startScan() {
        final boolean mIsBluetoothOn = mBluetoothUtils.isBluetoothOn();
        final boolean mIsBluetoothLePresent = mBluetoothUtils.isBluetoothLeSupported();
        mDeviceStore.clear();

        mLeDeviceListAdapter = new LeDeviceListAdapter(this, mDeviceStore.getDeviceCursor());
        mList.setAdapter(mLeDeviceListAdapter);

        mBluetoothUtils.askUserToEnableBluetoothIfNeeded();
        if (mIsBluetoothOn && mIsBluetoothLePresent) {
            mScanner.scanLeDevice(-1, true);
            invalidateOptionsMenu();
        }
    }
}
