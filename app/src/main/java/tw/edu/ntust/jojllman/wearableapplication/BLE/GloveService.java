package tw.edu.ntust.jojllman.wearableapplication.BLE;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import tw.edu.ntust.jojllman.wearableapplication.GloveSettingActivity;
import tw.edu.ntust.jojllman.wearableapplication.R;

/**
 * Created by Lian on 2016/4/6.
 */
public class GloveService implements RecognitionServiceListener, BluetoothLeServiceListener
{
    private final static String TAG = "GloveService";

    /***********************************************************
     * Common Components
     */
    private Resources mResources;
    private Activity gloveSettingActivity;
    private MediaPlayer mMediaPlayer;
    List<String> databaseList;
    List<String> recordFiles;

    /***********************************************************
     * UI Components
     */
    private Button btnScan, btnConnect, btnReset, btnRecord, btnRecognize;
    private TextView txtLeftHand, txtRightHand;
    private Spinner spnRecord;
    private ListView lvDatabase;
    private EditText etFilename;
    private TextView txtLeftRssi, txtRightRssi, txtResult;
    private boolean isActivityReady = false;

    /***********************************************************
     * BLE Components
     */
    private static final int 	REQUEST_ENABLE_BT = 1;
    private static final long 	SCAN_PERIOD = 5000; // 5 seconds

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice 		mBluetoothDeviceLeft, mBluetoothDeviceRight;
    private List<BluetoothDevice> 	mDeviceList;
    private BluetoothLeService 	mBluetoothLeService;
    private boolean 				mIsConnectedLeft, mIsConnectedRight;
    private Handler mScanHandler;
    // SDK version >= 21
    private BluetoothLeScanner mLeScanner;
    private List<ScanFilter> 		mLeScanFilters;
    private ScanSettings mLeScanSettings;

    /***********************************************************
     * Recording Components
     */
    private static final String PATH_DATABASE_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "WearableDatabase";
    private static final String PATH_DATABASE_DATABASE = PATH_DATABASE_ROOT + File.separator + "Database";
    private static final String PATH_DATABASE_RECORD = PATH_DATABASE_ROOT + File.separator + "Records";
    private static final String PATH_DATABASE_AUDIO = PATH_DATABASE_ROOT + File.separator + "Audio";
    private static final String FILE_EXTENSION_CSV = ".csv";
    private static final String FILE_EXTENSION_MP3 = ".mp3";

    private ScheduledExecutorService mExecutorServiceRecord;  // executor service for recording
    private ScheduledFuture<?> mScheduledFutureRecord;
    private BufferedWriter 			mBufferedWriterRecod;
    private int						mDataCounter;

    /***********************************************************
     * Recognition Components
     */
    private RecognitionService			mRecognitionService;
    private boolean 					    mIsRecognizing;
    private Thread						mDataPushThread;
    private GloveSignData				mCurSignData;


    public GloveService()
    {
        GloveSettingActivity.setGloveService(this);
    }

    private void scanPath(String path)
    {
        MediaScannerConnection.scanFile(gloveSettingActivity.getApplicationContext(), new String[]{path}, null, null);
    }

    public void initUI(Activity activity)
    {
        gloveSettingActivity = activity;
        mResources = gloveSettingActivity.getResources();
        getViewId();
        initRecognition();
        btnRecord = (Button) gloveSettingActivity.findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String filename = etFilename.getText().toString();

                Log.d(TAG,"Record clicked. "+btnRecord.getText()+"=="+mResources.getText(R.string.ui_text_record));
                if (btnRecord.getText().equals(mResources.getText(R.string.ui_text_record)))
                {
                    if (filename.equals(""))
                    {
                        Toast.makeText(gloveSettingActivity.getApplicationContext(), "Plz enter filename", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Start record
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final boolean ret = startRecord(filename);

                            gloveSettingActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (ret) {
                                        Toast.makeText(gloveSettingActivity.getApplicationContext(), "Start recording", Toast.LENGTH_SHORT).show();
                                        btnRecord.setText(R.string.ui_text_recording);
                                    } else {
                                        Toast.makeText(gloveSettingActivity.getApplicationContext(), "Start recording failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }, 3000);
                }
                else
                {
                    // Stop record
                    stopRecord();

                    Toast.makeText(gloveSettingActivity.getApplicationContext(), "Stop recording", Toast.LENGTH_SHORT).show();
                    btnRecord.setText(R.string.ui_text_record);
                    getRecordFilesAndUpdateRecordSpinner();
                    scanPath(PATH_DATABASE_RECORD + File.separator + filename + FILE_EXTENSION_CSV);
                }
            }
        });

        btnRecognize = (Button) gloveSettingActivity.findViewById(R.id.btnRecognize);
        btnRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRecognize.setText(R.string.ui_text_recognizing);
                txtResult.setText("");

                if (!mIsRecognizing) {
                    // Start recognize
                    mDataPushThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (mIsRecognizing) {

                                mRecognitionService.pushSignData(mCurSignData);

                                try {
                                    Thread.sleep(30);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    mRecognitionService.startRecognition();
                    mIsRecognizing = true;
                    mDataPushThread.start();
                } else {
                    // Stop recognize
                    mIsRecognizing = false;
                    mDataPushThread.interrupt();
                    mDataPushThread = null;
                    mRecognitionService.stopRecognition();
                }
            }
        });
        if(mBluetoothDeviceLeft != null){
            txtLeftHand.setText(mBluetoothDeviceLeft.getName()+"  "+mBluetoothDeviceLeft.getAddress());
        }
        if(mBluetoothDeviceRight != null){
            txtRightHand.setText(mBluetoothDeviceRight.getName()+"  "+mBluetoothDeviceRight.getAddress());
        }
        isActivityReady = true;
    }

    private void getViewId(){
        etFilename = (EditText) gloveSettingActivity.findViewById(R.id.etFilename);
        txtLeftHand = (TextView) gloveSettingActivity.findViewById(R.id.txtLeftHand);
        txtRightHand = (TextView) gloveSettingActivity.findViewById(R.id.txtRightHand);
        spnRecord = (Spinner) gloveSettingActivity.findViewById(R.id.spnRecord);
        txtLeftRssi = (TextView) gloveSettingActivity.findViewById(R.id.txtLeftRssi);
        txtRightRssi = (TextView) gloveSettingActivity.findViewById(R.id.txtRightRssi);
        txtResult = (TextView) gloveSettingActivity.findViewById(R.id.txtResult);

        lvDatabase = (ListView) gloveSettingActivity.findViewById(R.id.lvDatabase);
    }

    private void playAudio(String filename)
    {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
        {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mMediaPlayer = new MediaPlayer();
        try
        {
            mMediaPlayer.setDataSource(filename);
            mMediaPlayer.prepare();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        });
        mMediaPlayer.start();
    }

    /***********************************************************
     * Recording and Recognition Related Functions
     */

    private RecognitionServiceListener mRecognitionServiceListener = this;
    private ServiceConnection mRecognitionServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.d(TAG, "onRecognitionService connected. ");
            mRecognitionService = ((RecognitionService.ServiceBinder) service).getService();
            mRecognitionService.registerListener(mRecognitionServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mRecognitionService.unregisterListener(mRecognitionServiceListener);
            mRecognitionService = null;
        }
    };

    private void initRecognition()
    {
        mCurSignData = new GloveSignData();
        mIsRecognizing = false;

        // Check whether database root path is exist
        checkFolder(PATH_DATABASE_ROOT, "Records");

        getSignNames();
        getRecordFilesAndUpdateRecordSpinner();

        Log.d(TAG, "Connecting RecognitionService... ");
        // Bind recognition service
        Intent recServiceIntent = new Intent(gloveSettingActivity, RecognitionService.class);
        boolean status = gloveSettingActivity.bindService(recServiceIntent, mRecognitionServiceConnection, gloveSettingActivity.BIND_AUTO_CREATE);
        Log.d(TAG, "Connect to RecognitionService: " + status);
    }

    private void checkFolder(String root, String record)
    {
        File rootPath = new File(root);
        if (!rootPath.exists())
        {
            boolean status = rootPath.mkdirs();
            if (status)
            {
                Log.d(TAG, "Folder " + rootPath.getAbsolutePath() + " created!");
            }
            else
            {
                Log.d(TAG, "Unable to create folder: " + rootPath.getAbsolutePath());
            }
        }
        else
        {
            Log.d(TAG, "Folder " + rootPath.getAbsolutePath() + " already exists!");
        }

        File recordPath = new File(rootPath, record);
        if (!recordPath.exists())
        {
            boolean status = recordPath.mkdir();
            if (status)
            {
                Log.d(TAG, "Folder " + recordPath.getAbsolutePath() + " created!");
            }
            else
            {
                Log.d(TAG, "Unable to create folder: " + recordPath.getAbsolutePath());
            }
        }
        else
        {
            Log.d(TAG, "Folder " + recordPath.getAbsolutePath() + " already exists!");
        }
    }

    private void getSignNames()
    {
        File signNameFile = new File(PATH_DATABASE_DATABASE + File.separator + "SignName" + FILE_EXTENSION_CSV);
        BufferedReader reader;
        databaseList = new ArrayList<>();
        try
        {
            reader = new BufferedReader(new FileReader(signNameFile), 1024);

            String signName = "";
            // Read each line to get sign list
            while((signName = reader.readLine()) != null)
            {
                databaseList.add(signName);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ArrayAdapter<String> lvDatabaseAdapter = new ArrayAdapter<>(gloveSettingActivity.getApplicationContext(), android.R.layout.simple_list_item_1, databaseList);
        lvDatabase.setAdapter(lvDatabaseAdapter);
    }

    private void getRecordFilesAndUpdateRecordSpinner()
    {
        List<String> files = new ArrayList<>();

        File path = new File(PATH_DATABASE_RECORD);
        File list[] = path.listFiles();

        for (int i = 0; i < list.length; i++)
        {
            String fileName = list[i].getName();
            if (fileName.endsWith(FILE_EXTENSION_CSV))
            {
                files.add(fileName);
            }
        }

        if(gloveSettingActivity != null){
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(gloveSettingActivity.getApplication(), R.layout.layout_spinner_device, files);
            spnRecord.setAdapter(arrayAdapter);
        }
    }

    private boolean startRecord(String filename)
    {
        mExecutorServiceRecord = Executors.newSingleThreadScheduledExecutor();
        File file = new File(PATH_DATABASE_RECORD + File.separator + filename + FILE_EXTENSION_CSV);
        mDataCounter = 0;
        try
        {
            mBufferedWriterRecod = new BufferedWriter(new FileWriter(file), 1024);

            mScheduledFutureRecord = mExecutorServiceRecord.scheduleAtFixedRate(new Runnable()
            {
                @Override
                public void run()
                {
                    runRecord();
                }
            }, 2000, 30, TimeUnit.MILLISECONDS);

            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();

            return false;
        }
    }

    private boolean stopRecord()
    {
        if (!mScheduledFutureRecord.isCancelled())
            mScheduledFutureRecord.cancel(true);

        try
        {
            mBufferedWriterRecod.close();

            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();

            return false;
        }
    }

    private void runRecord()
    {
        try
        {
            mBufferedWriterRecod.write(mDataCounter + "," + mCurSignData.getCsv());
            mBufferedWriterRecod.newLine();
            mDataCounter++;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /***********************************************************
     * Service Listener Functions
     */

    @Override
    public void onLeDeviceConnected(BluetoothDevice device, boolean isLeft) {
        Log.d(TAG,"onLeDeviceConnected" + device.getName());
        if(isLeft){
            mIsConnectedLeft = true;
            mBluetoothDeviceLeft = device;
        }else{
            mIsConnectedRight = true;
            mBluetoothDeviceRight = device;
        }
    }

    @Override
    public void onLeDeviceDisconnected(final BluetoothDevice device)
    {
        if (device.equals(mBluetoothDeviceLeft))
        {
            mIsConnectedLeft = false;
            mBluetoothDeviceLeft = null;
            txtLeftHand.setText("Left");
        }
        else if (device.equals(mBluetoothDeviceRight))
        {
            mIsConnectedRight = false;
            mBluetoothDeviceRight = null;
            txtRightHand.setText("Right");
        }
    }
    @Override
    public void onLeServiceDiscovered(boolean status)
    {
        // Start data transmission
//        mBluetoothLeService.startNotification(mBluetoothDeviceLeft);
//        mBluetoothLeService.startNotification(mBluetoothDeviceRight);
    }

    @Override
    public void onQuaternionChanged(BluetoothDevice device, float[] quaternion)
    {
        if(isActivityReady) {
            Log.d(TAG, "onQuaternion:" + device.getAddress());
            if (device.equals(mBluetoothDeviceLeft)) {
                System.arraycopy(quaternion, 0, mCurSignData._quatL, 0, quaternion.length);
            } else if (device.equals(mBluetoothDeviceRight)) {
                System.arraycopy(quaternion, 0, mCurSignData._quatR, 0, quaternion.length);
            }
        }
    }

    @Override
    public void onGestureChanged(BluetoothDevice device, float[] gesture)
    {
        if(isActivityReady) {
            Log.d(TAG, "onGestureChanged:" + device.getAddress());
            if (device.equals(mBluetoothDeviceLeft)) {
                System.arraycopy(gesture, 0, mCurSignData._gestL, 0, gesture.length);
            } else if (device.equals(mBluetoothDeviceRight)) {
                System.arraycopy(gesture, 0, mCurSignData._gestR, 0, gesture.length);
            }
        }
    }

    @Override
    public void onTouchChanged(BluetoothDevice device, float[] touch)
    {

    }

    @Override
    public void onAccelerationChanged(BluetoothDevice device, float[] acceleration)
    {
        if(isActivityReady) {
            Log.d(TAG, "onAccelerationChanged:" + device.getAddress());
            if (device.equals(mBluetoothDeviceLeft)) {
                System.arraycopy(acceleration, 0, mCurSignData._acceL, 0, acceleration.length);
            } else if (device.equals(mBluetoothDeviceRight)) {
                System.arraycopy(acceleration, 0, mCurSignData._acceR, 0, acceleration.length);
            }
        }
    }

    @Override
    public void onRSSIRead(BluetoothDevice device, final int rssi)
    {
        if(isActivityReady) {
            if (device.equals(mBluetoothDeviceLeft)) {
                mCurSignData._rssiL = rssi;
                gloveSettingActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (txtLeftRssi != null) txtLeftRssi.setText(rssi + "");
                    }
                });

            } else if (device.equals(mBluetoothDeviceRight)) {
                mCurSignData._rssiR = rssi;
                gloveSettingActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (txtRightRssi != null) txtRightRssi.setText(rssi + "");
                    }
                });
            }
        }
    }

    @Override
    public void onRecognized(final String word)
    {
        gloveSettingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtResult.setText(word);
                playAudio(PATH_DATABASE_AUDIO + File.separator + word + FILE_EXTENSION_MP3);
            }
        });
    }

    @Override
    public void onRecognizeFinished()
    {
        gloveSettingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnRecognize.setText(R.string.ui_text_recognize);
            }
        });
    }
}
