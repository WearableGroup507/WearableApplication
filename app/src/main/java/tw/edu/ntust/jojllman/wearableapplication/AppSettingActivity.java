package tw.edu.ntust.jojllman.wearableapplication;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService;

public class AppSettingActivity extends AppCompatActivity {
    private GlobalVariable mGlobalVariable;
    private SeekBar mSeekBar_front;
    private SeekBar mSeekBar_side;
    private TextView mtxt_threshold_front;
    private TextView mtxt_threshold_side;
    private Switch mSwitch_distance;
    private Switch mSwitch_color;
    private TextView mtxt_glass_connected;
    private TextView mtxt_bracelet_connected;
    private TextView mtxt_bracelet_color;
    private TextView mtxt_bracelet_distance;


    private boolean mConnected_Glass = false;
    private boolean mConnected_Bracelet = false;

    private Intent mThresholdIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_THRESHOLD");
    private Intent mRequestConnectedIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.REQUEST_CONNECTED_DEVICES");
    private MsgReceiver mMsgReceiver;

    private Thread mThreadBracelet;

    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mConnected_Glass = intent.getBooleanExtra("Connected_Glass", false);
            mConnected_Bracelet = intent.getBooleanExtra("Connected_Bracelet", false);
            mtxt_glass_connected.setText(mConnected_Glass?R.string.device_connected:R.string.device_disconnected);
            mtxt_bracelet_connected.setText(mConnected_Bracelet?R.string.device_connected:R.string.device_disconnected);

            if(mConnected_Bracelet) {
                mThreadBracelet = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while(true) {
                                Thread.sleep(2000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mtxt_bracelet_distance.setText("顏色偵測R:" + BlunoService.Bracelet_R + " G:" + BlunoService.Bracelet_G + " B" + BlunoService.Bracelet_B);
                                        mtxt_bracelet_color.setText("距離偵測:" + BlunoService.Bracelet_DT + "mm");

                                    }
                                });
                            }
                        }
                        catch (InterruptedException e1)
                        {// TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                });
                mThreadBracelet.start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);

        mGlobalVariable = (GlobalVariable)getApplicationContext();

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

        findView();
        initialize();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mMsgReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp(){

        if(mGlobalVariable.isSettingChanged()) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(AppSettingActivity.this);
            builder.setMessage(R.string.accept_dialog)
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mGlobalVariable.saveSetting();
                            AppSettingActivity.this.finish();
                        }
                    })
                    .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mGlobalVariable.readSetting();
                            AppSettingActivity.this.finish();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(AppSettingActivity.this);
            builder.setMessage(R.string.accept_dialog)
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mGlobalVariable.saveSetting();
                            AppSettingActivity.this.finish();
                        }
                    })
                    .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mGlobalVariable.readSetting();
                            AppSettingActivity.this.finish();
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
            String contents=AppSettingActivity.this.getResources().getText(R.string.app_setting).toString()+"，";
            contents+=AppSettingActivity.this.getResources().getText(R.string.txt_set_vibrate_level).toString()+"，";
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

    private void findView() {
        mSeekBar_front = (SeekBar)findViewById(R.id.seekBar_glass_front);
        mSeekBar_side = (SeekBar)findViewById(R.id.seekBar_glass_side);
        mtxt_threshold_front = (TextView)findViewById(R.id.txt_glass_front_threshold_current);
        mtxt_threshold_side = (TextView)findViewById(R.id.txt_glass_side_threshold_current);
        mSwitch_distance = (Switch)findViewById(R.id.switch_distance);
        mSwitch_color = (Switch)findViewById(R.id.switch_color);
        mtxt_glass_connected = (TextView)findViewById(R.id.txt_glass_connected);
        mtxt_bracelet_connected = (TextView)findViewById(R.id.txt_bracelet_connected);
        mtxt_bracelet_distance = (TextView)findViewById(R.id.txt_bracelet_distance);
        mtxt_bracelet_color = (TextView)findViewById(R.id.txt_bracelet_color);
    }

    private void initialize() {
        final int glass_threshold_front = mGlobalVariable.getGlassFrontThreshold();
        final int glass_threshold_side = mGlobalVariable.getGlassSideThreshold();
        final boolean bracelet_distance_enabled = mGlobalVariable.isBraceletDistanceEnabled();
        final boolean bracelet_color_enabled = mGlobalVariable.isBraceletColorEnabled();

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

        mSwitch_distance.setChecked(bracelet_distance_enabled);
        mSwitch_color.setChecked(bracelet_color_enabled);

        mSwitch_distance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mGlobalVariable.setBraceletDistanceEnabled(b);
            }
        });

        mSwitch_color.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mGlobalVariable.setBraceletColorEnabled(b);
            }
        });

        mMsgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tw.edu.ntust.jojllman.wearableapplication.RESPONSE_CONNECTED_DEVICES");
        registerReceiver(mMsgReceiver, intentFilter);

        sendBroadcast(mRequestConnectedIntent);
    }


}
