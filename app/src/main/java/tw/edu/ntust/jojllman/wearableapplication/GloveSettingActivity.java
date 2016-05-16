package tw.edu.ntust.jojllman.wearableapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoLibrary;
import tw.edu.ntust.jojllman.wearableapplication.BLE.GloveListener;
import tw.edu.ntust.jojllman.wearableapplication.BLE.GloveService;

public class GloveSettingActivity extends AppCompatActivity implements GloveListener{

    private final static String TAG = "GloveSettingActivity";

    private Resources mResources;
    private Activity gloveSettingActivity;

    private Button btnScan, btnConnect, btnReset, btnRecord, btnRecognize;
    private TextView txtLeftHand, txtRightHand;
    private Spinner spnRecord;
    private ListView lvDatabase;
    private EditText etFilename;
    private TextView txtLeftRssi, txtRightRssi, txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glove_setting);

        gloveSettingActivity = this;
        mResources = gloveSettingActivity.getResources();

        initUI();
    }

    protected void onStart(){
        super.onStart();
        GloveService.gloveService.initRecognition(this);
    }

    public void initUI()
    {
        getViewId();
        btnRecord = (Button) gloveSettingActivity.findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "Record clicked. ");
                GloveService.gloveService.OnRecordClicked(etFilename.getText().toString());
                if(GloveService.gloveService.isRecording){
                    btnRecord.setText(R.string.ui_text_recording);
                }else{
                    btnRecord.setText(R.string.ui_text_record);
                }
            }
        });

        btnRecognize = (Button) gloveSettingActivity.findViewById(R.id.btnRecognize);
        btnRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRecognize.setText(R.string.ui_text_recognizing);
                txtResult.setText("");

                GloveService.gloveService.OnRecognizeClicked();
            }
        });
        if(GloveService.gloveService.mBluetoothDeviceLeft != null){
            txtLeftHand.setText(GloveService.gloveService.mBluetoothDeviceLeft.getName()+"  "+GloveService.gloveService.mBluetoothDeviceLeft.getAddress());
        }
        if(GloveService.gloveService.mBluetoothDeviceRight != null){
            txtRightHand.setText(GloveService.gloveService.mBluetoothDeviceRight.getName()+"  "+GloveService.gloveService.mBluetoothDeviceRight.getAddress());
        }
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

    @Override
    public void RecognizeUpdate(final String result, final boolean isRecognizing) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!result.equals("")) {
                    btnRecognize.setText(R.string.ui_text_recognizing);
                    txtResult.setText(result);
                }else if(!isRecognizing){
                    btnRecognize.setText(R.string.ui_text_recognize);
                    txtResult.setText("stopped");
                }
            }
        });
    }

    @Override
    public void RSSIUpdate(final boolean isLeftHand, final int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isLeftHand) {
                    if (txtLeftRssi != null) txtLeftRssi.setText(rssi + "");
                }else {
                    if (txtRightRssi != null) txtRightRssi.setText(rssi + "");
                }
            }
        });
    }

    @Override
    public void RecordFileUpdate(final List<String> files) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplication(), R.layout.layout_spinner_device, files);
                spnRecord.setAdapter(arrayAdapter);
            }
        });
    }
}
