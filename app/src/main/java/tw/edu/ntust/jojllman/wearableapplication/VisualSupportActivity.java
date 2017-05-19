package tw.edu.ntust.jojllman.wearableapplication;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import tw.edu.ntust.jojllman.wearableapplication.BLE.BluetoothLeService;
import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoLibrary;
import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService;

public class VisualSupportActivity extends BlunoLibrary {
    private static String TAG = VisualSupportActivity.class.getSimpleName();
    private Intent mTransferIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_SERVICE");
//    private Intent braceletDistanceIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.BRACELET_SEND_CONTROL");
    private Intent mREQUEST_CONNECTED_DEVICES = new Intent("tw.edu.ntust.jojllman.wearableapplication.REQUEST_CONNECTED_DEVICES");
    private Intent mRESET_REQUEST = new Intent("tw.ntust.jollman.wearbleapplication.RESET_REQUEST");
    private GlobalVariable globalVariable;
    private GlobalVariable.SavedDevices saveddevice;

    private Handler handler=new Handler();
//    private DeviceInfoView btn_device_glass;
//    private DeviceInfoView btn_device_bracelet;
    private LinearLayout layout_glass_dev;
    private LinearLayout layout_bracelet_dev;
    private boolean m_braceletConnected = false;
    private int click_count=0;

    private Intent braceletControlIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.BRACELET_SEND_CONTROL");
    private Intent glassControlIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.BRACELET_SEND_CONTROL");
    private Runnable autoConnectRunnable;
    private boolean killAutoConnectRunnable = false;
    private boolean useTextSignal = false;
    private Button ring_btn;

    private Handler mHandler = new Handler();
    private boolean killRunnable = false;
    private BlunoService.BraceletState m_braceletState= BlunoService.BraceletState.none;
    private TextToSpeech tts;
    private Button glass_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_menu);

        //final GlobalVariable dglobalVariable = (GlobalVariable)getApplicationContext();
        //dglobalVariable.readSetting();



        globalVariable = (GlobalVariable)getApplicationContext();
        globalVariable.readSetting();

        findView();

//        btn_device_glass = (DeviceInfoView)findViewById(R.id.dev_info_btn_visual_glass);
//        btn_device_bracelet = (DeviceInfoView)findViewById(R.id.dev_info_btn_visual_bracelet);
//
//        btn_device_glass.setDeviceType(DeviceInfoView.GLASS);
//        btn_device_bracelet.setDeviceType(DeviceInfoView.BRACELET);
//
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.glass);
//        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.bracelet);
//        btn_device_glass.setBitmapToDraw(bitmap);
//        btn_device_bracelet.setBitmapToDraw(bitmap2);

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
        createLanguageTTS();
        short auto = getIntent().getShortExtra("AutoEnter", (short) 1);
        if(auto == 0) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        if(!GlobalVariable.isServiceRunning(getApplicationContext(), "tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService")) {
            Intent intent = new Intent(this, BlunoService.class);
            startService(intent);
        }





        m_braceletConnected = false;
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
                        if (globalVariable.getSaved_devices().containsDeviceAddr(device.getAddress()) && devNameLow.startsWith(GlobalVariable.defaultNameGlass.toLowerCase()) ||
                                globalVariable.getSaved_devices().containsDeviceAddr(device.getAddress()) && devNameLow.startsWith(GlobalVariable.defaultNameBracelet.toLowerCase())) {

                            System.out.println("Device Name:" + device.getName() + "   " + "Device Name:" + device.getAddress());

                            mDeviceName = device.getName();
                            mDeviceAddress = device.getAddress();
                            if(mDeviceName.startsWith("Nordic_Bracelet"))
                                GlobalVariable.braceletAddress = mDeviceAddress;
                            else {
                                GlobalVariable.glassesAddress = mDeviceAddress;
                            }
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
                            if(GlobalVariable.braceletAddress != ""){
                                ((LinearLayout)layout_bracelet_dev.getParent()).setBackgroundColor(Color.parseColor("#0047b2"));
                                for(int i=0; i < layout_bracelet_dev.getChildCount(); i++){
                                    ((TextView)(layout_bracelet_dev.getChildAt(i))).setTextColor(Color.parseColor("#ffffff"));
                                }
                            }
                            else {
                                ((LinearLayout)layout_bracelet_dev.getParent()).setBackgroundColor(Color.parseColor("#092557"));
                                for(int i=0; i < layout_bracelet_dev.getChildCount(); i++){
                                    ((TextView)(layout_bracelet_dev.getChildAt(i))).setTextColor(Color.parseColor("#7E7E7E"));
                                }
                            }
                            if(GlobalVariable.glassesAddress != ""){
                                ((LinearLayout)layout_glass_dev.getParent()).setBackgroundColor(Color.parseColor("#0047b2"));
                                for(int i=0; i < layout_glass_dev.getChildCount(); i++){
                                    ((TextView)(layout_glass_dev.getChildAt(i))).setTextColor(Color.parseColor("#ffffff"));
                                }
                            }
                            else {
                                ((LinearLayout)layout_glass_dev.getParent()).setBackgroundColor(Color.parseColor("#092557"));
                                for(int i=0; i < layout_glass_dev.getChildCount(); i++){
                                    ((TextView)(layout_glass_dev.getChildAt(i))).setTextColor(Color.parseColor("#7E7E7E"));
                                }
                            }
                        }
                    }
                    handler.postDelayed(this, 2000);
                }else{
                    handler.removeCallbacksAndMessages(this);
                    Log.d(TAG,"removeCallbacksAndMessages autoConnectRunnable");
                    sendBroadcast(mREQUEST_CONNECTED_DEVICES);
                }

            }
        };
        ring_btn = (Button) findViewById(R.id.ring_btn);
        ring_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BlunoService.getBraceletPower()>0) {
                    braceletControlIntent.putExtra("BraceletDisconnect",true);
                    sendBroadcast(braceletControlIntent);
                    ring_btn.setText("開啟");
                    ((TextView)layout_bracelet_dev.getChildAt(2)).setText("電量 關閉中");
                    ((LinearLayout)layout_bracelet_dev.getParent()).setBackgroundColor(Color.parseColor("#092557"));
                    for(int i=0; i < layout_bracelet_dev.getChildCount(); i++){
                        ((TextView)(layout_bracelet_dev.getChildAt(i))).setTextColor(Color.parseColor("#7E7E7E"));
                    }
                }
                else if(GlobalVariable.braceletAddress != "") {
                    mTransferIntent.putExtra("mDeviceAddress", GlobalVariable.braceletAddress);
                    mTransferIntent.putExtra("connectionState", connectionState);
                    sendBroadcast(mTransferIntent);
                    ring_btn.setText("關閉");
                    ((LinearLayout)layout_bracelet_dev.getParent()).setBackgroundColor(Color.parseColor("#0047b2"));
                    for(int i=0; i < layout_bracelet_dev.getChildCount(); i++){
                        ((TextView)(layout_bracelet_dev.getChildAt(i))).setTextColor(Color.parseColor("#ffffff"));
                    }
                }

            }
        });
        glass_btn = (Button) findViewById(R.id.glass_btn);
        glass_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(BlunoService.Glass_RSSI>0) {
                    braceletControlIntent.putExtra("GlassDisconnect",true);
                    sendBroadcast(braceletControlIntent);
                    glass_btn.setText("開啟");
                    ((TextView)layout_glass_dev.getChildAt(2)).setText("電量 關閉中");
                    ((LinearLayout)layout_glass_dev.getParent()).setBackgroundColor(Color.parseColor("#092557"));
                    for(int i=0; i < layout_glass_dev.getChildCount(); i++){
                        ((TextView)(layout_glass_dev.getChildAt(i))).setTextColor(Color.parseColor("#7E7E7E"));
                    }
                }
                else if(GlobalVariable.glassesAddress != ""){
                    mTransferIntent.putExtra("mDeviceAddress", GlobalVariable.glassesAddress);
                    mTransferIntent.putExtra("connectionState", connectionState);
                    sendBroadcast(mTransferIntent);
                    glass_btn.setText("關閉");
                    ((LinearLayout)layout_glass_dev.getParent()).setBackgroundColor(Color.parseColor("#0047b2"));
                    for(int i=0; i < layout_glass_dev.getChildCount(); i++){
                        ((TextView)(layout_glass_dev.getChildAt(i))).setTextColor(Color.parseColor("#ffffff"));
                    }
                }
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tw.edu.ntust.jojllman.wearableapplication.RESPONSE_CONNECTED_DEVICES");
        registerReceiver(braceletReceiver, intentFilter);
    }
    private void createLanguageTTS()
    {
        if( tts == null )
        {
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
                @Override
                public void onInit(int arg0)
                {
                    // TTS 初始化成功
                    if( arg0 == TextToSpeech.SUCCESS )
                    {
                        // 指定的語系: 英文(美國)
                        Locale l = Locale.CHINESE;  // 不要用 Locale.ENGLISH, 會預設用英文(印度)

                        // 目前指定的【語系+國家】TTS, 已下載離線語音檔, 可以離線發音
                        if( tts.isLanguageAvailable( l ) == TextToSpeech.LANG_COUNTRY_AVAILABLE )
                        {
                            tts.setLanguage( l );
                        }
                    }
                }}
            );
        }
    }
    protected void onDestroy(){
        if(GlobalVariable.isServiceRunning(getApplicationContext(), "tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService")) {
            Intent intent = new Intent(this, BlunoService.class);
            stopService(intent);
        }
        super.onDestroy();
        unregisterReceiver(braceletReceiver);
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
        killRunnable = false;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(!killRunnable){
//                    Log.d(TAG, m_braceletState.name());
                    if(m_braceletState == BlunoService.BraceletState.distance) {
//                        ((TextView)layoutDistance.getChildAt(0)).setText(getString(R.string.btn_bracelet_distance) + "\n" + BlunoService.Bracelet_DT / 10 + "公分");
//                        layoutDistance.setContentDescription("物體距離" + BlunoService.Bracelet_DT / 10 + "公分。點擊關閉手環距離偵測。");
                        tts.speak((CharSequence)String.valueOf(BlunoService.Bracelet_DT / 10), TextToSpeech.QUEUE_ADD, null, null);
                    }else{
//                        layoutDistance.setContentDescription(getString(R.string.btn_bracelet_distance));
//                        ((TextView)layoutDistance.getChildAt(0)).setText(R.string.btn_bracelet_distance);
                    }
                    if(m_braceletState == BlunoService.BraceletState.color){
//                        ((TextView)layoutColor.getChildAt(0)).setText(getString(R.string.btn_bracelet_color) + "\n" + BlunoService.getColorName());
//                        layoutColor.setContentDescription(BlunoService.getColorName() + "。點擊關閉手環顏色辨識。");
                    }else{
//                        layoutColor.setContentDescription(getString(R.string.btn_bracelet_color));
//                        ((TextView)layoutColor.getChildAt(0)).setText(R.string.btn_bracelet_color);
                    }
                    if(m_braceletState == BlunoService.BraceletState.search){
//                        ((TextView)layoutSearch.getChildAt(0)).setText(getString(R.string.btn_bracelet_search) + "\n" + "尋找中");
//                        layoutSearch.setContentDescription(BlunoService.getColorName() + "。點擊關閉尋找手環。");
                    }else{
//                        layoutSearch.setContentDescription(getString(R.string.btn_bracelet_search));
//                        ((TextView)layoutSearch.getChildAt(0)).setText(R.string.btn_bracelet_search);
                    }
                }else{
                    mHandler.removeCallbacksAndMessages(this);
                }
                mHandler.postDelayed(this, 500);
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
                ((TextView)layout_glass_dev.getChildAt(1)).setText("裝置 " + BlunoService.getGlassName());        //get glass rssi
                ((TextView)layout_bracelet_dev.getChildAt(1)).setText("裝置 " + BlunoService.getBraceletName());     //get bracelet rssi
                ((TextView)layout_bracelet_dev.getChildAt(2)).setText("電量 " + BlunoService.getBraceletPower() + "%");
                layout_glass_dev.setContentDescription(getString(R.string.layout_glasses) + "未連線，" + ((TextView)layout_glass_dev.getChildAt(1)).getText());
                layout_bracelet_dev.setContentDescription(getString(R.string.layout_bracelet) + "未連線，" + ((TextView)layout_bracelet_dev.getChildAt(1)).getText());
                handler.postDelayed(this, 500); // set time here to refresh textView
            }
        });

        killAutoConnectRunnable = false;
        handler.post(autoConnectRunnable);

        sendBroadcast(mREQUEST_CONNECTED_DEVICES);
    }

    private void findView(){
        layout_glass_dev = (LinearLayout) findViewById(R.id.layout_glass_dev);
        layout_bracelet_dev = (LinearLayout) findViewById(R.id.layout_bracelet_dev);
        layout_glass_dev.setContentDescription(getString(R.string.layout_glasses) + "未連線，訊號 未知");
        layout_bracelet_dev.setContentDescription(getString(R.string.layout_bracelet) + "未連線，訊號 未知");
        Button ring = (Button) findViewById(R.id.ring_btn);
        ring.getCompoundDrawables()[0].setLevel(1);
        Button glass = (Button) findViewById(R.id.glass_btn);
        glass.getCompoundDrawables()[0].setLevel(1);
    }

    public void onPause(){
        scanLeDevice(false);
        handler.removeCallbacksAndMessages(null);
        if(BlunoService.getReadUltraSound()){
            BlunoService.setReadUltraSound(false);
        }
//        braceletDistanceIntent.putExtra("sendDistance",false);
//        sendBroadcast(braceletDistanceIntent);
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
            event.getText().add(VisualSupportActivity.this.getResources().getText(R.string.visual_main));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    private BroadcastReceiver braceletReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            m_braceletConnected = intent.getBooleanExtra("Connected_Bracelet", false);
        }
    };

    public void OnDeviceClick(View view) {
        sendBroadcast(mREQUEST_CONNECTED_DEVICES);
        if (view.getId() == R.id.layout_glass_dev) {
            // 增加眼鏡按下動作
            Log.d(TAG, "layout_glass_dev pressed");
            if (!BlunoService.getReadUltraSound()) {
                BlunoService.setReadUltraSound(true);
                view.announceForAccessibility("開啟眼鏡避障功能");
            } else {
                BlunoService.setReadUltraSound(false);
                view.announceForAccessibility("關閉眼鏡避障功能");
            }
        }else if (view.getId() == R.id.layout_bracelet_dev) {
            // 增加手環按下動作
            Log.d(TAG, "layout_bracelet_dev pressed");
            if (m_braceletConnected) {
                Intent intent = new Intent();
                intent.setClass(this, BraceletControlActivity.class);
                startActivity(intent);
            }
        }else if (view.getId() == R.id.layout_visual_search_dev){
            if(BlunoService.getBraceletPower() > 0)
                OnSearchClick(view);
        }else if (view.getId() == R.id.layout_setting){
            Intent intent = new Intent();
            intent.setClass(this  , VisualSettingActivity.class);
            startActivity(intent);
        }
    }
    public void OnDistanceClick(final View view){
//        view.setContentDescription("測試 測試 test test");
        Log.i(TAG,"OnDistanceClick");
        if(m_braceletState == BlunoService.BraceletState.none){
            braceletControlIntent.putExtra("BraceletDistance",true);
            sendBroadcast(braceletControlIntent);
            view.announceForAccessibility("開啟手環距離偵測");
        }else if(m_braceletState == BlunoService.BraceletState.distance){
            braceletControlIntent.putExtra("BraceletDistance",false);
            sendBroadcast(braceletControlIntent);
            view.announceForAccessibility("關閉手環距離偵測");
        }else if(m_braceletState == BlunoService.BraceletState.color){
            braceletControlIntent.putExtra("BraceletColor",false);
            braceletControlIntent.putExtra("BraceletDistance",true);
            sendBroadcast(braceletControlIntent);
            view.announceForAccessibility("關閉手環顏色辨識，開啟手環距離偵測");
        }
    }
    public void OnSearchClick(View view){
        Log.i(TAG,"OnSearchClick");
        braceletControlIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.BRACELET_SEND_CONTROL");
        braceletControlIntent.putExtra("BraceletSearch", true);
        sendBroadcast(braceletControlIntent);
        view.announceForAccessibility("尋找手環");
    }
    public void OnColorClick(final View view){
        Log.i(TAG,"OnColorClick");
        if(m_braceletState == BlunoService.BraceletState.none){
            braceletControlIntent.putExtra("BraceletColor",true);
            sendBroadcast(braceletControlIntent);
            view.announceForAccessibility("開啟手環顏色辨識");
        }else if(m_braceletState == BlunoService.BraceletState.color){
            braceletControlIntent.putExtra("BraceletColor",false);
            sendBroadcast(braceletControlIntent);
            view.announceForAccessibility("關閉手環顏色辨識");
        }else if(m_braceletState == BlunoService.BraceletState.distance){
            braceletControlIntent.putExtra("BraceletDistance",false);
            braceletControlIntent.putExtra("BraceletColor",true);
            sendBroadcast(braceletControlIntent);
            view.announceForAccessibility("關閉手環距離偵測，開啟手環顏色辨識");
        }
    }
//            new Thread(){
//                public void run(){
//                    super.run();
//                    if(m_braceletState == BlunoService.BraceletState.none){
//                        braceletDistanceIntent.putExtra("sendDistance",true);
//                        sendBroadcast(braceletDistanceIntent);
//                        view.announceForAccessibility("開啟手環距離偵測");
//                    }else if(m_braceletState == BlunoService.BraceletState.distance){
//                        braceletDistanceIntent.putExtra("sendDistance",false);
//                        sendBroadcast(braceletDistanceIntent);
//                        try {
//                            Thread.sleep(100);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        braceletDistanceIntent.putExtra("sendColor",true);
//                        sendBroadcast(braceletDistanceIntent);
//                        view.announceForAccessibility("開啟手環顏色辨識，關閉手環距離偵測");
//                    }else if(m_braceletState == BlunoService.BraceletState.color){
//                        braceletDistanceIntent.putExtra("sendColor",false);
//                        sendBroadcast(braceletDistanceIntent);
//                        view.announceForAccessibility("關閉手環顏色辨識");
//                    }
//                }
//            }.start();
//        }
//    }

//    public void OnHelpClick(View view){
//        DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//
//        final Dialog dialog = new Dialog(VisualSupportActivity.this,R.style.CustomDialog){
//            @Override
//            public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
//                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                    String contents=VisualSupportActivity.this.getResources().getText(R.string.help_dialog).toString()+"，";
//                    contents+=VisualSupportActivity.this.getResources().getText(R.string.help_message).toString();
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
//                    intent.setClass(VisualSupportActivity.this  , AppManageActivity.class);
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
