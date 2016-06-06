package tw.edu.ntust.jojllman.wearableapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService;

public class BraceletControlActivity extends AppCompatActivity {
    private static String TAG = BraceletControlActivity.class.getSimpleName();
    private Intent braceletControlIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.BRACELET_SEND_CONTROL");

    private BlunoService.BraceletState m_braceletState= BlunoService.BraceletState.none;
    private Button btnDistance;
    private Button btnColor;
    private Button btnSearch;
    private boolean killRunnable = false;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bracelet_control);

        findView();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tw.edu.ntust.jojllman.wearableapplication.BRACELET_STATE");
        registerReceiver(braceletReceiver, intentFilter);
    }

    protected void onResume(){
        super.onResume();

        killRunnable = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(!killRunnable){
//                    Log.d(TAG, m_braceletState.name());
                    if(m_braceletState == BlunoService.BraceletState.distance) {
                        btnDistance.setText(getString(R.string.btn_bracelet_distance) + "\n" + BlunoService.Bracelet_DT / 10 + "公分");
                        btnDistance.setContentDescription("物體距離" + BlunoService.Bracelet_DT / 10 + "公分。關閉手環距離偵測");
                    }else{
                        btnDistance.setContentDescription(getString(R.string.btn_bracelet_distance));
                        btnDistance.setText(R.string.btn_bracelet_distance);
                    }
                    if(m_braceletState == BlunoService.BraceletState.color){
                        btnColor.setText(getString(R.string.btn_bracelet_color) + "\n" + BlunoService.getColorName());
                        btnColor.setContentDescription(BlunoService.getColorName() + "。關閉手環顏色辨識");
                    }else{
                        btnColor.setContentDescription(getString(R.string.btn_bracelet_color));
                        btnColor.setText(R.string.btn_bracelet_color);
                    }
                }else{
                    mHandler.removeCallbacksAndMessages(this);
                }
                mHandler.postDelayed(this, 500);
            }
        });
    }

    protected void onPause(){
        super.onPause();
        killRunnable = true;
    }

    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(braceletReceiver);
    }

    private void findView() {
        btnDistance = (Button)findViewById(R.id.btn_bracelet_distance);
        btnColor = (Button)findViewById(R.id.btn_bracelet_color);
        btnSearch = (Button)findViewById(R.id.btn_bracelet_search);
    }

    private BroadcastReceiver braceletReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String braceletState = intent.getStringExtra("BraceletState");
            m_braceletState = BlunoService.BraceletState.valueOf(braceletState);
        }
    };

    public void OnDistanceClick(final View view){
//        view.setContentDescription("測試 測試 test test");
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

    public void OnColorClick(final View view){
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

    public void OnSearchClick(final View view){

    }
}
