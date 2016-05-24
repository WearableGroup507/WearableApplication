package tw.edu.ntust.jojllman.wearableapplication;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.List;

import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoLibrary;
import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService;

public class VisualSupportActivity extends BlunoLibrary {
    private static String TAG = VisualSupportActivity.class.getSimpleName();
    private Intent mTransferIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_SERVICE");
    private GlobalVariable globalVariable;

    private Handler handler=new Handler();
    private DeviceInfoView btn_device_glass;
    private DeviceInfoView btn_device_bracelet;
    private int click_count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_support);

        globalVariable = (GlobalVariable)getApplicationContext();
        
        btn_device_glass = (DeviceInfoView)findViewById(R.id.dev_info_btn_visual_glass);
        btn_device_bracelet = (DeviceInfoView)findViewById(R.id.dev_info_btn_visual_bracelet);

        btn_device_glass.setDeviceType(DeviceInfoView.GLASS);
        btn_device_bracelet.setDeviceType(DeviceInfoView.BRACELET);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.glass);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.bracelet);
        btn_device_glass.setBitmapToDraw(bitmap);
        btn_device_bracelet.setBitmapToDraw(bitmap2);

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
    }

    @Override
    public void onResume(){
        super.onResume();

        onCreateProcess();
        switch (mConnectionState) {
            case isNull:
                connectionState="isScanning";
                mConnectionState = theConnectionState.valueOf(connectionState);
                onConnectionStateChange(mConnectionState);
                scanLeDevice(true);
                break;
            case isToScan:
                connectionState="isScanning";
                mConnectionState = theConnectionState.valueOf(connectionState);
                onConnectionStateChange(mConnectionState);
                scanLeDevice(true);
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

        handler.post(new Runnable() {
            @Override
            public void run() {
                btn_device_glass.setSignal((short) ((100-98) / 2));        //TODO: get glass rssi
                btn_device_bracelet.setSignal((short) ((100-98) / 2));     //TODO: get bracelet rssi
                handler.postDelayed(this, 500); // set time here to refresh textView
            }
        });

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (BluetoothDevice device : getScannedDevices()) {
                    String devNameLow = device.getName().toLowerCase();
                    if (globalVariable.getSaved_devices().containsDeviceAddr(device.getAddress()) && devNameLow.startsWith(GlobalVariable.defaultNameGlass.toLowerCase()) ||
                            globalVariable.getSaved_devices().containsDeviceAddr(device.getAddress()) && devNameLow.startsWith(GlobalVariable.defaultNameBracelet.toLowerCase())) {

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
                            connectionState = "isConnecting";
                            mConnectionState = theConnectionState.valueOf(connectionState);
                            onConnectionStateChange(mConnectionState);
                        }
                    }
                }
                handler.postDelayed(this, 500);
            }
        });
    }

    public void onPause(){
        scanLeDevice(false);
        handler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(VisualSupportActivity.this.getResources().getText(R.string.visual_main));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnDeviceClick(View view){
        if(view.getId() == R.id.dev_info_btn_visual_glass){
            // 增加眼鏡按下動作
            Log.d(TAG,"dev_info_btn_visual_glass pressed");
            if(!BlunoService.getReadUltraSound()){
                BlunoService.setReadUltraSound(true);
                view.announceForAccessibility("開啟眼鏡避障功能");
            }else{
                BlunoService.setReadUltraSound(false);
                view.announceForAccessibility("關閉眼鏡避障功能");
            }
        }

        if(view.getId() == R.id.dev_info_btn_visual_bracelet){
            // TODO: 增加手環按下動作
            Log.d(TAG,"dev_info_btn_visual_bracelet pressed");
        }
    }

    public void OnHelpClick(View view){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final Dialog dialog = new Dialog(VisualSupportActivity.this,R.style.CustomDialog){
            @Override
            public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    String contents=VisualSupportActivity.this.getResources().getText(R.string.help_dialog).toString()+"，";
                    contents+=VisualSupportActivity.this.getResources().getText(R.string.help_message).toString();
                    event.getText().add(contents);
                    return true;
                }
                return super.dispatchPopulateAccessibilityEvent(event);
            }
        };
        dialog.setContentView(R.layout.dialog_help);

        ImageButton ibtn = (ImageButton)dialog.findViewById(R.id.img_btn_setting);
        ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(click_count++>2){
                    click_count=0;
                    if(dialog.isShowing()) {
                        dialog.hide();
                    }
                    Intent intent = new Intent();
                    intent.setClass(VisualSupportActivity.this  , AppManageActivity.class);
                    startActivity(intent);
                }
            }
        });

        Button btn = (Button)dialog.findViewById(R.id.btn_ok);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing()) {
                    click_count = 0;
                    dialog.hide();
                }
            }
        });

        dialog.show();
    }

    public boolean isTalkbackEnabled()
    {
        Intent screenReaderIntent = new Intent("android.accessibilityservice.AccessibilityService");
        screenReaderIntent.addCategory("android.accessibilityservice.category.FEEDBACK_SPOKEN");
        List<ResolveInfo> screenReaders = getPackageManager().queryIntentServices(
                screenReaderIntent, 0);
        ContentResolver cr = getContentResolver();
        Cursor cursor = null;
        int status = 0;
        for (ResolveInfo screenReader : screenReaders) {
            // All screen readers are expected to implement a content provider
            // that responds to
            // content://<nameofpackage>.providers.StatusProvider
            cursor = cr.query(Uri.parse("content://" + screenReader.serviceInfo.packageName
                    + ".providers.StatusProvider"), null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                // These content providers use a special cursor that only has one element,
                // an integer that is 1 if the screen reader is running.
                status = cursor.getInt(0);
                cursor.close();
                if (status == 1) {
                    return true;
                }
            }
        }
        return false;
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
}
