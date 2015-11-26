package tw.edu.ntust.jojllman.wearableapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class AppSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);

        GlobalVariable globalVariable = (GlobalVariable)getApplicationContext();

        switch(globalVariable.getVibrate_level()){
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
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(AppSettingActivity.this.getResources().getText(R.string.app_setting));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnRadioClick(View v) {
        GlobalVariable globalVariable = (GlobalVariable)getApplicationContext();

        switch(v.getId()){
            case R.id.rdo_light:
                globalVariable.setVibrate_level(GlobalVariable.VIBRATE_LIGHT);
                break;
            case R.id.rdo_mid:
                globalVariable.setVibrate_level(GlobalVariable.VIBRATE_MID);
                break;
            case R.id.rdo_strong:
                globalVariable.setVibrate_level(GlobalVariable.VIBRATE_STRONG);
                break;
        }
    }
}
