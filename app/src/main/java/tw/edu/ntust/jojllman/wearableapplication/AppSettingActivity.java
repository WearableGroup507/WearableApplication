package tw.edu.ntust.jojllman.wearableapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RadioButton;

public class AppSettingActivity extends AppCompatActivity {
    private GlobalVariable globalVariable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);

        globalVariable = (GlobalVariable)getApplicationContext();

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && globalVariable.isSettingChanged())
        {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(AppSettingActivity.this);
            builder.setMessage(R.string.accept_dialog)
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            globalVariable.saveSetting();
                            AppSettingActivity.this.finish();
                        }
                    })
                    .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            globalVariable.readSetting();
                            AppSettingActivity.this.finish();
                        }
                    });
            builder.create().show();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String contents=AppSettingActivity.this.getResources().getText(R.string.app_setting).toString()+"，";
            contents+=AppSettingActivity.this.getResources().getText(R.string.txt_set_vibrate_level).toString()+"，";
            switch(globalVariable.getVibrate_level()){
                case GlobalVariable.VIBRATE_LIGHT:
                    contents+="目前強度，弱";
                    break;
                case GlobalVariable.VIBRATE_MID:
                    contents+="目前強度，中";
                    break;
                case GlobalVariable.VIBRATE_STRONG:
                    contents+="目前強度，強";
                    break;
            }
            event.getText().add(contents);
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnRadioClick(View v) {
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
