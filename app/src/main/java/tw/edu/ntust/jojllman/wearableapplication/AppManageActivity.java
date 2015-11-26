package tw.edu.ntust.jojllman.wearableapplication;

import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

public class AppManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manage);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(AppManageActivity.this.getResources().getText(R.string.app_manage));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnSearchDeviceClick(View view){
        Vibrator vi=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        vi.vibrate(1000);
//        Intent intent = new Intent();
//        intent.setClass(AppManageActivity.this  , SearchDeviceActivity.class);
//        startActivity(intent);
    }

    public void OnAppSettingClick(View view){
        Intent intent = new Intent();
        intent.setClass(AppManageActivity.this  , AppSettingActivity.class);
        startActivity(intent);
    }
}
