package tw.edu.ntust.jojllman.wearableapplication.BLE;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import tw.edu.ntust.jojllman.wearableapplication.GlobalVariable;
import tw.edu.ntust.jojllman.wearableapplication.VisualSupportActivity;

/**
 * This is a service class for manipulating data from bracelet and glass
 */

public class BlunoService extends Service {
//    public final static String SerialPortUUID="0000dfb1-0000-1000-8000-00805f9b34fb";
//    public final static String CommandUUID="0000dfb2-0000-1000-8000-00805f9b34fb";
//    public final static String ModelNumberStringUUID="00002a24-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_GLASS_SERVICE =
            UUID.fromString("0000AAAF-0000-1000-8000-00805F9B34FB");
    public final static UUID UUID_GLASS_CHARACTERISTIC =
            UUID.fromString("0000AAA1-0000-1000-8000-00805F9B34FB");
    public final static UUID UUID_GLASS_CHARACTERISTIC_IP =
            UUID.fromString("0000AAA2-0000-1000-8000-00805F9B34FB");
    public final static UUID UUID_BRACELET_NOTIFY =
            UUID.fromString(BraceletGattAttributes.NOTIFY);
    public final static UUID UUID_BRACELET_SERVICE =
            UUID.fromString(BraceletGattAttributes.SERVICE);
    public final static UUID UUID_BRACELET_WRITE =
            UUID.fromString(BraceletGattAttributes.WRITE);
    private final static int NUM_DEVICE = 2;
    private GlobalVariable mGlobalVariable;
    private static Handler handler = new Handler();
    private Intent transferIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_ACTIVITY");
    private Intent disonnectIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.DISCONNECTED_DEVICES");
    private Intent braceletStateIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.BRACELET_STATE");
    private Intent braceletControlIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.BRACELET_SEND_CONTROL");
    private Intent displayIPIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.DISPLAYIP");
    private Context serviceContext=this;
    private MsgReceiver msgReceiver;
    private ThresholdReceiver thresholdReceiver;
    private DeleteReceiver deleteReceiver;
    private int mBaudrate=115200;
    BluetoothLeService mBluetoothLeService;

    private static boolean mConnected_Glass = false;
    private static boolean mConnected_Bracelet = false;
    private static boolean mConnected_GloveLeft = false;
    private boolean mConnected_GloveRight = false;
    public static boolean firstdisplayIP = false;
    private BluetoothDevice mGlassDevice;
    private BluetoothDevice mBraceletDevice;
    private BluetoothDevice mGloveDeviceLeft, mGloveDeviceRight;

    public String connectionState;
    private enum theConnectionState{
        isToScan, isScanning, isConnecting, isConnected, isDisconnecting
    }

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGlassGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mBraceletGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGloveGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private BluetoothLeServiceListener mBluetoothLeServiceListener;
    private HashMap<String, BluetoothGattCharacteristic> mSendNotificationCharacteristics;
    private HashMap<String, BluetoothGattCharacteristic> mSendCommandCharacteristics;
    private HashMap<String, BluetoothGattCharacteristic> mSetNotificationCharacteristics;
    private BluetoothGattCharacteristic mUltraSoundCharacteristic;
    private BluetoothGattCharacteristic mUltraSoundCharacteristic_IP;
//    private static BluetoothGattCharacteristic mSCharacteristic, mModelNumberCharacteristic,
//                    mSerialPortCharacteristic, mCommandCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mWriteCharacteristic;

    private String mDeviceAddress;
    private String mPassword="AT+PASSWOR=DFRobot\r\n";
    private String mBaudrateBuffer = "AT+CURRUART="+mBaudrate+"\r\n";

//    public boolean mConnected = false;
    private final static String TAG = BlunoService.class.getSimpleName();

    static public int Bracelet_R, Bracelet_G, Bracelet_B, Bracelet_C, Bracelet_P;
    static public int Bracelet_DT;      //mm
    private static int Bracelet_RSSI;
    private static String BraceletName = "未連線";
    static public int Bracelet_BAT;
    static public int glass_battery;
    private BluetoothGatt BraceletGatt;
    public static int getBracelet_RSSI(){return Bracelet_RSSI;}
    public static String getBraceletName(){return BraceletName;}
    public static int distance_rssi;
    public static String BraceletPower;
    private static int[] data = new int[3];
    private static int index=0;
//    private static boolean bSendingBraceletDistance = false;
//    public static boolean getbSendingBraceletDistance(){return bSendingBraceletDistance;}
//    private boolean isFirstStop = false;
//    private Runnable mBraceletDistanceNotifyRunnable;
//    private Runnable mBraceletNotifyRunnable;

    private BraceletState m_braceletState = BraceletState.none;
    public enum BraceletState{
        none, distance, color, search
    }

    private final static int AVOID_ALLDIRECTION = 11;
    private final static int AVOID_FRONT = 12;
    private final static int AVOID_LEFT = 13;
    private final static int AVOID_RIGHT = 14;
    private final static int AVOID_LEFTANDRIGHT = 15;

    private final static int MOVE_LEFT = 21;
    private final static int MOVE_RIGHT = 22;

    //private static boolean readUltraSound = false;
    private static boolean readUltraSound = false;
    public static void setReadUltraSound(boolean b){readUltraSound = b;}
    public static boolean getReadUltraSound(){return readUltraSound;}
    private static Runnable readUltraSoundRunnable;
    public static int Glass_RSSI;
    private static String GlassName = "未連線";
    public static int getGlass_RSSI(){return Glass_RSSI;}
    public static String getGlassName(){return GlassName;}
    private int move_direction;
    private int pre_avoid_state;
    private int front  = 100;
    private int left       = 100;
    private int right      = 100;
    private int frontTemp = 100;
    private int leftTemp = 100;
    private int rightTemp = 100;
    private int frontThreshold;
    private int sidesThreshold;
    //private int postedNotificationCount = 0;
    private theConnectionState mConnectionState;
    protected enum warningState{
        left, right, front, twoSide,frontAndLeft, frontAndRight, allDirection, safe, others
    }
    private warningState mWarningState;
    private String mWarningText;
    private int mWarningCount = 0;
    private boolean turnOff = false;
    private boolean onNotification = false;
    private static final int mWarningCountThreshold = 40;
    private Uri soundUri;
    static boolean played = false;
    static boolean bracelet_disconnect_enable =false;
    static boolean glass_disconnect_enable =false;

    private long[] vibrate = {0, 500};
    //private doNotification notify;
    //private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

    private String URL ;
    DoRead_url doRead_url;
    public static Runnable readMjpegrunnable;


    private Thread			    mReadRssiThread;
    private boolean				mReadRssiThreadRunning;




    private Runnable mConnectingOverTimeRunnable=new Runnable(){

        @Override
        public void run() {
            if(connectionState=="isConnecting") {
                connectionState = "isToScan";
                transferIntent.putExtra("connectionState", connectionState);
                sendBroadcast(transferIntent);
            }
            mConnectionState = theConnectionState.valueOf(connectionState);
            onConectionStateChange(mConnectionState);
            mBluetoothLeService.close();
        }};

    private Runnable mDisonnectingOverTimeRunnable=new Runnable(){

        @Override
        public void run() {
            if(connectionState=="isDisconnecting") {
                transferIntent.putExtra("connectionState", connectionState);
                sendBroadcast(transferIntent);
            }
            mConnectionState = theConnectionState.valueOf(connectionState);
            onConectionStateChange(mConnectionState);
            mBluetoothLeService.close();
        }};

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("BlunoService mServiceConnection onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            //notify = new doNotification();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("mServiceConnection onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    public static TextToSpeechService mTTSService;

    private ServiceConnection TextToSpeechServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "TextToSpeechService unconnected");
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "TextToSpeechService connected");
            mTTSService = ((TextToSpeechService.LocalBinder)service).getService();
        }
    };

    private void WriteValue(BluetoothDevice dev, BluetoothGattCharacteristic characteristic, String strValue)
    {
        BluetoothGatt gatt = mBluetoothLeService.getGattFromDevice(dev);
        characteristic.setValue(strValue.getBytes());
        gatt.writeCharacteristic(characteristic);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i("BlunoService", "Create");
        onCreateProcess();
        serialBegin(115200);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BlunoService", "Start");
        onStartProcess();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("BlunoService", "Destroy");
        onDestroyProcess();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }

    public void onCreateProcess() {
        mGlobalVariable = (GlobalVariable)getApplicationContext();
        Intent gattServiceIntent = new Intent(serviceContext, BluetoothLeService.class);
        getApplicationContext().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        Log.d(TAG,"Try binding TextToSpeechService");
        Intent ttsIntent = new Intent(serviceContext, TextToSpeechService.class);
        boolean b = getApplicationContext().bindService(ttsIntent, TextToSpeechServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG,"TextToSpeechService bound = "+b);

        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_SERVICE");
        intentFilter.addAction("tw.edu.ntust.jojllman.wearableapplication.REQUEST_CONNECTED_DEVICES");
        registerReceiver(msgReceiver, intentFilter);

        thresholdReceiver = new ThresholdReceiver();
        IntentFilter thresholdIntentFilter = new IntentFilter();
        thresholdIntentFilter.addAction("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_THRESHOLD");
        registerReceiver(thresholdReceiver, thresholdIntentFilter);

        deleteReceiver = new DeleteReceiver();
        IntentFilter deleteIntentFilter = new IntentFilter();
        deleteIntentFilter.addAction("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_DELETE");
        registerReceiver(deleteReceiver, deleteIntentFilter);

        IntentFilter braceletControlIntentFilter = new IntentFilter();
        braceletControlIntentFilter.addAction(braceletControlIntent.getAction());
        registerReceiver(mBraceletControlReceiver, braceletControlIntentFilter);

        IntentFilter DisplayIPIntentFilter = new IntentFilter();
        DisplayIPIntentFilter.addAction(displayIPIntent.getAction());
        registerReceiver(displayipReceiver, DisplayIPIntentFilter);


        Log.d(TAG,"Start reading RSSI.");
        startReadingRssi();  //fuck you rssi noise

        gloveInit();

        frontThreshold = mGlobalVariable.getGlassFrontThreshold();
        sidesThreshold = mGlobalVariable.getGlassSideThreshold();

        System.out.println("BlunoService onCreate");
    }

    private void gloveInit()
    {
        GloveService gloveService = new GloveService(this);
        mBluetoothLeServiceListener = gloveService;

        mSendNotificationCharacteristics = new HashMap<>(NUM_DEVICE);
        mSendCommandCharacteristics = new HashMap<>(NUM_DEVICE);
        mSetNotificationCharacteristics = new HashMap<>(NUM_DEVICE);
    }

    public void onStartProcess() {
        System.out.println("BlunoService onStart");
        serviceContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    public void onDestroyProcess() {
        System.out.println("BlunoService onDestroy");
        mReadRssiThreadRunning = false;
        connectionState="isToScan";
        transferIntent.putExtra("connectionState", connectionState);
        sendBroadcast(transferIntent);
        mConnectionState = theConnectionState.valueOf(connectionState);
        onConectionStateChange(mConnectionState);

        unregisterReceiver(mGattUpdateReceiver);
        unregisterReceiver(msgReceiver);
        unregisterReceiver(thresholdReceiver);
        unregisterReceiver(deleteReceiver);
        unregisterReceiver(mBraceletControlReceiver);
        unregisterReceiver(displayipReceiver);

        if(mBluetoothLeService!=null)
        {
            mBluetoothLeService.disconnect();
            handler.removeCallbacks(mDisonnectingOverTimeRunnable);
            mBluetoothLeService.close();
        }
//        mSCharacteristic=null;
        try{
            getApplicationContext().unbindService(TextToSpeechServiceConnection);
            mTTSService = null;
            getApplicationContext().unbindService(mServiceConnection);
            mBluetoothLeService = null;
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra("DEVICE");
            System.out.println("mGattUpdateReceiver->onReceive->action=" + action);
            //System.out.println("mGattUpdateReceiver->onReceive->context=" + context.toString());
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                handler.removeCallbacks(mConnectingOverTimeRunnable);
                connectionState = "isToScan";
                transferIntent.putExtra("connectionState", connectionState);
                sendBroadcast(transferIntent);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connectionState = "isToScan";
                transferIntent.putExtra("connectionState", connectionState);
                sendBroadcast(transferIntent);
                mConnectionState = theConnectionState.valueOf(connectionState);
                onConectionStateChange(mConnectionState);
                handler.removeCallbacks(mDisonnectingOverTimeRunnable);
                sendBroadcast(disonnectIntent);
                mBluetoothLeServiceListener.onLeDeviceDisconnected(device);
                //mBluetoothLeService.close();
                if(device.equals(mGlassDevice)){
                    handler.removeCallbacks(readUltraSoundRunnable);
                    handler.removeCallbacks(readMjpegrunnable);
                    readUltraSoundRunnable=null;
                    readMjpegrunnable=null;
                    mTTSService.speak("眼鏡裝置已斷線。");
                    mGlassDevice=null;
                    glass_battery = -1;
                   GlobalVariable.glass_connect_state=false;
                    mConnected_Glass=false;
                   mBluetoothLeService.removeDeviceGatt(device);
                }
                if(device.equals(mBraceletDevice)){
//                    handler.removeCallbacks(mBraceletNotifyRunnable);
//                    handler.removeCallbacks(mBraceletDistanceNotifyRunnable);
                    mTTSService.speak("手環裝置已斷線。");
                    Bracelet_BAT = -1;
                    played = false;
                    mBraceletDevice=null;
                    mConnected_Bracelet=false;
                    mBluetoothLeService.removeDeviceGatt(device);
                }
                if(device.equals(mGloveDeviceLeft)){
                    mTTSService.speak("左手手套已斷線。");
                    mGloveDeviceLeft=null;
                }
                if(device.equals(mGloveDeviceRight)){
                    mTTSService.speak("右手手套已斷線。");
                    mGloveDeviceRight=null;
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices(device)) {
                    System.out.println("ACTION_GATT_SERVICES_DISCOVERED  "+
                            gattService.getUuid().toString());
                }
                getGattServices(device, mBluetoothLeService.getSupportedGattServices(device));
            }

            if(device.equals(mGlassDevice)) {
                Log.d(TAG, "Device is glass");
                if (BluetoothLeService.ACTION_ULTRASOUND_DATA.equals(action)) {
                    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    System.out.println("displayData " + intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }else if(BluetoothLeService.ACTION_GLASS_IP.equals(action)) {
                    Log.d(TAG,"Firstdisplay :"+firstdisplayIP +"\tURL connect:"+GlobalVariable.URL_state);
                    if(!firstdisplayIP || !GlobalVariable.URL_state){
                        displayIP(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                        firstdisplayIP = true;
                    }
//                    if(mSCharacteristic==mModelNumberCharacteristic)
//                    {
//                        if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA).toUpperCase().startsWith("DF BLUNO")) {
//                            mBluetoothLeService.setCharacteristicNotification(device, mSCharacteristic, false);
//                            mSCharacteristic=mCommandCharacteristic;
//                            mSCharacteristic.setValue(mPassword);
//                            mBluetoothLeService.writeCharacteristic(device, mSCharacteristic);
//                            mSCharacteristic.setValue(mBaudrateBuffer);
//                            mBluetoothLeService.writeCharacteristic(device, mSCharacteristic);
//                            mSCharacteristic=mSerialPortCharacteristic;
//                            mBluetoothLeService.setCharacteristicNotification(device, mSCharacteristic, true);
//                            connectionState = "isConnected";
//                            transferIntent.putExtra("connectionState", connectionState);
//                            sendBroadcast(transferIntent);
//                            mConnectionState = theConnectionState.valueOf(connectionState);
//                            onConectionStateChange(mConnectionState);
//                        }
//                        else {
//                            Toast.makeText(serviceContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
//                            connectionState = "isToScan";
//                            transferIntent.putExtra("connectionState", connectionState);
//                            sendBroadcast(transferIntent);
//                            mConnectionState = theConnectionState.valueOf(connectionState);
//                            onConectionStateChange(mConnectionState);
//                        }
//                    }
//                    else if (mSCharacteristic==mSerialPortCharacteristic) {
////                        onSerialReceived(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
//                        displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
//                    }
                }else if(BluetoothLeService.ON_READ_REMOTE_RSSI.equals(action)){
                    Glass_RSSI=intent.getIntExtra("RSSI",0);
                    GlassName = intent.getStringExtra("Name");
                }
            }
            else if(device.equals(mBraceletDevice)) {
                Log.d(TAG, "Device is bracelet");
                if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    Message msg;
                    final byte[] data =  intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String datastring =new String(data);
                    Log.d(TAG, datastring);
                    String PW=datastring;
                    int aastart = datastring.indexOf("aa");
                    int abstart = datastring.indexOf("ab");
                    int ajstart = datastring.indexOf("aj");
                    int aestart = datastring.indexOf("ae");
                    Log.d(TAG, "aastart=" + aastart + ", abstart=" + abstart);
//                    Log.d(TAG, "PW=" + PW);
                    Log.i(TAG, "State: " + m_braceletState);
//                    Log.i(TAG, "PW equals ad00001" + PW.startsWith("ad00001")); //距離
//                    Log.i(TAG, "PW equals ad00010" + PW.startsWith("ad00010")); //重置
//                    Log.i(TAG, "PW equals ad00100" + PW.startsWith("ad00100")); //距離
//                    Log.i(TAG, "PW equals ad01000" + PW.startsWith("ad01000")); //尋找手機

                    if(m_braceletState == BraceletState.none) {
                        if(PW.startsWith("ad00001")) {
                            braceletControlIntent.putExtra("BraceletDistance",true);
                            sendBroadcast(braceletControlIntent);
                            mTTSService.speak("開啟手環距離偵測");
                        }
                        else if(PW.startsWith("ad00010")) {

                        }
                        else if(PW.startsWith("ad00100")) {
                            braceletControlIntent.putExtra("BraceletColor",true);
                            sendBroadcast(braceletControlIntent);
                            mTTSService.speak("開啟手環顏色辨識");
                        }
                        else if(PW.startsWith("ad01000")) {
                            //TODO: speak out I'm here (search for phone)
                            mTTSService.speak("手機在這裡。");
                        }
                    }
                    else if(m_braceletState == BraceletState.distance) {
                        if(PW.startsWith("ad10000")) {
                            //TODO: speak out distance
                            mTTSService.speak("前方"+Bracelet_DT/10+"公分有障礙物");
                        }
                        else if(PW.startsWith("ad00010")) {
                            braceletControlIntent.putExtra("BraceletDistance",false);
                            sendBroadcast(braceletControlIntent);
                            mTTSService.speak("關閉手環距離偵測");
                        }
                        else if (data != null && data.length > 5) {
                            if(aastart!=-1){
                                final StringBuilder stringDT= new StringBuilder();
                                //	Log.w("dt", Integer.valueOf(stringDT.append(s, aastart+2, aastart+6).toString())+"");
                                Bracelet_DT = Integer.valueOf(stringDT.append(datastring, aastart+2, aastart+6).toString());
                                Log.d(TAG, "DT:" + Bracelet_DT + "mm");
                                if(Bracelet_DT < 200){
                                    String edtSend = "ag001001";
                                    WriteValue(mBraceletDevice,mWriteCharacteristic,edtSend);
                                }

                                //	DT=String.valueOf(dt);
                            }
                        }
                    }
                    else if(m_braceletState == BraceletState.color) {
                        if(PW.startsWith("ad10000")) {
                            //TODO: speak out color
                            mTTSService.speak(getColorName());
                        }
                        else if(PW.startsWith("ad00010")) {
                            braceletControlIntent.putExtra("BraceletColor",false);
                            sendBroadcast(braceletControlIntent);
                            mTTSService.speak("關閉手環顏色辨識");
                        }
                        else if (data != null && data.length > 10) {
                            if(abstart!=-1){
                                final StringBuilder stringR= new StringBuilder();
                                final StringBuilder stringG= new StringBuilder();
                                final StringBuilder stringB= new StringBuilder();
//                                Bracelet_R = Integer.valueOf(stringR.append(datastring, abstart+2, abstart+5).toString());
//                                Bracelet_G = Integer.valueOf(stringG.append(datastring, abstart+5, abstart+8).toString());
//                                Bracelet_B = +Integer.valueOf(stringB.append(datastring, abstart+8, abstart+11).toString());
//                                Log.d(TAG, "R:" + Bracelet_R + ", G:" + Bracelet_G + ", B:" + Bracelet_B);
                                //Log.e("test", stringR.append(s, abstart+2, abstart+4).toString());
                            }
                        }
                        else if (data != null && data.length == 7) {
                            if(ajstart != -1) {
                                final StringBuilder stringC= new StringBuilder();
                                final StringBuilder stringP= new StringBuilder();
                                final StringBuilder stringB= new StringBuilder();
                                Bracelet_C = Integer.valueOf(stringC.append(datastring, ajstart+2, ajstart+4).toString());
                                Bracelet_P = Integer.valueOf(stringP.append(datastring, ajstart+4, ajstart+5).toString());
                                Bracelet_B = +Integer.valueOf(stringB.append(datastring, ajstart+5, ajstart+6).toString());
                                mTTSService.speak(getColorName());
                            }
                        }
                    }else if(m_braceletState == BraceletState.search){
                        if(PW.startsWith("ad00010")) {
                            braceletControlIntent.putExtra("BraceletSearch",false);
                            sendBroadcast(braceletControlIntent);
                            mTTSService.speak("關閉尋找手環");
                        }
                    }
                    if(PW.startsWith("ae")){
                        if(aestart!=-1) {
                            final StringBuilder stringBAT = new StringBuilder();
                            Bracelet_BAT = Integer.valueOf(stringBAT.append(datastring.substring(2,5)).toString());
                            Log.d(TAG, "BAT:" + Bracelet_BAT);
                        }
                    }

                    braceletStateIntent.putExtra("BraceletState", m_braceletState.name());
                    sendBroadcast(braceletStateIntent);
                }else if(BluetoothLeService.ON_READ_REMOTE_RSSI.equals(action)){
                    BraceletName = intent.getStringExtra("Name");
                    Bracelet_RSSI=intent.getIntExtra("RSSI",0);
                    calculateAccuracy(Bracelet_RSSI+100);
                }
            }

            else if(device.equals(mGloveDeviceLeft) || device.equals(mGloveDeviceRight)) {
                Log.d(TAG, "Device is glove.");
                if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    gloveUpdate(device, intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
                }else if(BluetoothLeService.ON_READ_REMOTE_RSSI.equals(action)){
                    mBluetoothLeServiceListener.onRSSIRead(device, intent.getIntExtra("RSSI", 0));
                }
            }
        }
    };

    public static String getColorName(){
        if(Bracelet_P == 1){
            if(Bracelet_B == 0) {
                return "這是灰色";
            }
            if(Bracelet_B == 1) {
                return "這是白色";
            }
            if(Bracelet_B == 2) {
                return "這是黑色";
            }
        }
        else {
            String prefix = "";
            if(Bracelet_B == 0) {
                prefix = "";
            }
            if(Bracelet_B == 1) {
                prefix = "亮";
            }
            if(Bracelet_B == 2) {
                prefix = "暗";
            }
            if(Bracelet_C == 0) {
                return "這是" + prefix + "紅色";
            }
            if(Bracelet_C == 1) {
                return "這是" + prefix + "橙色";
            }
            if(Bracelet_C == 2) {
                return "這是" + prefix + "黃色";
            }
            if(Bracelet_C == 3) {
                return "這是" + prefix + "黃綠色";
            }
            if(Bracelet_C == 4) {
                return "這是" + prefix + "綠色";
            }
            if(Bracelet_C == 5) {
                return "這是" + prefix + "青綠色";
            }
            if(Bracelet_C == 6) {
                return "這是" + prefix + "青色";
            }
            if(Bracelet_C == 7) {
                return "這是" + prefix + "藍清色";
            }
            if(Bracelet_C == 8) {
                return "這是" + prefix + "藍色";
            }
            if(Bracelet_C == 9) {
                return "這是" + prefix + "藍紫色";
            }
            if(Bracelet_C == 10) {
                return "這是" + prefix + "紫色";
            }
            if(Bracelet_C == 11) {
                return "這是" + prefix + "洋紅色";
            }
        }
//        if(Bracelet_R>150 && Bracelet_G<80 && Bracelet_B<80)
//            return "這是紅色";
//        else if(Bracelet_G>Bracelet_R && (Bracelet_G-30)>Bracelet_B)
//            return "這是綠色";
//        else if(Bracelet_G>Bracelet_R && Bracelet_B>100)
//            return "這是藍色";
//        else if(Bracelet_B<140 && (Bracelet_R-Bracelet_G)<70&& Bracelet_B>5)  // && (Bracelet_R-20)>Bracelet_G
//            return "這是黃色";
////else if(Math.abs(Bracelet_R-Bracelet_B)<20 && (Bracelet_R+20)>Bracelet_G && Bracelet_G>50)
////	return "這是紫色";
//        else if(Bracelet_R>200 && Bracelet_G>80 && Bracelet_G<130 && Bracelet_B<70)
//            return "這是橘色";
//        else if(Bracelet_R>240 && Bracelet_G>240 && Bracelet_B>240)
//            return "這是白色";
//        else if(Bracelet_R<5 && Bracelet_G<5 && Bracelet_B<5)
//            return "這是黑色";
        return "未知的顏色";
    }

    private final BroadcastReceiver mBraceletControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(braceletControlIntent.getAction())) {
                final boolean braceletDistance =intent.getBooleanExtra("BraceletDistance",false);
                final boolean braceletColor =intent.getBooleanExtra("BraceletColor",false);
                final boolean braceletSearch = intent.getBooleanExtra("BraceletSearch",false);
                final boolean braceletDisconnect = intent.getBooleanExtra("BraceletDisconnect", false);
                final boolean glassDisconnect = intent.getBooleanExtra("GlassDisconnect",false);

                Log.i(TAG,"braceletDistance " + braceletDistance);
                Log.i(TAG,"braceletColor " + braceletColor);
                Log.i(TAG,"braceletSearch " + braceletSearch);
                new Thread(){
                    public void run(){
                        super.run();
                        try {
                            if(braceletDistance){
                                m_braceletState = BraceletState.distance;
                                String edtSend = "ag001001";

                                WriteValue(mBraceletDevice,mWriteCharacteristic,edtSend);
                            }else if(m_braceletState == BraceletState.distance){
                                m_braceletState = BraceletState.none;
                                Thread.sleep(100);
                                String edtSend = "aa0";
                                WriteValue(mBraceletDevice,mWriteCharacteristic,edtSend);
                                Thread.sleep(100);
                                String edtSend3 = "ac0";
                                WriteValue(mBraceletDevice,mWriteCharacteristic,edtSend3);
                                Thread.sleep(100);
                            }
                            if(braceletColor){
                                m_braceletState = BraceletState.color;
                                Thread.sleep(100);
                                String edtSend4 = "aw1";
                                WriteValue(mBraceletDevice, mWriteCharacteristic, edtSend4);
                                Thread.sleep(100);
                                String edtSend = "ab1";
                                WriteValue(mBraceletDevice,mWriteCharacteristic,edtSend);
                                Thread.sleep(100);
                            }else if(m_braceletState == BraceletState.color){
                                m_braceletState = BraceletState.none;
                                Thread.sleep(100);
                                String edtSend = "ab0";
                                WriteValue(mBraceletDevice,mWriteCharacteristic,edtSend);
                                Thread.sleep(100);
                            }
                            if(braceletSearch){
                                //find bracelet
                                m_braceletState = BraceletState.search;
                                Thread.sleep(100);
                                String edtSend = "ai1";
                                WriteValue(mBraceletDevice,mWriteCharacteristic,edtSend);
                                Thread.sleep(100);
//                                if(distance_rssi < 33){
//                                    edtSend = "ag001001";
//                                }else{
                                    edtSend = "ag001002";
//                                }
                                WriteValue(mBraceletDevice,mWriteCharacteristic,edtSend);
                                Thread.sleep(100);
                            }else if(m_braceletState == BraceletState.search){
                                m_braceletState = BraceletState.none;
                            }
                            if(braceletDisconnect){
                                if(getBraceletDevice()==null) {
                                    System.out.println("Disconnect error:Bracelet Device not set.");
                                }
                                else {
                                    bracelet_disconnect_enable =true;
                                    disConnect(getBraceletDevice());
                                }
                            }
                            if(glassDisconnect){
                                if(getGlassDevice()==null){
                                    System.out.println("Disconnect error: Glass Device not set.");
                                }
                                else{
                                    glass_disconnect_enable =true;
                                    disConnect(getGlassDevice());
                                }
                            }
                            braceletStateIntent.putExtra("BraceletState", m_braceletState.name());
                            sendBroadcast(braceletStateIntent);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }
    };

//    private void setBraceletFunction(boolean b){
//        if(!mConnected_Bracelet) {
//            Log.e(TAG,"Bracelet not found.");
//            return;
//        }
//        if(mWriteCharacteristic==null){
//            Log.e(TAG,"Bracelet mWriteCharacteristic not found.");
//            return;
//        }
//        if(b && !bSendingBraceletDistance) {
//            new Thread(){
//                @Override
//                public void run(){
//                    super.run();
//                    try {
//                        bSendingBraceletDistance = true;
//                        String edtSend4 = "aw1";
//                        WriteValue(mBraceletDevice, mWriteCharacteristic, edtSend4);
//                        Thread.sleep(100);
//                        String edtSend = "aa1";
//                        WriteValue(mBraceletDevice, mWriteCharacteristic, edtSend);
//                        String edtSend3 = "ac1";
//                        WriteValue(mBraceletDevice, mWriteCharacteristic, edtSend3);
//                    } catch (Exception e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }.start();
//        }else if(bSendingBraceletDistance){
//            new Thread(){
//                @Override
//                public void run(){
//                    super.run();
//                    try {
//                        bSendingBraceletDistance = false;
//                        Thread.sleep(100);
//                        String edtSend = "aa0";
//                        WriteValue(mBraceletDevice, mWriteCharacteristic, edtSend);
//                        Thread.sleep(100);
//                        String edtSend3 = "ac0";
//                        WriteValue(mBraceletDevice, mWriteCharacteristic, edtSend3);
//                    } catch (Exception e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }.start();
//        }
//        mBraceletNotifyRunnable = new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(200);
//                    setBraceletCharacteristicNotification(true);
//                    Thread.sleep(400);
//                    setBraceletCharacteristicNotification(true);
//                }
//                catch (InterruptedException e1)
//                {
//                    e1.printStackTrace();
//                }
//            }
//        };
//        handler.post(mBraceletNotifyRunnable);
//
//        mBraceletDistanceNotifyRunnable = new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if(bSendingBraceletDistance) {
//                        Thread.sleep(100);
//                        isFirstStop=true;
//                        String edtSend = "aa1";
//                        WriteValue(mBraceletDevice, mWriteCharacteristic,edtSend);
//                        String edtSend2 = "ac1";
//                        WriteValue(mBraceletDevice, mWriteCharacteristic,edtSend2);
//                    }else if(isFirstStop){
//                        isFirstStop=false;
//                        String edtSend = "aa0";
//                        WriteValue(mBraceletDevice, mWriteCharacteristic,edtSend);
//                        String edtSend2 = "ac0";
//                        WriteValue(mBraceletDevice, mWriteCharacteristic,edtSend2);
//                        Thread.sleep(500);
//                    }
//                }
//                catch (InterruptedException e1)
//                {
//                    String edtSend = "aa0";
//                    WriteValue(mBraceletDevice, mWriteCharacteristic,edtSend);
//                    String edtSend2 = "ac0";
//                    WriteValue(mBraceletDevice, mWriteCharacteristic,edtSend2);
//                    e1.printStackTrace();
//                }
//            }
//        };
//        handler.post(mBraceletDistanceNotifyRunnable);
//    }

    private boolean setBraceletCharacteristicNotification(boolean enabled){
        if(mBraceletDevice == null){
            Log.d(TAG, "mBraceletDevice is missing.");
            return false;
        }
        if(mNotifyCharacteristic == null){
            Log.d(TAG, "mNotifyCharacteristic is missing.");
            return false;
        }
        if(mWriteCharacteristic == null){
            Log.d(TAG, "mWriteCharacteristic is missing.");
            return false;
        }
        mBluetoothLeService.setCharacteristicNotification(mBraceletDevice,mNotifyCharacteristic,enabled);
        mBluetoothLeService.setCharacteristicNotification(mBraceletDevice,mWriteCharacteristic,enabled);
        return true;
    }

    private void getGattServices(BluetoothDevice device, List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
//        mModelNumberCharacteristic=null;
//        mSerialPortCharacteristic=null;
//        mCommandCharacteristic=null;

        short deviceType = -1; // 0:glass, 1:bracelet, 2:glove

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            System.out.println("displayGattServices + uuid="+uuid);

            if(uuid.equalsIgnoreCase(UUID_BRACELET_SERVICE.toString()))
            {
                Log.d(TAG, "GET BRACELET SERVICE.");
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
                {
                    Log.i(TAG, gattCharacteristic.getUuid().toString());
                    if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_BRACELET_NOTIFY.toString()))
                    {
                        Log.i(TAG, UUID_BRACELET_NOTIFY.toString());
                        mNotifyCharacteristic = gattCharacteristic;
                        //setCharacteristicNotification(gattCharacteristic, true);
                        mBluetoothLeService.setCharacteristicNotification(device, gattCharacteristic, true);
                        //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                        deviceType = 1;
                    }
                    if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_BRACELET_WRITE.toString()))
                    {
                        mWriteCharacteristic = gattCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(device, gattCharacteristic, true);
                        deviceType = 1;
                    }
                }
            }
            else if(uuid.equalsIgnoreCase(GloveGattAttributes.UUID_GLOVE_SERVICE.toString()))
            {
                Log.d(TAG, "GET GLOVE SERVICE.");
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    /***********Gloves***********/
                    // Add discovered characteristics to correspond map.
                    if (gattCharacteristic.getUuid().equals(GloveGattAttributes.UUID_SEND_NOTIFICATION)) {
                        mSendNotificationCharacteristics.put(device.getAddress(), gattCharacteristic);
                        deviceType = 2;
                    } else if (gattCharacteristic.getUuid().equals(GloveGattAttributes.UUID_SEND_COMMAND)) {
                        mSendCommandCharacteristics.put(device.getAddress(), gattCharacteristic);
                        deviceType = 2;
                    } else if (gattCharacteristic.getUuid().equals(GloveGattAttributes.UUID_SET_NOTIFICATION)) {
                        mSetNotificationCharacteristics.put(device.getAddress(), gattCharacteristic);
                        deviceType = 2;
                    }
                    boolean sts = (mSendCommandCharacteristics.get(device.getAddress()) != null) && (mSetNotificationCharacteristics.get(device.getAddress()) != null) && (mSendNotificationCharacteristics.get(device.getAddress()) != null);
                    //mBluetoothLeServiceListener.onLeServiceDiscovered(sts);
                }
            }
            else if(uuid.equalsIgnoreCase(UUID_GLASS_SERVICE.toString()))
            {
                Log.d(TAG, "GET GLASS SERVICE.");
                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    charas.add(gattCharacteristic);
                    if(gattCharacteristic.getUuid().equals(UUID_GLASS_CHARACTERISTIC)) {
                        mUltraSoundCharacteristic = gattCharacteristic;
                        deviceType = 0;
                        System.out.println("mUltraSoundCharacteristic  " + mUltraSoundCharacteristic.getUuid().toString());
                    }
                    if(gattCharacteristic.getUuid().equals(UUID_GLASS_CHARACTERISTIC_IP)) {
                        mUltraSoundCharacteristic_IP = gattCharacteristic;
                        System.out.println("mUltraSoundCharacteristic_IP  " + mUltraSoundCharacteristic_IP.getUuid().toString());
                    }
                    //uuid = gattCharacteristic.getUuid().toString();
//                    if (uuid.equals(ModelNumberStringUUID)) {
//                        mModelNumberCharacteristic = gattCharacteristic;
//                        deviceType = 0;
//                        System.out.println("mModelNumberCharacteristic  " + mModelNumberCharacteristic.getUuid().toString());
//                    } else if (uuid.equals(SerialPortUUID)) {
//                        mSerialPortCharacteristic = gattCharacteristic;
//                        deviceType = 0;
//                        System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristic.getUuid().toString());
////                    updateConnectionState(R.string.comm_establish);
//                    } else if (uuid.equals(CommandUUID)) {
//                        mCommandCharacteristic = gattCharacteristic;
//                        deviceType = 0;
//                        System.out.println("mCommandCharacteristic  " + mCommandCharacteristic.getUuid().toString());
////                    updateConnectionState(R.string.comm_establish);
//                    }
                }
            }
            // 0:glass, 1:bracelet, 2:glove
            switch (deviceType) {
                case 0:
                    mConnected_Glass = true;
                    mGlassDevice = device;
                    mGlassGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
                    mGlassGattCharacteristics.add(charas);
                    mBluetoothLeService.setGlassGatt(mBluetoothLeService.getGattFromDevice(device));
                    Glass_RSSI = 118;

                    if (mUltraSoundCharacteristic==null) {
                        Toast.makeText(serviceContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
                        connectionState = "isToScan";
                        transferIntent.putExtra("connectionState", connectionState);
                        sendBroadcast(transferIntent);
                        mConnectionState = theConnectionState.valueOf(connectionState);
                        onConectionStateChange(mConnectionState);
                    }else{
                        readUltraSound();
                        readIP();
                    }

//                    if (mModelNumberCharacteristic==null || mSerialPortCharacteristic==null || mCommandCharacteristic==null) {
//                        Toast.makeText(serviceContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
//                        connectionState = "isToScan";
//                        transferIntent.putExtra("connectionState", connectionState);
//                        sendBroadcast(transferIntent);
//                        mConnectionState = theConnectionState.valueOf(connectionState);
//                        onConectionStateChange(mConnectionState);
//                    }
//                    else {
//                        mSCharacteristic=mModelNumberCharacteristic;
//                        mBluetoothLeService.setCharacteristicNotification(device, mSCharacteristic, true);
//                        mBluetoothLeService.readCharacteristic(device, mSCharacteristic);
//                    }
                    mTTSService.speak("眼鏡裝置已連線。");
                    GlobalVariable.glass_connect_state=true;

                    Log.d(TAG, "Connected to glass device.");
                    VisualSupportActivity.glassConnected();
                    setReadUltraSound(true);

                    break;
                case 1:
                    mConnected_Bracelet = true;
                    mBraceletDevice = device;
                    mBraceletGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
                    mBraceletGattCharacteristics.add(charas);
                    mBluetoothLeService.setBraceletGatt(mBluetoothLeService.getGattFromDevice(device));
                    Bracelet_RSSI = 118;
                    new Thread(){
                        public void run(){
                            super.run();
                            try {
                                Thread.sleep(200);
                                String edtSend4 = "aw1";
                                WriteValue(mBraceletDevice, mWriteCharacteristic, edtSend4);
                                Thread.sleep(100);
                                WriteValue(mBraceletDevice, mWriteCharacteristic, "ae");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
//                    initBraceletRunnable();
                    if(!played){
                        mTTSService.speak("手環裝置已連線。");
                        played = true;
                    }
                    Log.d(TAG, "Connected to bracelet device.");
                    VisualSupportActivity.braceletConnect();
                    break;
                case 2:
                    if(!mConnected_GloveLeft && device.getName().toLowerCase().startsWith(GlobalVariable.defaultNameGlove.toLowerCase() + "l"))
                    {
                        mConnected_GloveLeft = true;
                        mGloveDeviceLeft = device;
                        mBluetoothLeServiceListener.onLeDeviceConnected(device,true);
                        mTTSService.speak("右手手套已連線。");
                        Log.d(TAG, "Connected to glove device left.");
                    }
                    else if(!mConnected_GloveRight && device.getName().toLowerCase().startsWith(GlobalVariable.defaultNameGlove.toLowerCase() + "r"))
                    {
                        mConnected_GloveRight = true;
                        mGloveDeviceRight = device;
                        mBluetoothLeServiceListener.onLeDeviceConnected(device,false);
                        mTTSService.speak("左手手套已連線。");
                        Log.d(TAG, "Connected to glove device right.");
                    }
                    if(mConnected_GloveLeft && mConnected_GloveRight){
                        startNotification(mGloveDeviceLeft);
                        startNotification(mGloveDeviceRight);
                        resetRemoteDevice(mGloveDeviceLeft);
                        resetRemoteDevice(mGloveDeviceRight);
                    }
                    mGloveGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
                    mGloveGattCharacteristics.add(charas);
                    mBluetoothLeService.setGloveGatt(mBluetoothLeService.getGattFromDevice(device));
                    break;
                default:
                    Log.d(TAG, "Connected to an unknown device.");
                    break;
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_ULTRASOUND_DATA);
        intentFilter.addAction(BluetoothLeService.ACTION_GLASS_IP);
        intentFilter.addAction(BluetoothLeService.ON_READ_REMOTE_RSSI);
        return intentFilter;
    }

    public void serialBegin(int baud){
        mBaudrate=baud;
        mBaudrateBuffer = "AT+CURRUART="+mBaudrate+"\r\n";
    }

    public void onConectionStateChange(theConnectionState mConnectionState){
        switch (mConnectionState) {											//Four connection state
            case isConnected:
                break;
            case isConnecting:
                break;
            case isToScan:
                break;
            case isScanning:
                break;
            case isDisconnecting:
                break;
            default:
                break;
        }
    }

    public void readUltraSound(){
        if (mBluetoothLeService == null || mUltraSoundCharacteristic == null || mGlassDevice == null) {
            Log.d(TAG, "Glass not initialized");
            return;
        }
        if(readUltraSoundRunnable == null) {
            readUltraSoundRunnable = new Runnable() {
                @Override
                public void run() {
                    if (readUltraSound)
                        mBluetoothLeService.readCharacteristic(mGlassDevice, mUltraSoundCharacteristic);
                    handler.postDelayed(this, 100); // set time here to refresh textView
                }
            };
            handler.post(readUltraSoundRunnable);
        }
    }
    private void readIP(){
        if (mBluetoothLeService == null || mUltraSoundCharacteristic_IP == null || mGlassDevice == null) {
            Log.d(TAG, "Glass not initialized");
            return;
        }
        mBluetoothLeService.readCharacteristic(mGlassDevice, mUltraSoundCharacteristic_IP);
    }

    private void displayData(String data) {
        if (data != null) {
            String tokens[] = data.split(",");
            Log.i(TAG, "Rec data:"+tokens[0]+","+tokens[1]+","+tokens[2]+","+tokens[3]);
            int front_distance=0;
            int left_distance=0;
            int right_distance =0;
            String BAT_tmp;
            try {
                front_distance = Integer.parseInt(tokens[0]);
                left_distance = Integer.parseInt(tokens[1]);
                right_distance = Integer.parseInt(tokens[2]);
                BAT_tmp = tokens[3].replace("%","");
                glass_battery = Integer.parseInt(BAT_tmp.trim());
                if(glass_battery==0)
                    glass_battery=1; //防止電量低 飄到0 產生開關無法運作之情形
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException");
            };
            Log.i(TAG, "front:"+front_distance+"\tleft:"+left_distance+"\tright"+right_distance+"\tpower:"+glass_battery);
            int avoid_state_now = 0;

            if(left_distance<sidesThreshold && right_distance<sidesThreshold && front_distance<frontThreshold) {
                avoid_state_now = AVOID_ALLDIRECTION;
            }else if(front_distance < frontThreshold) {
                avoid_state_now = AVOID_FRONT;
            }else if(right_distance < sidesThreshold && left_distance < sidesThreshold) {
                avoid_state_now = AVOID_LEFTANDRIGHT;
            }else if(left_distance<sidesThreshold) {
                avoid_state_now = AVOID_LEFT;
            }else if(right_distance<sidesThreshold) {
                avoid_state_now = AVOID_RIGHT;
            }

            if(!mTTSService.isSpeaking() || avoid_state_now != pre_avoid_state){
                Log.i(TAG, "Avoid notify");
                switch (avoid_state_now){
                    case AVOID_ALLDIRECTION:
                        mTTSService.speak("密集區域注意");
                        break;
                    case AVOID_FRONT:
                        if(left_distance > right_distance || move_direction == MOVE_LEFT) {
                            mTTSService.speak("前方注意 請向左避開");
                            move_direction = MOVE_LEFT;
                        }else {
                            mTTSService.speak("前方注意 請向右避開");
                            move_direction = MOVE_RIGHT;
                        }
                        break;
                    case AVOID_LEFTANDRIGHT:
                        mTTSService.speak("左右側注意");
                        break;
                    case AVOID_LEFT:
                        mTTSService.speak("左側注意");
                        break;
                    case AVOID_RIGHT:
                        mTTSService.speak("右側注意");
                        break;
                    default:
                        move_direction = 0;
                        break;
                }
            }

            pre_avoid_state = avoid_state_now;
        }
    }

//    public void onSerialReceived(String theString){
//        String[] buffer = theString.split(",");
//        Log.i("BlunoService", "onSerialRcecived");
//        try{
//            front  = Integer.parseInt(buffer[0]);
//            left   = Integer.parseInt(buffer[1]);
//            right  = Integer.parseInt(buffer[2]);
//            Log.i("BlunoService", "front:" + front + ", left:" + left + ", right:" + right);
//            stateProcess();
//            transferIntent.putExtra("front", front);
//            transferIntent.putExtra("left", left);
//            transferIntent.putExtra("right", right);
//            sendBroadcast(transferIntent);
//            Log.i("BlunoService", "Warning Count = " + mWarningCount);
//
//        }catch(Exception e){
//            Log.e("BlunoService", "[Error onSerialReceived]: "+e.toString());
//        }
//    }
//
//    public void stateProcess(){
//        if(turnOff) {
//            if(mWarningCount < mWarningCountThreshold) {
//                mWarningCount = mWarningCountThreshold;
//            }
//        }
//
//        if((left > sidesThreshold && right > sidesThreshold && front > frontThreshold) || mWarningCount >= mWarningCountThreshold){
//            mWarningState = warningState.safe;
//            //turnOff = true;
//            if(mWarningCount >= (mWarningCountThreshold+1) && (Math.abs(left-leftTemp) > 5 || Math.abs(right-rightTemp) > 5 ||
//                    Math.abs(front-frontTemp) > 5)){
//                mWarningCount = 0;
//                turnOff = false;
//            }
//            leftTemp = left;
//            rightTemp = right;
//            frontTemp = front;
//            mWarningCount+=1;
//        }
//        else if(left < sidesThreshold && right > sidesThreshold && front > frontThreshold){
//            if(mWarningState != warningState.left)
//                mWarningCount = 0;
//            mWarningState = warningState.left;
//            mWarningText = "左方危險, 注意";
//            mWarningCount += 1;
//            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_left);
//            //vibrate = new long[]{0, 100, 500, 100, 500};
//            if(!onNotification) {
//                doNotification notify = new doNotification();
//                notify.start();
//            }
//
//        }
//        else if(left > sidesThreshold && right < sidesThreshold && front > frontThreshold){
//            if(mWarningState != warningState.right)
//                mWarningCount = 0;
//            mWarningState = warningState.right;
//            mWarningText = "右方危險, 注意";
//            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_right);
//            //vibrate = new long[]{0, 500, 500, 100, 500};
//            mWarningCount += 1;
//            //postNotifications();
//            if(!onNotification) {
//                doNotification notify = new doNotification();
//                notify.start();
//            }
//        }
//        else if(left < sidesThreshold && right < sidesThreshold && front > frontThreshold){
//            if(mWarningState != warningState.twoSide)
//                mWarningCount = 0;
//            mWarningState = warningState.twoSide;
//            mWarningText = "兩側危險, 注意";
//            mWarningCount += 1;
//            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_twosides);
//            //vibrate = new long[]{0, 1000, 500, 100, 500};
//            //postNotifications();
//            if(!onNotification) {
//                doNotification notify = new doNotification();
//                notify.start();
//            }
//        }
//        else if(left > sidesThreshold && right > sidesThreshold && front < frontThreshold){
//            if(mWarningState != warningState.front)
//                mWarningCount = 0;
//            mWarningState = warningState.front;
//            mWarningText = "前方危險, 注意";
//            mWarningCount += 1;
//            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_front);
//            //vibrate = new long[]{0, 0, 500, 500, 500};
//            //postNotifications();
//            if(!onNotification) {
//                doNotification notify = new doNotification();
//                notify.start();
//            }
//        }
//
//        else if(left < sidesThreshold && right < sidesThreshold && front < frontThreshold){
//            if(mWarningState != warningState.allDirection)
//                mWarningCount = 0;
//            mWarningState = warningState.allDirection;
//            mWarningText = "密集區域, 注意";
//            mWarningCount += 1;
//            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning);
//            //vibrate = new long[]{0, 1000, 500, 500, 500};
//            //postNotifications();
//            if(!onNotification) {
//                doNotification notify = new doNotification();
//                notify.start();
//            }
//        }
//        else if(left < sidesThreshold && right > sidesThreshold && front < frontThreshold){
//            if(mWarningState != warningState.frontAndLeft)
//                mWarningCount = 0;
//            mWarningState = warningState.frontAndLeft;
//            mWarningText = "前方與左方危險, 注意";
//            mWarningCount += 1;
//            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_frontleft);
//            //vibrate = new long[]{0, 100, 500, 500, 500};
//            //postNotifications();
//            if(!onNotification) {
//                doNotification notify = new doNotification();
//                notify.start();
//            }
//        }
//        else if(left > sidesThreshold && right < sidesThreshold && front < frontThreshold){
//            if(mWarningState != warningState.frontAndRight)
//                mWarningCount = 0;
//            mWarningState = warningState.frontAndRight;
//            mWarningText = "前方與右方危險, 注意";
//            mWarningCount += 1;
//            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_frontright);
//            //vibrate = new long[]{0, 500, 500, 500, 500};
//            //postNotifications();
//            if(!onNotification) {
//                doNotification notify = new doNotification();
//                notify.start();
//            }
//        }
//        else{
//            if(mWarningState != warningState.others)
//                mWarningCount = 0;
//            mWarningState = warningState.others;
//            mWarningText = "注意";
//            mWarningCount += 1;
//            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning);
//            //vibrate = new long[]{0, 1000, 500, 500, 500};
//            //postNotifications();
//            if(!onNotification) {
//                doNotification notify = new doNotification();
//                notify.start();
//            }
//        }
//
//    }
    /*
    private void postNotifications(){
        sendBroadcast(new Intent(NotificationIntentReceiver.ACTION_ENABLE_MESSAGES)
                .setClass(this, NotificationIntentReceiver.class));

        NotificationPreset preset = NotificationPresets.BASIC;
        CharSequence titlePreset = "危險!";
        CharSequence textPreset =mWarningText;
        PriorityPreset priorityPreset = PriorityPresets.MAX;
        ActionsPreset actionsPreset = ActionsPresets.NO_ACTIONS_PRESET;

        NotificationPreset.BuildOptions options = new NotificationPreset.BuildOptions(
                titlePreset,
                textPreset,
                priorityPreset,
                actionsPreset,
                true,
                false,
                false,
                true
        );

        Notification[] notifications = preset.buildNotifications(this, options);

        // Post new notifications
        for (int i = 0; i < notifications.length; i++) {
            NotificationManagerCompat.from(this).notify(i, notifications[i]);
        }
        // Cancel any that are beyond the current count.
        for (int i = notifications.length; i < postedNotificationCount; i++) {
            NotificationManagerCompat.from(this).cancel(i);
        }
        postedNotificationCount = notifications.length;
    }
    */
//   public void myNotification(){
//       int notificationId = 001;
//
//       Intent openIntent = new Intent(this, VisualSupportActivity.class);
//       openIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//       PendingIntent openPendingIntent = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//       Intent deleteIntent = new Intent(this, DeleteService.class);
//       PendingIntent deletePendingIntent = PendingIntent.getService(this, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//
//       NotificationCompat.WearableExtender wearableExtender =
//               new NotificationCompat.WearableExtender()
//                       .setHintHideIcon(true)
//                       .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.warning));
//
//       NotificationCompat.Builder notificationBuilder =
//               new NotificationCompat.Builder(this)
//                       .setSmallIcon(R.mipmap.ic_launcher)
//                       .setContentTitle("危險！")
//                       .setContentText(mWarningText)
//                       .setContentIntent(openPendingIntent)
//                       .setDeleteIntent(deletePendingIntent)
//                       .addAction(R.drawable.ic_full_reply, "Turn Off", deletePendingIntent)
//                       .extend(wearableExtender)
//                       .setPriority(NotificationCompat.PRIORITY_HIGH)
//                       .setSound(soundUri)
//                       .setVibrate(vibrate);
//
//       // Get an instance of the NotificationManager service
//       NotificationManagerCompat notificationManager =
//               NotificationManagerCompat.from(this);
//
//       // Build the notification and issues it with notification manager.
//       notificationManager.notify(notificationId, notificationBuilder.build());
//
//       if(!mTTSService.isSpeaking()){
//           mTTSService.speak(mWarningText);
//       }
//   }
//
//    public class doNotification extends Thread {
//        @Override
//        public void run(){
//            onNotification = true;
//            myNotification();
//            try {
//                sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            onNotification = false;
//        }
//    }

    public class MsgReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction() == "tw.edu.ntust.jojllman.wearableapplication.REQUEST_CONNECTED_DEVICES") {
                Intent tempIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.RESPONSE_CONNECTED_DEVICES");
                tempIntent.putExtra("Connected_Glass", mConnected_Glass);
                tempIntent.putExtra("Connected_Bracelet", mConnected_Bracelet);
                tempIntent.putExtra("Connected_Glove_Left", mConnected_GloveLeft);
                tempIntent.putExtra("Connected_Glove_Right", mConnected_GloveRight);
                sendBroadcast(tempIntent);
                return;
            }

            mDeviceAddress = intent.getStringExtra("mDeviceAddress");
            connectionState = intent.getStringExtra("connectionState");
            Log.d(TAG, "mDeviceAddress "+mDeviceAddress);

            if (mBluetoothLeService.connect(mDeviceAddress)) {
                Log.d(TAG, "Connect request success");
                mConnectionState = theConnectionState.valueOf(connectionState);
                onConectionStateChange(mConnectionState);
                handler.postDelayed(mConnectingOverTimeRunnable, 10000);
            } else {
                Log.d(TAG, "Connect request fail");
                connectionState = "isToScan";
                transferIntent.putExtra("connectionState", connectionState);
                sendBroadcast(transferIntent);
                mConnectionState = theConnectionState.valueOf(connectionState);
                onConectionStateChange(mConnectionState);
            }
        }
    }

    public class ThresholdReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            frontThreshold = intent.getIntExtra("frontThreshold", frontThreshold);
            sidesThreshold = intent.getIntExtra("sidesThreshold", sidesThreshold);
        }
    }

    private final BroadcastReceiver displayipReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
                final boolean Displayipflag = intent.getBooleanExtra("DisplayIP", false);
                System.out.println("Displayflag:"+Displayipflag);
            if(Displayipflag && firstdisplayIP){
                //displayIP(mGlobalVariable.glassesIPAddress);
            }


        }
    };


    public class DeleteReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            turnOff = intent.getBooleanExtra("turnOff", turnOff);
        }
    }

    public BluetoothDevice getGlassDevice() {
        return mGlassDevice;
    }

    public BluetoothDevice getBraceletDevice() {
        return mBraceletDevice;
    }

    public BluetoothDevice getGloveDevice(boolean isLeft) {
        if(isLeft)return mGloveDeviceLeft;
        return mGloveDeviceRight;
    }

    public void removeDevice(BluetoothDevice device) {
        mBluetoothLeService.removeDeviceGatt(device);

        if(device == mGlassDevice) {
            mGlassDevice = null;
            mConnected_Glass = false;
            mBluetoothLeService.setGlassGatt(null);
        }
        else if(device == mGloveDeviceLeft) {
            mGloveDeviceLeft = null;
            mConnected_GloveLeft = false;
            mBluetoothLeService.setGloveGatt(null);
        }
        else if(device == mGloveDeviceRight) {
            mGloveDeviceRight = null;
            mConnected_GloveRight = false;
            mBluetoothLeService.setGloveGatt(null);
        }
        else if(device == mBraceletDevice) {
            mBraceletDevice = null;
            mConnected_Bracelet = false;
            mBluetoothLeService.setBraceletGatt(null);
        }

        disonnectIntent.putExtra("Connected_Glass", mConnected_Glass);
        disonnectIntent.putExtra("Connected_Bracelet", mConnected_Bracelet);
        disonnectIntent.putExtra("Connected_GloveLeft", mConnected_GloveLeft);
        disonnectIntent.putExtra("Connected_GloveRight", mConnected_GloveRight);
        sendBroadcast(disonnectIntent);
    }

//    public void swapGloves()
//    {
//        BluetoothDevice tempDev = mGloveDeviceLeft;
//        mGloveDeviceLeft = mGloveDeviceRight;
//        mGloveDeviceRight = tempDev;
//        if(mConnected_GloveLeft != mConnected_GloveRight)
//        {
//            boolean tempB = mConnected_GloveLeft;
//            mConnected_GloveLeft = mConnected_GloveRight;
//            mConnected_GloveRight = tempB;
//        }
//    }

    private void gloveUpdate (final BluetoothDevice device, final byte[] data)
    {
        // Get package identifier
        byte ident = (byte)(data[0] & 0x0F);

        // Get package index
        byte index = (byte)((data[0] >> 4) & 0x0F);

        if (ident == GloveGattAttributes.IDENTIFIER_QUATERNION)
        {
            float[] q = new float[4]; // w,x,y,z
            q[0] = ByteBuffer.wrap(data, 1, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            q[1] = ByteBuffer.wrap(data, 5, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            q[2] = ByteBuffer.wrap(data, 9, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            q[3] = ByteBuffer.wrap(data, 13, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();

            //Log.d(TAG, device.getAddress() + " " + q[0] + " " + q[1] + " " + q[2] + " " + q[3]);

            mBluetoothLeServiceListener.onQuaternionChanged(device, q);
        }
        else if (ident == GloveGattAttributes.IDENTIFIER_GESTURE_N_ACCELERATION)
        {
            float[] flex = new float[10];
            //float[] touch = new float[10];
            float[] acce = new float[3];

            if (device.equals(mGloveDeviceLeft))
            {
                for (int i = 0; i < 10; i++)
                {
                    flex[i] = (float)((data[i/2+1]>>(4*((i^0x01)&0x01))) & 0x0F) * 6.0f;
                    //touch[i] = (float)((data[i/2+6]>>(4*((i^0x01)&0x01))) & 0x0F);
                }
            }
            else if (device.equals(mGloveDeviceRight))
            {
                for (int i = 0; i < 10; i++)
                {
                    flex[9-i] = (float)((data[i/2+1]>>(4*((i^0x01)&0x01))) & 0x0F) * 6.0f;
                    //touch[9-i] = (float)((data[i/2+6]>>(4*((i^0x01)&0x01))) & 0x0F);
                }
            }

            acce[0] = (ByteBuffer.wrap(data, 11, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() >> 4) * 2.0f / 1000.0f;
            acce[1] = (ByteBuffer.wrap(data, 13, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() >> 4) * 2.0f / 1000.0f;
            acce[2] = (ByteBuffer.wrap(data, 15, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() >> 4) * 2.0f / 1000.0f;

            mBluetoothLeServiceListener.onGestureChanged(device, flex);
            //mBluetoothLeServiceListener.onTouchChanged(device, touch);
            mBluetoothLeServiceListener.onAccelerationChanged(device, acce);
        }
    }

    public boolean startNotification (BluetoothDevice device)
    {
        String address = device.getAddress();
        BluetoothGatt gatt = mBluetoothLeService.mBluetoothGatts.get(address);
        BluetoothGattCharacteristic sendGattCharacteristic = mSendNotificationCharacteristics.get(address);
        BluetoothGattCharacteristic setGattCharacteristic = mSetNotificationCharacteristics.get(address);

        Log.d(TAG, "starting notification : " + gatt.getDevice().getName() + " " + address);

        boolean sts;
        byte[] value = new byte[1];
        value[0] = GloveGattAttributes.NOTIFICATION_START;
        sendGattCharacteristic.setValue(value);
        sts = gatt.writeCharacteristic(sendGattCharacteristic);
        Log.d(TAG, "startNotification (Write): " + sts);

        if (sts)
        {
            sts = gatt.setCharacteristicNotification(setGattCharacteristic, true);
            Log.d(TAG, "startNotification (Set): " + sts);

            return sts;
        }

        return false;
    }

    public boolean stopNotification (BluetoothDevice device)
    {
        String address = device.getAddress();
        BluetoothGatt gatt = mBluetoothLeService.mBluetoothGatts.get(address);
        BluetoothGattCharacteristic sendGattCharacteristic = mSendNotificationCharacteristics.get(address);
        BluetoothGattCharacteristic setGattCharacteristic = mSetNotificationCharacteristics.get(address);

        boolean sts;
        byte[] value = new byte[1];
        value[0] = GloveGattAttributes.NOTIFICATION_STOP;
        sendGattCharacteristic.setValue(value);
        sts = gatt.writeCharacteristic(sendGattCharacteristic);
        Log.d(TAG, "stopNotification (Write): " + sts);

        if (sts)
        {
            sts = gatt.setCharacteristicNotification(setGattCharacteristic, false);
            Log.d(TAG, "stopNotification (Set): " + sts);

            return sts;
        }

        return false;
    }

    public boolean resetRemoteDevice (BluetoothDevice device)
    {
        String address = device.getAddress();
        BluetoothGatt gatt = mBluetoothLeService.mBluetoothGatts.get(address);
        BluetoothGattCharacteristic sendGattCharacteristic = mSendCommandCharacteristics.get(address);

        boolean sts;
        byte[] value = new byte[1];
        value[0] = GloveGattAttributes.COMMAND_RESET;
        sendGattCharacteristic.setValue(value);
        sts = gatt.writeCharacteristic(sendGattCharacteristic);

        return sts;
    }

    private void startReadingRssi()
    {
        mReadRssiThreadRunning = true;
        mReadRssiThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (mReadRssiThreadRunning)
                {
                    if ((mConnected_GloveLeft && mConnected_GloveRight) ||
                            mConnected_Glass || mConnected_Bracelet)
                    {
                        try
                        {
                            mBluetoothLeService.readRemoteRssi();
                            Thread.sleep(2000);
                            mBluetoothLeService.readRemoteRssi();
                            Thread.sleep(2000);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        mReadRssiThread.start();
    }

    private static void calculateAccuracy(int rssi) {
        data[index] = rssi;
        //Log.i("", data[0] + "" + data[1] + "" + data[2]);
        if (index == 0) {
            if (data[0] <= (data[1] + 3) && data[0] >= (data[1] - 3) && data[0] <= (data[2] + 3) && data[0] >= (data[2] - 3))
                distance_rssi = data[0];
            index = 1;
        } else if (index == 1) {
            if (data[1] <= (data[2] + 3) && data[1] >= (data[2] - 3) && data[1] <= (data[0] + 3) && data[1] >= (data[0] - 3))
                distance_rssi = data[1];
            index = 2;
        } else if (index == 2) {
            if (data[2] <= (data[0] + 3) && data[2] >= (data[0] - 3) && data[2] <= (data[1] + 3) && data[2] >= (data[1] - 3))
                distance_rssi = data[1];
            index = 0;
        }
    }
    public static int getBraceletPower() {
        return Bracelet_BAT;
    }
    public static int getGlassbattery() {return  glass_battery;}
    public static boolean getConnected_Bracelet() { return mConnected_Bracelet;}
    public static boolean getmConnected_Glass() { return mConnected_Glass;}

    public void disConnect(BluetoothDevice device){
        if(mConnected_Bracelet||mConnected_Glass){
            if (bracelet_disconnect_enable) {
                mBluetoothLeService.disconnect(mBluetoothLeService.getGattFromDevice(device));
                mConnected_Bracelet = false;
//            mBraceletDevice = null;
//            mBraceletGattCharacteristics.clear();
//            mBluetoothLeService.setBraceletGatt(null);
                bracelet_disconnect_enable =false;
            }
            if(glass_disconnect_enable){
                mBluetoothLeService.disconnect(mBluetoothLeService.getGattFromDevice(device));
                mConnected_Glass= false;
                bracelet_disconnect_enable =false;
            }
        }

    }
    public class DoRead_url extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            URL httpURL ;
            try {
                Log.d(TAG, "1. Sending http request");
                httpURL = new URL(url[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) httpURL.openConnection();
                Log.d(TAG, "2. Request finished, status = " + urlConnection.getResponseMessage());
                return new MjpegInputStream(new BufferedInputStream(urlConnection.getInputStream()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-ClientProtocolException", e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-IOException", e);
            }

            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mGlobalVariable.mv.setSource(result);
            mGlobalVariable.mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
        }
    }
    private void displayIP(String ip_data){
        Log.d(TAG,"displayIP ip_data="+ip_data);
        mGlobalVariable.glassesIPAddress=ip_data;
          if (mGlobalVariable.glassesIPAddress != null) {
//            if (readMjpegrunnable == null) {
//                readMjpegrunnable = new Runnable() {
//                    @Override
//                    public void run() {
                        mGlobalVariable.mv.setState(MjpegView.STATE_NORMAL);
                        SharedPreferences settings = getSharedPreferences("Preference", 0);

                        if(GlobalVariable.tag_btn_enable) {
                            mGlobalVariable.mv.setState(MjpegView.STATE_QRTAGDETECT);
                        }else{
                            mGlobalVariable.mv.setState(MjpegView.STATE_BLANK);
                        }
                        //mv.setState(MjpegView.STATE_NORMAL);
                        //String IP = settings.getString("IP", "192.168.1.25:9000");
                        URL = "http://" + mGlobalVariable.glassesIPAddress + ":9000/?action=stream";
                        Log.d(TAG, "URL =" + URL);
                        doRead_url = new DoRead_url();
                        doRead_url.execute(URL);

                    }
//                };
//                handler.post(readMjpegrunnable);
//            }else{
//                mGlobalVariable.mv.setState(MjpegView.STATE_QRTAGDETECT);
//            }
//        }
    }
    public static void initName(){
        BraceletName="未連線";
        GlassName="未連線";
    }
    public static void speak(String s){
        mTTSService.speak(s);
    }
    public static void initReadMjpegrunnable(){
        handler.removeCallbacks(readMjpegrunnable);
        readMjpegrunnable=null;
    }
}

