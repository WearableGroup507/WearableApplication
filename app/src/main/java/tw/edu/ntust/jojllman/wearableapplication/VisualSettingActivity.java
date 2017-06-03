package tw.edu.ntust.jojllman.wearableapplication;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoLibrary;
import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService;

public class VisualSettingActivity extends BlunoLibrary {
    private GlobalVariable mGlobalVariable;
    private SeekBar mSeekBar_front;
    private SeekBar mSeekBar_side;
    private TextView mtxt_threshold_front;
    private TextView mtxt_threshold_side;
    private Button system_reset;
    private Button search_bracelet;
    private Button search_glass;


    private boolean mConnected_Glass = false;
    private boolean mConnected_Bracelet = false;
    private boolean mConnected_Glove_Left = false;
    private boolean mConnected_Glove_Right = false;
    private Intent mThresholdIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_THRESHOLD");
    private Intent mRequestConnectedIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.REQUEST_CONNECTED_DEVICES");
    private Intent braceletControlIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.BRACELET_SEND_CONTROL");
    private Intent mTransferIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_SERVICE");

    private MsgReceiver mMsgReceiver;
    private MsgReceiver scan_MsgReceiver;
    private Handler mHandler = new Handler();
    private boolean killRunnable = false;

    @Override
    public void onConnectionStateChange(theConnectionState mConnectionState) {
        switch (mConnectionState) {
            case isConnected:
//                buttonScan.setText("Connected");
                break;
            case isConnecting:
//                buttonScan.setText("Connecting");
                if(mDeviceName.startsWith("Nordic_Bracelet"))
                    GlobalVariable.braceletAddress = mDeviceAddress;
                else {
                    GlobalVariable.glassesAddress = mDeviceAddress;
                }
                mTransferIntent.putExtra("mDeviceAddress", mDeviceAddress);
                mTransferIntent.putExtra("connectionState", connectionState);
//                mThresholdIntent.putExtra("frontThreshold", mThresholdIntent);
//                mThresholdIntent.putExtra("sidesThreshold", mThresholdIntent);
                sendBroadcast(mTransferIntent);
                ((GlobalVariable)getApplicationContext()).getSaved_devices().addDevice(mDeviceName, mDeviceAddress);
                this.finish();
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

    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.compareTo("tw.edu.ntust.jojllman.wearableapplication.RESPONSE_CONNECTED_DEVICES") == 0 ||
                    action.compareTo("tw.edu.ntust.jojllman.wearableapplication.DISCONNECTED_DEVICES") == 0) {
                mConnected_Glass = intent.getBooleanExtra("Connected_Glass", false);
                mConnected_Bracelet = intent.getBooleanExtra("Connected_Bracelet", false);
                mConnected_Glove_Left = intent.getBooleanExtra("Connected_Glove_Left", false);
                mConnected_Glove_Right = intent.getBooleanExtra("Connected_Glove_Right", false);
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SettingTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_setting);

        mGlobalVariable = (GlobalVariable)getApplicationContext();

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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setTitle("裝置設定");

        findView();
        initialize();

        scan_MsgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_ACTIVITY");
        registerReceiver(scan_MsgReceiver, intentFilter);



        system_reset = (Button) findViewById(R.id.system_reset);
        system_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGlobalVariable.initDevice();
                System.out.println("Device log file init().");
                braceletControlIntent.putExtra("BraceletDisconnect",true);
                braceletControlIntent.putExtra("GlassDisconnect",true);
                sendBroadcast(braceletControlIntent);

                BlunoService.initName();
                mGlobalVariable.braceletAddress= null;
                mGlobalVariable.glassesAddress=null;
                VisualSupportActivity.colorInit();
                BlunoService.speak("系統已重置。");
            }
        });
        search_bracelet = (Button) findViewById(R.id.ble_bracelet_btn);
        search_bracelet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("brace here");
//                Intent intent = new Intent();
//                intent.setClass(VisualSettingActivity.this, VisualSearchActivity.class);
//                startActivity(intent);
//                VisualSettingActivity.this.finish();

                scanlist();
            }
        });
        search_glass = (Button) findViewById(R.id.ble_glass_btn);
        search_glass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("glass here");
//                Intent intent = new Intent();
//                intent.setClass(VisualSettingActivity.this, VisualSearchActivity.class);
//                startActivity(intent);
//                VisualSettingActivity.this.finish();
                scanlist();
            }
        });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mMsgReceiver);
        unregisterReceiver(scan_MsgReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp(){

        if(mGlobalVariable.isSettingChanged()) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(VisualSettingActivity.this);
            builder.setMessage(R.string.accept_dialog)
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mGlobalVariable.saveSetting();
                            VisualSettingActivity.this.finish();
                        }
                    })
                    .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mGlobalVariable.readSetting();
                            VisualSettingActivity.this.finish();
                        }
                    });
            builder.create().show();

        }
        else {
            onBackPressed();
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && mGlobalVariable.isSettingChanged())
        {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(VisualSettingActivity.this);
            builder.setMessage(R.string.accept_dialog)
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mGlobalVariable.saveSetting();
                            VisualSettingActivity.this.finish();
                        }
                    })
                    .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mGlobalVariable.readSetting();
                            VisualSettingActivity.this.finish();
                        }
                    });
            builder.create().show();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String contents=VisualSettingActivity.this.getResources().getText(R.string.app_setting).toString()+"，";
            contents+=VisualSettingActivity.this.getResources().getText(R.string.txt_set_vibrate_level).toString()+"，";
            switch(mGlobalVariable.getVibrate_level()){
                case GlobalVariable.VIBRATE_LIGHT:
                    contents+="目前強度，弱";
                    break;
                case GlobalVariable.VIBRATE_MID:
                    contents+="目前強度，中";
                    break;
                case GlobalVariable.VIBRATE_STRONG:
                    contents+="目前強度，強";
                    break;
            }
            event.getText().add(contents);
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnRadioClick(View v) {
        switch(v.getId()){
            case R.id.rdo_light:
                mGlobalVariable.setVibrate_level(GlobalVariable.VIBRATE_LIGHT);
                break;
            case R.id.rdo_mid:
                mGlobalVariable.setVibrate_level(GlobalVariable.VIBRATE_MID);
                break;
            case R.id.rdo_strong:
                mGlobalVariable.setVibrate_level(GlobalVariable.VIBRATE_STRONG);
                break;
        }
    }

//    public void OnGloveSettingClick(View view){
//        if(mConnected_Glove_Left && mConnected_Glove_Right) {
//            Intent intent = new Intent();
//            intent.setClass(AppSettingActivity.this, GloveSettingActivity.class);
//            startActivity(intent);
//        }
//    }

    private void findView() {
        mSeekBar_front = (SeekBar)findViewById(R.id.seekBar_glass_front);
        mSeekBar_side = (SeekBar)findViewById(R.id.seekBar_glass_side);
        mtxt_threshold_front = (TextView)findViewById(R.id.txt_glass_front_threshold_current);
        mtxt_threshold_side = (TextView)findViewById(R.id.txt_glass_side_threshold_current);
    }

    private void initialize() {
        final int glass_threshold_front = mGlobalVariable.getGlassFrontThreshold();
        final int glass_threshold_side = mGlobalVariable.getGlassSideThreshold();

        switch(mGlobalVariable.getVibrate_level()){
            case GlobalVariable.VIBRATE_LIGHT:
                ((RadioButton)findViewById(R.id.rdo_light)).setChecked(true);
                break;
            case GlobalVariable.VIBRATE_MID:
                ((RadioButton)findViewById(R.id.rdo_mid)).setChecked(true);
                break;
            case GlobalVariable.VIBRATE_STRONG:
                ((RadioButton)findViewById(R.id.rdo_strong)).setChecked(true);
                break;
        }

        mtxt_threshold_front.setText(glass_threshold_front + "");
        mtxt_threshold_side.setText(glass_threshold_side + "");
        mSeekBar_front.setProgress(glass_threshold_front);
        mSeekBar_side.setProgress(glass_threshold_side);

        mSeekBar_front.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mGlobalVariable.setGlassFrontThreshold(i);
                mtxt_threshold_front.setText(i + "");
                mThresholdIntent.putExtra("frontThreshold", i);
                mThresholdIntent.putExtra("sidesThreshold", mGlobalVariable.getGlassSideThreshold());
                sendBroadcast(mThresholdIntent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBar_side.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mGlobalVariable.setGlassSideThreshold(i);
                mtxt_threshold_side.setText(i + "");
                mThresholdIntent.putExtra("frontThreshold", mGlobalVariable.getGlassFrontThreshold());
                mThresholdIntent.putExtra("sidesThreshold", i);
                sendBroadcast(mThresholdIntent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mMsgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tw.edu.ntust.jojllman.wearableapplication.RESPONSE_CONNECTED_DEVICES");
        intentFilter.addAction("tw.edu.ntust.jojllman.wearableapplication.DISCONNECTED_DEVICES");
        registerReceiver(mMsgReceiver, intentFilter);

        sendBroadcast(mRequestConnectedIntent);
    }
    public void scanlist(){
        if(!GlobalVariable.isServiceRunning(getApplicationContext(), "tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService")) {

            Intent intent = new Intent(VisualSettingActivity.this, BlunoService.class);
            startService(intent);
        }

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
        killRunnable = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(!isFinishing()) {
                    if (!killRunnable) {
                        if (getScannedDevices().length > 0) {
                            mScanDeviceDialog.show();
                            killRunnable = true;
                        }
                    } else {
                        mHandler.removeCallbacksAndMessages(this);
                    }
                    mHandler.postDelayed(this, 1500);
                }
            }
        });
    }
}
