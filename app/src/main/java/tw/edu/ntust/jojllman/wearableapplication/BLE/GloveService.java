package tw.edu.ntust.jojllman.wearableapplication.BLE;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
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

import tw.edu.ntust.jojllman.wearableapplication.AppManageActivity;
import tw.edu.ntust.jojllman.wearableapplication.GlobalVariable;

/**
 * Created by Lian on 2016/4/6.
 */
public class GloveService implements RecognitionServiceListener, BluetoothLeServiceListener
{
    private final static String TAG = "GloveService";
    public static GloveService gloveService;
    private BlunoService blunoService;

    /***********************************************************
     * Common Components
     */
    private MediaPlayer mMediaPlayer;
    List<String> databaseList;
    List<String> recordFiles;

    /***********************************************************
     * UI Components
     */
    private Activity gloveActivity;
    public boolean isActivityReady = false;
    private GloveListener glovelistener;

    /***********************************************************
     * BLE Components
     */
    private static final int 	REQUEST_ENABLE_BT = 1;
    private static final long 	SCAN_PERIOD = 5000; // 5 seconds

    private BluetoothAdapter mBluetoothAdapter;
    public BluetoothDevice 		mBluetoothDeviceLeft, mBluetoothDeviceRight;
    private List<BluetoothDevice> 	mDeviceList;
    private BluetoothLeService 	mBluetoothLeService;
    private boolean 				mIsConnectedLeft, mIsConnectedRight;
    private Handler mScanHandler;
    // SDK version >= 21
    private BluetoothLeScanner mLeScanner;
    private List<ScanFilter> 		mLeScanFilters;
    private ScanSettings mLeScanSettings;
    public int rssiLeft = 0;
    public int rssiRight = 0;

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
    public boolean                  isRecording = false;

    /***********************************************************
     * Recognition Components
     */
    private RecognitionService			mRecognitionService;
    public boolean 					    isRecognizing;
    private Thread						mDataPushThread;
    private GloveSignData				mCurSignData;


    public GloveService(BlunoService service)
    {
        gloveService=this;
        blunoService=service;
    }

    private void scanPath(String path)
    {
        MediaScannerConnection.scanFile(gloveActivity.getApplicationContext(), new String[]{path}, null, null);
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

    public void initRecognition(Activity activity)
    {
        if(gloveActivity != null && GlobalVariable.isServiceRunning(gloveActivity, "tw.edu.ntust.jojllman.wearableapplication.BLE.RecognitionService")){
            gloveActivity.unbindService(mRecognitionServiceConnection);
        }
        gloveActivity = activity;
        mCurSignData = new GloveSignData();
        isRecognizing = false;
        isActivityReady = true;

        glovelistener=(GloveListener)activity;

        // Check whether database root path is exist
        checkFolder(PATH_DATABASE_ROOT, "Records");

        getSignNames();
        getRecordFilesAndUpdateRecordSpinner();

        Log.d(TAG, "Connecting RecognitionService... ");
        // Bind recognition service
        Intent recServiceIntent = new Intent(activity, RecognitionService.class);
        boolean status = activity.bindService(recServiceIntent, mRecognitionServiceConnection, Activity.BIND_AUTO_CREATE);
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

        glovelistener.RecordFileUpdate(files);
    }

    public void OnRecordClicked(final String filename){

        if (!isRecording)
        {
            isRecording=true;
            if (filename.equals(""))
            {
                Toast.makeText(gloveActivity.getApplicationContext(), "Plz enter filename", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start record
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    final boolean ret = GloveService.gloveService.startRecord(filename);

                    gloveActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ret) {
                                Toast.makeText(gloveActivity.getApplicationContext(), "Start recording", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(gloveActivity.getApplicationContext(), "Start recording failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }, 3000);
        }
        else
        {
            isRecording=false;
            // Stop record
            stopRecord();

            Toast.makeText(gloveActivity.getApplicationContext(), "Stop recording", Toast.LENGTH_SHORT).show();
            getRecordFilesAndUpdateRecordSpinner();
            scanPath(PATH_DATABASE_RECORD + File.separator + filename + FILE_EXTENSION_CSV);
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
        }
        else if (device.equals(mBluetoothDeviceRight))
        {
            mIsConnectedRight = false;
            mBluetoothDeviceRight = null;
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
        if (device.equals(mBluetoothDeviceLeft)) {
            rssiLeft = rssi;
            if(isActivityReady){
                mCurSignData._rssiL = rssi;
                glovelistener.RSSIUpdate(true, rssi);
            }
        } else if (device.equals(mBluetoothDeviceRight)) {
            rssiRight = rssi;
            if(isActivityReady){
                mCurSignData._rssiR = rssi;
                glovelistener.RSSIUpdate(false, rssi);
            }
        }
    }

    @Override
    public void onRecognized(final String word)
    {
        glovelistener.RecognizeUpdate(word,isRecognizing);

        gloveActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playAudio(PATH_DATABASE_AUDIO + File.separator + word + FILE_EXTENSION_MP3);
            }
        });
    }

    @Override
    public void onRecognizeFinished() {
        glovelistener.RecognizeUpdate("", isRecognizing);
    }

    public void OnRecognizeClicked(){
        if (!isRecognizing) {
            // Start recognize
            mDataPushThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRecognizing) {

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
            isRecognizing = true;
            mDataPushThread.start();
        } else {
            // Stop recognize
            isRecognizing = false;
            mDataPushThread.interrupt();
            mDataPushThread = null;
            mRecognitionService.stopRecognition();
        }
    }

    public void OnResetClicked(){
        if(mBluetoothDeviceLeft != null) {
            blunoService.resetRemoteDevice(mBluetoothDeviceLeft);
        }
        if(mBluetoothDeviceRight != null) {
            blunoService.resetRemoteDevice(mBluetoothDeviceRight);
        }
    }
}
