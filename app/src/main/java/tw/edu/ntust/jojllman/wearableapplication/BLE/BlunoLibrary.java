package tw.edu.ntust.jojllman.wearableapplication.BLE;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import tw.edu.ntust.jojllman.wearableapplication.R;

public abstract class BlunoLibrary extends AppCompatActivity {
    private Context mainContext = this;

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    BluetoothLeService mBluetoothLeService;
    private LeDeviceListAdapter mLeDeviceListAdapter = null;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning = false;
    protected AlertDialog mScanDeviceDialog;
    protected String mDeviceName;
    protected String mDeviceAddress;
    public String connectionState = "isNull";

    protected enum theConnectionState {
        isNull, isToScan, isScanning, isConnecting, isConnected, isDisconnecting
    }

    ;
    protected theConnectionState mConnectionState = theConnectionState.isNull;

    private final static String TAG = BlunoLibrary.class.getSimpleName();

    public abstract void onConnectionStateChange(theConnectionState mConnectionState);

    public void onCreateProcess() {
        if (!initiate()) {
            Toast.makeText(mainContext, R.string.error_bluetooth_not_supported,
                    Toast.LENGTH_SHORT).show();
            ((Activity) mainContext).finish();
        }

        if (mBluetoothLeService == null) {
            Log.d(TAG, "Bind BluetoothLeService.");
            System.out.println("mConnectionState="+mConnectionState);
            Intent gattServiceIntent = new Intent(mainContext, BluetoothLeService.class);
            mainContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }

            // Initializes list view adapter.
            mLeDeviceListAdapter = new LeDeviceListAdapter();

        // Initializes and show the scan Device Dialog
        mScanDeviceDialog = new AlertDialog.Builder(mainContext)
                .setTitle("BLE Device Scan...")
                .setAdapter(mLeDeviceListAdapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(which);
                        if (device == null)
                            return;
                        scanLeDevice(false);
                        System.out.println("onListItemClick " + which);
                        System.out.println("onListItemClick " + device.getName());
                        System.out.println("Device Name:" + device.getName() + "   " + "Device Name:" + device.getAddress());

                        mDeviceName = device.getName();
                        mDeviceAddress = device.getAddress();

                        if (mDeviceName == null)
                            mDeviceName = getString(R.string.unknown_device);

                        if (mDeviceName.equals("No Device Available") && mDeviceAddress.equals("No Address Available")) {
                            connectionState = "isToScan";
                            mConnectionState = theConnectionState.valueOf(connectionState);
                            onConnectionStateChange(mConnectionState);
                        } else {
//					mainContext.unbindService(mServiceConnection);
//					mBluetoothLeService = null;
                            connectionState = "isConnecting";
                            mConnectionState = theConnectionState.valueOf(connectionState);
                            onConnectionStateChange(mConnectionState);
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface arg0) {
                        System.out.println("mBluetoothAdapter.stopLeScan");

                        connectionState = "isToScan";
                        mConnectionState = theConnectionState.valueOf(connectionState);
                        onConnectionStateChange(mConnectionState);
                        mScanDeviceDialog.dismiss();

                        scanLeDevice(false);
                    }
                })
                .create();
    }

    boolean initiate() {
        // Use this check to determine whether BLE is supported on the device.
        // Then you can
        // selectively disable BLE-related features.
        if (!mainContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        // Initializes a Bluetooth adapter. For API level 18 and above, get a
        // reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) mainContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
//        if(android.os.Build.VERSION.SDK_INT>=21) {
//            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
//            if (mBluetoothLeScanner == null) {
//                return false;
//            }
//        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }

    public void onPause() {
        if (mBluetoothLeService != null) {
            Log.d(TAG, "onPause() Unbind BluetoothLeService.");
            unbindService(mServiceConnection);
            mBluetoothLeService = null;     //important, because onServiceDisconnected won't call unless something goes wrong.
        }
        super.onPause();
    }

    public void onResume() {
        if (mBluetoothLeService == null) {
            onCreateProcess();
        }
        super.onResume();
    }

    protected void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.

            System.out.println("mBluetoothAdapter.startLeScan");

            if (mLeDeviceListAdapter != null) {
                mLeDeviceListAdapter.clear();
                mLeDeviceListAdapter.notifyDataSetChanged();
            }

            if (!mScanning) {
                mScanning = true;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
//                if(android.os.Build.VERSION.SDK_INT<21) {
//                    mBluetoothAdapter.startLeScan(mLeScanCallback);
//                }else{
//                    mBluetoothLeScanner.startScan(mScanCallback);
//                }
            }
        } else {
            if (mScanning) {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                if(android.os.Build.VERSION.SDK_INT<21) {
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                }else{
//                    mBluetoothLeScanner.stopScan(mScanCallback);
//                }
            }
        }
    }

    protected BluetoothDevice[] getScannedDevices() {
        return mLeDeviceListAdapter.mLeDevices.toArray(new BluetoothDevice[mLeDeviceListAdapter.mLeDevices.size()]);
    }

    // Code to manage Service lifecycle.
    protected final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("Blunolibrary mServiceConnection onServiceConnected");
            System.out.println("mConnectionState="+mConnectionState);
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                //((Activity) mainContext).finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("mServiceConnection onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            ((Activity) mainContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("mLeScanCallback onLeScan run ");
                    mLeDeviceListAdapter.addDevice(device);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

//    private ScanCallback mScanCallback = new ScanCallback() {
//
//        @Override
//        public void onScanResult(int callbackType, final ScanResult result) {
//            ((Activity) mainContext).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    System.out.println("mLeScanCallback onLeScan run ");
//                    mLeDeviceListAdapter.addDevice(result.getDevice());
//                    mLeDeviceListAdapter.notifyDataSetChanged();
//                }
//            });
//        }
//    };

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = ((Activity) mainContext).getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view
                        .findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view
                        .findViewById(R.id.device_name);

                System.out.println("mInflator.inflate  getView");
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }
}
