package tw.edu.ntust.jojllman.wearableapplication;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoLibrary;
import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService;
import tw.edu.ntust.jojllman.wearableapplication.BLE.GloveService;

public class HearingSupportActivity extends BlunoLibrary {
    private Intent mTransferIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_SERVICE");
    private GlobalVariable globalVariable;

    private int click_count;
    private Handler handler=new Handler();
//    private DeviceInfoView btn_device_glove;
    private RelativeLayout layout_glove_dev;

    private Runnable autoConnectRunnable;
    private boolean killAutoConnectRunnable = false;
    private boolean useTextSignal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearing_menu);

        globalVariable = (GlobalVariable)getApplicationContext();

//        btn_device_glove = (DeviceInfoView)findViewById(R.id.dev_info_btn_hearing_glove);
//
//        btn_device_glove.setDeviceType(DeviceInfoView.GLOVE);
//
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.glove);
//        btn_device_glove.setBitmapToDraw(bitmap);

        layout_glove_dev = (RelativeLayout) findViewById(R.id.layout_glove_dev);

        View decorView = getWindow().getDecorView();
        /*decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/

        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        short auto = getIntent().getShortExtra("AutoEnter", (short) 0);
        if(auto == 0) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if(!GlobalVariable.isServiceRunning(getApplicationContext(), "tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService")) {
            Intent intent = new Intent(this, BlunoService.class);
            startService(intent);
        }

        killAutoConnectRunnable = false;
        autoConnectRunnable = new Runnable() {
            @Override
            public void run() {
                if(!killAutoConnectRunnable) {
                    for (BluetoothDevice device : getScannedDevices()) {
                        String devNameLow;
                        if(device.getName() == null){
                            devNameLow = getString(R.string.unknown_device);
                        }else{
                            devNameLow = device.getName().toLowerCase();
                        }
                        if (globalVariable.getSaved_devices().containsDeviceAddr(device.getAddress()) && devNameLow.startsWith(GlobalVariable.defaultNameGlove.toLowerCase())) {

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
                    handler.postDelayed(this, 2000);
                }else{
                    handler.removeCallbacksAndMessages(this);
                }
            }
        };
    }

    protected void onDestroy(){
        if(GlobalVariable.isServiceRunning(getApplicationContext(), "tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService")) {
            Intent intent = new Intent(this, BlunoService.class);
            stopService(intent);
        }
        super.onDestroy();
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
                ((TextView)layout_glove_dev.getChildAt(1)).setText("訊號 " + getTxtSignal((GloveService.gloveService.rssiLeft + GloveService.gloveService.rssiRight) / 2));
                handler.postDelayed(this, 500); // set time here to refresh textView
            }
        });

        killAutoConnectRunnable = false;
        handler.post(autoConnectRunnable);
    }

    @Override
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

    private String getTxtSignal(int signal){
        if(!useTextSignal){
            return ""+signal;
        }
        if(signal < -100){
            return "弱";
        }else if(signal < -50){
            return "中";
        }else if(signal < 0){
            return "強";
        }else{
            return "未知";
        }
    }

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
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(HearingSupportActivity.this.getResources().getText(R.string.hearing_main));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnDeviceClick(View view){
        if(view.getId() == R.id.layout_glove_dev) {
            if (GloveService.gloveService.mBluetoothDeviceLeft != null &&
                    GloveService.gloveService.mBluetoothDeviceRight != null &&
                    GloveService.gloveService.mBluetoothDeviceLeft != GloveService.gloveService.mBluetoothDeviceRight) {
                Intent intent = new Intent();
                intent.setClass(HearingSupportActivity.this, GloveControlActivity.class);
                startActivity(intent);
            }
        }else if(view.getId() == R.id.layout_hearing_search_device){
            Intent intent = new Intent();
            intent.setClass(this, HearingSearchActivity.class);
            startActivity(intent);
        }
    }
//
//    public void OnHelpClick(View view){
//        DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//
//        final Dialog dialog = new Dialog(HearingSupportActivity.this,R.style.CustomDialog){
//            @Override
//            public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
//                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                    String contents=HearingSupportActivity.this.getResources().getText(R.string.help_dialog).toString()+"，";
//                    contents+=HearingSupportActivity.this.getResources().getText(R.string.help_message).toString();
//                    event.getText().add(contents);
//                    return true;
//                }
//                return super.dispatchPopulateAccessibilityEvent(event);
//            }
//        };
//        dialog.setContentView(R.layout.dialog_help);
//
//        ImageButton ibtn = (ImageButton)dialog.findViewById(R.id.img_btn_setting);
//        ibtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(click_count++>2){
//                    click_count=0;
//                    if(dialog.isShowing()) {
//                        dialog.hide();
//                    }
//                    Intent intent = new Intent();
//                    intent.setClass(HearingSupportActivity.this  , AppManageActivity.class);
//                    startActivity(intent);
//                }
//            }
//        });
//
//        Button btn = (Button)dialog.findViewById(R.id.btn_ok);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (dialog.isShowing()) {
//                    click_count = 0;
//                    dialog.hide();
//                }
//            }
//        });
//
//        dialog.show();
//    }

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
                killAutoConnectRunnable = true;
                scanLeDevice(false);
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
