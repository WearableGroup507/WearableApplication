package tw.edu.ntust.jojllman.wearableapplication;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;

import java.util.List;

import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoLibrary;
import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService;

public class AppManageActivity extends BlunoLibrary {

    private static String TAG = AppManageActivity.class.getSimpleName();
    private Button mButtonScan;
    private Intent mTransferIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_SERVICE");

    private MsgReceiver mMsgReceiver;

    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            front  = intent.getIntExtra("front", 0);
//            left   = intent.getIntExtra("left", 0);
//            right  = intent.getIntExtra("right", 0);
            connectionState = intent.getStringExtra("connectionState");

            mConnectionState = theConnectionState.valueOf(connectionState);
            onConnectionStateChange(mConnectionState);
//            serialReceivedFront.setText(Integer.toString(front));
//            serialReceivedLeft.setText(Integer.toString(left));
//            serialReceivedRight.setText(Integer.toString(right));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manage);

        View decorView = getWindow().getDecorView();
        /*decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/

        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Create BluetoothLeService
        onCreateProcess();
        findView();

        //Register message receiver
        mMsgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_ACTIVITY");
        registerReceiver(mMsgReceiver, intentFilter);

        if(!GlobalVariable.isServiceRunning(getApplicationContext(), "tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService")) {
            Intent intent = new Intent(AppManageActivity.this, BlunoService.class);
            startService(intent);
        }
    }

    @Override
    protected void onDestroy() {
        if(GlobalVariable.isServiceRunning(getApplicationContext(), "tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService")) {
            Intent intent = new Intent(this, BlunoService.class);
            stopService(intent);
        }
        super.onDestroy();
        unregisterReceiver(mMsgReceiver);
//        unbindService(mServiceConnection);
        Log.i(TAG, "onDestroy");
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(AppManageActivity.this.getResources().getText(R.string.app_manage));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnSearchDeviceClick(View view){

        if(!GlobalVariable.isServiceRunning(getApplicationContext(), "tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService")) {
            onCreateProcess();

            Intent intent = new Intent(AppManageActivity.this, BlunoService.class);
            startService(intent);
        }

        switch (mConnectionState) {
            case isNull:
                connectionState="isScanning";
                mConnectionState = theConnectionState.valueOf(connectionState);
                onConnectionStateChange(mConnectionState);
                scanLeDevice(true);
                mScanDeviceDialog.show();
                break;
            case isToScan:
                connectionState="isScanning";
                mConnectionState = theConnectionState.valueOf(connectionState);
                onConnectionStateChange(mConnectionState);
                scanLeDevice(true);
                mScanDeviceDialog.show();
                break;
            case isScanning:
                break;
            case isConnecting:
                break;
            case isConnected:
                break;
            case isDisconnecting:
                break;
            default:
                break;
        }
    }

    public void OnAppSettingClick(View view){
        Intent intent = new Intent();
        intent.setClass(AppManageActivity.this  , AppSettingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionStateChange(theConnectionState mConnectionState) {
        switch (mConnectionState) {
            case isConnected:
//                buttonScan.setText("Connected");
                break;
            case isConnecting:
//                buttonScan.setText("Connecting");
                mTransferIntent.putExtra("mDeviceAddress", mDeviceAddress);
                mTransferIntent.putExtra("connectionState", connectionState);
//                mThresholdIntent.putExtra("frontThreshold", mThresholdIntent);
//                mThresholdIntent.putExtra("sidesThreshold", mThresholdIntent);
                sendBroadcast(mTransferIntent);
                ((GlobalVariable)getApplicationContext()).getSaved_devices().addDevice(mDeviceName, mDeviceAddress);
//                sendBroadcast(mThresholdIntent);
                //TODO: move to app setting
                break;
            case isToScan:
//                buttonScan.setText("Scan");
                break;
            case isScanning:
//                buttonScan.setText("Scanning");
                break;
            case isDisconnecting:
//                buttonScan.setText("isDisconnecting");
                break;
        }
    }

    protected void findView() {
        mButtonScan = (Button)findViewById(R.id.btn_search_device);
    }
}
