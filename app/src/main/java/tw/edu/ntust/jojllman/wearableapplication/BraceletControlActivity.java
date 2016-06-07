package tw.edu.ntust.jojllman.wearableapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService;

public class BraceletControlActivity extends AppCompatActivity {
    private static String TAG = BraceletControlActivity.class.getSimpleName();
    private Intent braceletControlIntent = new Intent("tw.edu.ntust.jojllman.wearableapplication.BRACELET_SEND_CONTROL");

    private BlunoService.BraceletState m_braceletState= BlunoService.BraceletState.none;
    private RelativeLayout layoutDistance;
    private RelativeLayout layoutColor;
    private RelativeLayout layoutSearch;
    private boolean killRunnable = false;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_bracelet_menu);

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
                        ((TextView)layoutDistance.getChildAt(0)).setText(getString(R.string.btn_bracelet_distance) + "\n" + BlunoService.Bracelet_DT / 10 + "公分");
                        layoutDistance.setContentDescription("物體距離" + BlunoService.Bracelet_DT / 10 + "公分。點擊關閉手環距離偵測");
                    }else{
                        layoutDistance.setContentDescription(getString(R.string.btn_bracelet_distance));
                        ((TextView)layoutDistance.getChildAt(0)).setText(R.string.btn_bracelet_distance);
                    }
                    if(m_braceletState == BlunoService.BraceletState.color){
                        ((TextView)layoutColor.getChildAt(0)).setText(getString(R.string.btn_bracelet_color) + "\n" + BlunoService.getColorName());
                        layoutColor.setContentDescription(BlunoService.getColorName() + "。點擊關閉手環顏色辨識");
                    }else{
                        layoutColor.setContentDescription(getString(R.string.btn_bracelet_color));
                        ((TextView)layoutColor.getChildAt(0)).setText(R.string.btn_bracelet_color);
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
        layoutDistance = (RelativeLayout)findViewById(R.id.layout_bracelet_distance);
        layoutColor = (RelativeLayout)findViewById(R.id.layout_bracelet_color);
        layoutSearch = (RelativeLayout)findViewById(R.id.layout_bracelet_search);
    }

    private BroadcastReceiver braceletReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String braceletState = intent.getStringExtra("BraceletState");
            m_braceletState = BlunoService.BraceletState.valueOf(braceletState);
        }
    };

    public void OnBraceletMenuClick(final View view){
        if(view.getId() == R.id.layout_bracelet_color){
            OnColorClick(view);
        }else if(view.getId() == R.id.layout_bracelet_distance){
            OnDistanceClick(view);
        }else if(view.getId() == R.id.layout_bracelet_search){
            OnSearchClick(view);
        }
    }

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
//        braceletControlIntent.putExtra("BraceletSearch",true);
//        sendBroadcast(braceletControlIntent);
    }
}
