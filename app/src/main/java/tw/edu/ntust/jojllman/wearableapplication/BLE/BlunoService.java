package tw.edu.ntust.jojllman.wearableapplication.BLE;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
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
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import tw.edu.ntust.jojllman.wearableapplication.R;
import tw.edu.ntust.jojllman.wearableapplication.VisualSupportActivity;

/**
 * This is a service class for manipulating data from bracelet and glass
 */

public class BlunoService extends Service {
    public final static String SerialPortUUID="0000dfb1-0000-1000-8000-00805f9b34fb";
    public final static String CommandUUID="0000dfb2-0000-1000-8000-00805f9b34fb";
    public final static String ModelNumberStringUUID="00002a24-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_BRACELET_NOTIFY =
            UUID.fromString(BraceletGattAttributes.NOTIFY);
    public final static UUID UUID_BRACELET_SERVICE =
            UUID.fromString(BraceletGattAttributes.SERVICE);
    private final static int NUM_DEVICE = 2;
    private Handler handler = new Handler();
    private Intent transferIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.RECEIVER_ACTIVITY");
    private Intent disonnectIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.DISCONNECTED_DEVICES");
    private Intent braceletIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.BRACELET_STATE");
    private Context serviceContext=this;
    private MsgReceiver msgReceiver;
    private ThresholdReceiver thresholdReceiver;
    private DeleteReceiver deleteReceiver;
    private int mBaudrate=115200;
    BluetoothLeService mBluetoothLeService;

    private boolean mConnected_Glass = false;
    private boolean mConnected_Bracelet = false;
    private boolean mConnected_GloveLeft = false;
    private boolean mConnected_GloveRight = false;
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
    private static BluetoothGattCharacteristic mSCharacteristic, mModelNumberCharacteristic,
                    mSerialPortCharacteristic, mCommandCharacteristic;
    private static BluetoothGattCharacteristic mNotifyCharacteristic;

    private String mDeviceAddress;
    private String mPassword="AT+PASSWOR=DFRobot\r\n";
    private String mBaudrateBuffer = "AT+CURRUART="+mBaudrate+"\r\n";

    public boolean mConnected = false;
    private final static String TAG = BlunoService.class.getSimpleName();

    static public int Bracelet_R, Bracelet_G, Bracelet_B;
    static public String Bracelet_DT;
    private BraceletState m_braceletState = BraceletState.none;
    public enum BraceletState{
        none, distance, color
    }

    private int front  = 100;
    private int left       = 100;
    private int right      = 100;
    private int frontTemp = 100;
    private int leftTemp = 100;
    private int rightTemp = 100;
    private int frontThreshold = 50;
    private int sidesThreshold = 50;
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
    private long[] vibrate = {0, 500};
    //private doNotification notify;
    //private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

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
            System.out.println("mServiceConnection onServiceConnected");
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

    private RecognitionServiceListener mRecognitionServiceListener;

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
        super.onDestroy();
        Log.i("BlunoService", "Destroy");
        onDestroyProcess();
    }

    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }

    public void onCreateProcess() {
        Intent gattServiceIntent = new Intent(serviceContext, BluetoothLeService.class);
        serviceContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

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

        gloveInit();

        System.out.println("BlunoService onCreate");
    }

    private void gloveInit()
    {
        GloveService gloveService = new GloveService(this);
        mBluetoothLeServiceListener = gloveService;

        Log.d(TAG,"Start reading RSSI.");
        startReadingRssi();

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
        serviceContext.unregisterReceiver(mGattUpdateReceiver);
        connectionState="isToScan";
        transferIntent.putExtra("connectionState", connectionState);
        sendBroadcast(transferIntent);
        mConnectionState = theConnectionState.valueOf(connectionState);
        onConectionStateChange(mConnectionState);
        unregisterReceiver(msgReceiver);
        if(mBluetoothLeService!=null)
        {
            mBluetoothLeService.disconnect();
            handler.removeCallbacks(mDisonnectingOverTimeRunnable);
            mBluetoothLeService.close();
        }
        mSCharacteristic=null;
        serviceContext.unbindService(mServiceConnection);
        mBluetoothLeService = null;
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
                if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    if(mSCharacteristic==mModelNumberCharacteristic)
                    {
                        if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA).toUpperCase().startsWith("DF BLUNO")) {
                            mBluetoothLeService.setCharacteristicNotification(device, mSCharacteristic, false);
                            mSCharacteristic=mCommandCharacteristic;
                            mSCharacteristic.setValue(mPassword);
                            mBluetoothLeService.writeCharacteristic(device, mSCharacteristic);
                            mSCharacteristic.setValue(mBaudrateBuffer);
                            mBluetoothLeService.writeCharacteristic(device, mSCharacteristic);
                            mSCharacteristic=mSerialPortCharacteristic;
                            mBluetoothLeService.setCharacteristicNotification(device, mSCharacteristic, true);
                            connectionState = "isConnected";
                            transferIntent.putExtra("connectionState", connectionState);
                            sendBroadcast(transferIntent);
                            mConnectionState = theConnectionState.valueOf(connectionState);
                            onConectionStateChange(mConnectionState);
                        }
                        else {
                            Toast.makeText(serviceContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
                            connectionState = "isToScan";
                            transferIntent.putExtra("connectionState", connectionState);
                            sendBroadcast(transferIntent);
                            mConnectionState = theConnectionState.valueOf(connectionState);
                            onConectionStateChange(mConnectionState);
                        }
                    }
                    else if (mSCharacteristic==mSerialPortCharacteristic) {
                        onSerialReceived(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    }
                    System.out.println("displayData " + intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
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
                    Log.d(TAG, "aastart=" + aastart + ", abstart=" + abstart);
                    Log.d(TAG, "PW=" + PW);
                    Log.i(TAG, "State: " + m_braceletState);
                    Log.i(TAG, "PW equals ad00001" + PW.startsWith("ad00001")); //距離
                    Log.i(TAG, "PW equals ad00010" + PW.startsWith("ad00010")); //重置
                    Log.i(TAG, "PW equals ad00100" + PW.startsWith("ad00100")); //距離
                    Log.i(TAG, "PW equals ad01000" + PW.startsWith("ad01000")); //尋找手機

                    if(m_braceletState == BraceletState.none) {
                        if(PW.startsWith("ad00001")) {
                            m_braceletState = BraceletState.distance;
                            BluetoothGatt gatt = mBluetoothLeService.getGattFromDevice(device);
                            String edtSend = "aa1";
                            mNotifyCharacteristic.setValue(edtSend);
                            gatt.writeCharacteristic(mNotifyCharacteristic);
                            String edtSend3 = "ac1";
                            mNotifyCharacteristic.setValue(edtSend3);
                            gatt.writeCharacteristic(mNotifyCharacteristic);
                        }
                        else if(PW.startsWith("ad00010")) {

                        }
                        else if(PW.startsWith("ad00100")) {
                            m_braceletState = BraceletState.color;
                            BluetoothGatt gatt = mBluetoothLeService.getGattFromDevice(device);
                            String edtSend = "ab1";
                            mNotifyCharacteristic.setValue(edtSend);
                            gatt.writeCharacteristic(mNotifyCharacteristic);
                        }
                        else if(PW.startsWith("ad01000")) {
                            //TODO: speak out I'm here
                        }
                    }
                    else if(m_braceletState == BraceletState.distance) {
                        if(PW.startsWith("ad10000")) {
                            //TODO: speak out distance
                        }
                        else if(PW.startsWith("ad00010")) {
                            m_braceletState = BraceletState.none;
                            BluetoothGatt gatt = mBluetoothLeService.getGattFromDevice(device);
                            String edtSend = "aa0";
                            mNotifyCharacteristic.setValue(edtSend);
                            gatt.writeCharacteristic(mNotifyCharacteristic);
                            String edtSend3 = "ac0";
                            mNotifyCharacteristic.setValue(edtSend3);
                            gatt.writeCharacteristic(mNotifyCharacteristic);
                        }
                        else if (data != null && data.length > 0) {
                            if(aastart!=-1){
                                final StringBuilder stringDT= new StringBuilder();
                                //	Log.w("dt", Integer.valueOf(stringDT.append(s, aastart+2, aastart+6).toString())+"");
                                Bracelet_DT = Integer.valueOf(stringDT.append(datastring, aastart+2, aastart+6).toString())+"mm";
                                Log.d(TAG, "DT:" + Bracelet_DT);
                                //	DT=String.valueOf(dt);
                            }
                        }
                    }
                    else if(m_braceletState == BraceletState.color) {
                        if(PW.startsWith("ad10000")) {
                            //TODO: speak out color
                        }
                        else if(PW.startsWith("ad00010")) {
                            m_braceletState = BraceletState.none;
                            BluetoothGatt gatt = mBluetoothLeService.getGattFromDevice(device);
                            String edtSend = "ab0";
                            mNotifyCharacteristic.setValue(edtSend);
                            gatt.writeCharacteristic(mNotifyCharacteristic);
                        }
                        else if (data != null && data.length > 0) {
                            if(abstart!=-1){
                                final StringBuilder stringR= new StringBuilder();
                                final StringBuilder stringG= new StringBuilder();
                                final StringBuilder stringB= new StringBuilder();
                                Bracelet_R = Integer.valueOf(stringR.append(datastring, abstart+2, abstart+5).toString());
                                Bracelet_G = Integer.valueOf(stringG.append(datastring, abstart+5, abstart+8).toString());
                                Bracelet_B = +Integer.valueOf(stringB.append(datastring, abstart+8, abstart+11).toString());
                                Log.d(TAG, "R:" + Bracelet_R + ", G:" + Bracelet_G + ", B:" + Bracelet_B);
                                //Log.e("test", stringR.append(s, abstart+2, abstart+4).toString());
                            }
                        }
                    }

                    braceletIntent.putExtra("BraceletState", m_braceletState + "");
                    sendBroadcast(braceletIntent);
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

    private void getGattServices(BluetoothDevice device, List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        mModelNumberCharacteristic=null;
        mSerialPortCharacteristic=null;
        mCommandCharacteristic=null;

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
                Log.i(TAG, "Count is:" + gattCharacteristics.size());
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
                        break;
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
            else
            {
                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    charas.add(gattCharacteristic);
                    //uuid = gattCharacteristic.getUuid().toString();
                    if (uuid.equals(ModelNumberStringUUID)) {
                        mModelNumberCharacteristic = gattCharacteristic;
                        deviceType = 0;
                        System.out.println("mModelNumberCharacteristic  " + mModelNumberCharacteristic.getUuid().toString());
                    } else if (uuid.equals(SerialPortUUID)) {
                        mSerialPortCharacteristic = gattCharacteristic;
                        deviceType = 0;
                        System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                    } else if (uuid.equals(CommandUUID)) {
                        mCommandCharacteristic = gattCharacteristic;
                        deviceType = 0;
                        System.out.println("mCommandCharacteristic  " + mCommandCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                    }
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

                    if (mModelNumberCharacteristic==null || mSerialPortCharacteristic==null || mCommandCharacteristic==null) {
                        Toast.makeText(serviceContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
                        connectionState = "isToScan";
                        transferIntent.putExtra("connectionState", connectionState);
                        sendBroadcast(transferIntent);
                        mConnectionState = theConnectionState.valueOf(connectionState);
                        onConectionStateChange(mConnectionState);
                    }
                    else {
                        mSCharacteristic=mModelNumberCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(device, mSCharacteristic, true);
                        mBluetoothLeService.readCharacteristic(device, mSCharacteristic);
                    }
                    Log.d(TAG, "Connected to glass device.");
                    break;
                case 1:
                    mConnected_Bracelet = true;
                    mBraceletDevice = device;
                    mBraceletGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
                    mBraceletGattCharacteristics.add(charas);
                    mBluetoothLeService.setBraceletGatt(mBluetoothLeService.getGattFromDevice(device));
                    Log.d(TAG, "Connected to bracelet device.");
                    break;
                case 2:
                    if(!mConnected_GloveLeft)
                    {
                        mConnected_GloveLeft = true;
                        mGloveDeviceLeft = device;
                        mBluetoothLeServiceListener.onLeDeviceConnected(device,true);
                        Log.d(TAG, "Connected to glove device left.");
                    }
                    else if(!mConnected_GloveRight)
                    {
                        mConnected_GloveRight = true;
                        mGloveDeviceRight = device;
                        mBluetoothLeServiceListener.onLeDeviceConnected(device,false);
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

    public void onSerialReceived(String theString){
        String[] buffer = theString.split(",");
        Log.i("BlunoService", "onSerialRcecived");
        try{
            front  = Integer.parseInt(buffer[0]);
            left   = Integer.parseInt(buffer[1]);
            right  = Integer.parseInt(buffer[2]);
            Log.i("BlunoService", "front:" + front + ", left:" + left + ", right:" + right);
            stateProcess();
            transferIntent.putExtra("front", front);
            transferIntent.putExtra("left", left);
            transferIntent.putExtra("right", right);
            sendBroadcast(transferIntent);
            Log.i("BlunoService", "Warning Count = " + mWarningCount);

        }catch(Exception e){
            Log.e("BlunoService", "[Error onSerialReceived]: "+e.toString());
        }
    }

    public void stateProcess(){
        if(turnOff) {
            if(mWarningCount < mWarningCountThreshold) {
                mWarningCount = mWarningCountThreshold;
            }
        }

        if((left > sidesThreshold && right > sidesThreshold && front > frontThreshold) || mWarningCount >= mWarningCountThreshold){
            mWarningState = warningState.safe;
            //turnOff = true;
            if(mWarningCount >= (mWarningCountThreshold+1) && (Math.abs(left-leftTemp) > 5 || Math.abs(right-rightTemp) > 5 ||
                    Math.abs(front-frontTemp) > 5)){
                mWarningCount = 0;
                turnOff = false;
            }
            leftTemp = left;
            rightTemp = right;
            frontTemp = front;
            mWarningCount+=1;
        }
        else if(left < sidesThreshold && right > sidesThreshold && front > frontThreshold){
            if(mWarningState != warningState.left)
                mWarningCount = 0;
            mWarningState = warningState.left;
            mWarningText = "左方危險, 注意";
            mWarningCount += 1;
            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_left);
            //vibrate = new long[]{0, 100, 500, 100, 500};
            if(!onNotification) {
                doNotification notify = new doNotification();
                notify.start();
            }

        }
        else if(left > sidesThreshold && right < sidesThreshold && front > frontThreshold){
            if(mWarningState != warningState.right)
                mWarningCount = 0;
            mWarningState = warningState.right;
            mWarningText = "右方危險, 注意";
            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_right);
            //vibrate = new long[]{0, 500, 500, 100, 500};
            mWarningCount += 1;
            //postNotifications();
            if(!onNotification) {
                doNotification notify = new doNotification();
                notify.start();
            }
        }
        else if(left < sidesThreshold && right < sidesThreshold && front > frontThreshold){
            if(mWarningState != warningState.twoSide)
                mWarningCount = 0;
            mWarningState = warningState.twoSide;
            mWarningText = "兩側危險, 注意";
            mWarningCount += 1;
            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_twosides);
            //vibrate = new long[]{0, 1000, 500, 100, 500};
            //postNotifications();
            if(!onNotification) {
                doNotification notify = new doNotification();
                notify.start();
            }
        }
        else if(left > sidesThreshold && right > sidesThreshold && front < frontThreshold){
            if(mWarningState != warningState.front)
                mWarningCount = 0;
            mWarningState = warningState.front;
            mWarningText = "前方危險, 注意";
            mWarningCount += 1;
            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_front);
            //vibrate = new long[]{0, 0, 500, 500, 500};
            //postNotifications();
            if(!onNotification) {
                doNotification notify = new doNotification();
                notify.start();
            }
        }

        else if(left < sidesThreshold && right < sidesThreshold && front < frontThreshold){
            if(mWarningState != warningState.allDirection)
                mWarningCount = 0;
            mWarningState = warningState.allDirection;
            mWarningText = "密集區域, 注意";
            mWarningCount += 1;
            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning);
            //vibrate = new long[]{0, 1000, 500, 500, 500};
            //postNotifications();
            if(!onNotification) {
                doNotification notify = new doNotification();
                notify.start();
            }
        }
        else if(left < sidesThreshold && right > sidesThreshold && front < frontThreshold){
            if(mWarningState != warningState.frontAndLeft)
                mWarningCount = 0;
            mWarningState = warningState.frontAndLeft;
            mWarningText = "前方與左方危險, 注意";
            mWarningCount += 1;
            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_frontleft);
            //vibrate = new long[]{0, 100, 500, 500, 500};
            //postNotifications();
            if(!onNotification) {
                doNotification notify = new doNotification();
                notify.start();
            }
        }
        else if(left > sidesThreshold && right < sidesThreshold && front < frontThreshold){
            if(mWarningState != warningState.frontAndRight)
                mWarningCount = 0;
            mWarningState = warningState.frontAndRight;
            mWarningText = "前方與右方危險, 注意";
            mWarningCount += 1;
            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning_frontright);
            //vibrate = new long[]{0, 500, 500, 500, 500};
            //postNotifications();
            if(!onNotification) {
                doNotification notify = new doNotification();
                notify.start();
            }
        }
        else{
            if(mWarningState != warningState.others)
                mWarningCount = 0;
            mWarningState = warningState.others;
            mWarningText = "注意";
            mWarningCount += 1;
            soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning);
            //vibrate = new long[]{0, 1000, 500, 500, 500};
            //postNotifications();
            if(!onNotification) {
                doNotification notify = new doNotification();
                notify.start();
            }
        }

    }
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
   public void myNotification(){
       int notificationId = 001;

       Intent openIntent = new Intent(this, VisualSupportActivity.class);
       openIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
       PendingIntent openPendingIntent = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

       Intent deleteIntent = new Intent(this, DeleteService.class);
       PendingIntent deletePendingIntent = PendingIntent.getService(this, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);


       NotificationCompat.WearableExtender wearableExtender =
               new NotificationCompat.WearableExtender()
                       .setHintHideIcon(true)
                       .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.warning));

       NotificationCompat.Builder notificationBuilder =
               new NotificationCompat.Builder(this)
                       .setSmallIcon(R.mipmap.ic_launcher)
                       .setContentTitle("危險！")
                       .setContentText(mWarningText)
                       .setContentIntent(openPendingIntent)
                       .setDeleteIntent(deletePendingIntent)
                       .addAction(R.drawable.ic_full_reply, "Turn Off", deletePendingIntent)
                       .extend(wearableExtender)
                       .setPriority(NotificationCompat.PRIORITY_HIGH)
                       .setSound(soundUri)
                       .setVibrate(vibrate);

       // Get an instance of the NotificationManager service
       NotificationManagerCompat notificationManager =
               NotificationManagerCompat.from(this);

       // Build the notification and issues it with notification manager.
       notificationManager.notify(notificationId, notificationBuilder.build());
   }

    public class doNotification extends Thread {
        @Override
        public void run(){
            onNotification = true;
            myNotification();
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            onNotification = false;
        }
    }

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
            Log.d(TAG, "mDeviceAddress"+mDeviceAddress);

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

    private void removeDevice(BluetoothDevice device) {
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
                    if (mConnected_GloveLeft && mConnected_GloveRight)
                    {
                        mBluetoothLeService.readRemoteRssi();
                    }

                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        mReadRssiThread.start();
    }
}
