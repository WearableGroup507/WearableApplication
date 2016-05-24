package tw.edu.ntust.jojllman.wearableapplication;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.ImageButton;

import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoLibrary;
import tw.edu.ntust.jojllman.wearableapplication.BLE.BlunoService;
import tw.edu.ntust.jojllman.wearableapplication.BLE.TextToSpeechService;

public class ChooseClassActivity extends AppCompatActivity {
    private int mClickCount =0;
    private short mAutoEnter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_class);

        final GlobalVariable globalVariable = (GlobalVariable)getApplicationContext();
        globalVariable.readSetting();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ImageButton ibtnVisual = (ImageButton) findViewById(R.id.img_btn_visual);
        ibtnVisual.getLayoutParams().height = (int)(metrics.heightPixels*0.4);

        ImageButton ibtnHearing = (ImageButton) findViewById(R.id.img_btn_hearing);
        ibtnHearing.getLayoutParams().height = (int)(metrics.heightPixels*0.4);

        Button btnHelp = (Button) findViewById(R.id.btn_help);
        btnHelp.getLayoutParams().height = (int)(metrics.heightPixels*0.1);

        View decorView = getWindow().getDecorView();
        /*decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/

        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        Intent intent = new Intent(ChooseClassActivity.this, BlunoService.class);
        startService(intent);

        if(mAutoEnter == 1){
            Intent intentVisual = new Intent(ChooseClassActivity.this  , VisualSupportActivity.class);
            startActivity(intentVisual);
            this.finish();
            overridePendingTransition(0, 0);
        }else if(mAutoEnter == 2){
            Intent intentHearing = new Intent(ChooseClassActivity.this  , HearingSupportActivity.class);
            startActivity(intentHearing);
            this.finish();
            overridePendingTransition(0, 0);
        }
    }

    public void onResume(){
        super.onResume();

    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String contents=ChooseClassActivity.this.getResources().getText(R.string.app_name).toString();
            contents+=ChooseClassActivity.this.getResources().getText(R.string.app_menu).toString();
            event.getText().add(contents);
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnVisualClick(View view){
        mClickCount =0;

        Intent intent = new Intent();
        intent.setClass(ChooseClassActivity.this  , VisualSupportActivity.class);
        startActivity(intent);
//        this.finish();
    }

    public void OnHearingClick(View view){
        mClickCount =0;

        Intent intent = new Intent();
        intent.setClass(ChooseClassActivity.this  , HearingSupportActivity.class);
        startActivity(intent);
//        this.finish();
    }

    public void OnHelpClick(View view){
//        this.findViewById(android.R.id.content).announceForAccessibility("0123456789");
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final Dialog dialog = new Dialog(ChooseClassActivity.this,R.style.CustomDialog){
            @Override
            public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    String contents=ChooseClassActivity.this.getResources().getText(R.string.help_dialog).toString()+"，";
                    contents+=ChooseClassActivity.this.getResources().getText(R.string.help_message).toString();
                    event.getText().add(contents);
                    return true;
                }
                return super.dispatchPopulateAccessibilityEvent(event);
            }
        };
        dialog.setContentView(R.layout.dialog_help);

        ImageButton ibtn = (ImageButton)dialog.findViewById(R.id.img_btn_setting);
        ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (++mClickCount > 2) {
                    mClickCount = 0;
                    if (dialog.isShowing()) {
                        dialog.hide();
                    }
                    Intent intent = new Intent();
                    intent.setClass(ChooseClassActivity.this, AppManageActivity.class);
                    startActivity(intent);
                } else {
                    v.announceForAccessibility("繼續點擊" + (3 - mClickCount) + "次進入管理頁面");
                }
            }
        });

        Button btn = (Button)dialog.findViewById(R.id.btn_ok);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing()) {
                    mClickCount = 0;
                    dialog.hide();
                }
            }
        });

        dialog.show();
    }
}
