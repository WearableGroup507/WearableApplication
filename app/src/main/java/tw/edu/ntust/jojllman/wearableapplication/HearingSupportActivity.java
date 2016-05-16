package tw.edu.ntust.jojllman.wearableapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.ImageButton;

import tw.edu.ntust.jojllman.wearableapplication.BLE.GloveService;

public class HearingSupportActivity extends AppCompatActivity {
    int click_count;

    Handler handler=new Handler();
    DeviceInfoView btn_device_glove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearing_support);

        btn_device_glove = (DeviceInfoView)findViewById(R.id.dev_info_btn_hearing_glove);
        
        btn_device_glove.setDeviceType(DeviceInfoView.GLOVE);
        
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.glove);
        btn_device_glove.setBitmapToDraw(bitmap);

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
        actionBar.setDisplayHomeAsUpEnabled(true);

        handler.post(new Runnable() {
            @Override
            public void run() {
                btn_device_glove.setSignal((short) ((GloveService.gloveService.rssiLeft + GloveService.gloveService.rssiRight)/2));
                handler.postDelayed(this, 500); // set time here to refresh textView
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(HearingSupportActivity.this.getResources().getText(R.string.hearing_main));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnDeviceClick(View view){
        if(GloveService.gloveService.mBluetoothDeviceLeft != null &&
                GloveService.gloveService.mBluetoothDeviceRight != null &&
                GloveService.gloveService.mBluetoothDeviceLeft != GloveService.gloveService.mBluetoothDeviceRight)
        {
            Intent intent = new Intent();
            intent.setClass(HearingSupportActivity.this, GloveControlActivity.class);
            startActivity(intent);
        }
    }

    public void OnHelpClick(View view){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final Dialog dialog = new Dialog(HearingSupportActivity.this,R.style.CustomDialog){
            @Override
            public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    String contents=HearingSupportActivity.this.getResources().getText(R.string.help_dialog).toString()+"，";
                    contents+=HearingSupportActivity.this.getResources().getText(R.string.help_message).toString();
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
                if(click_count++>2){
                    click_count=0;
                    if(dialog.isShowing()) {
                        dialog.hide();
                    }
                    Intent intent = new Intent();
                    intent.setClass(HearingSupportActivity.this  , AppManageActivity.class);
                    startActivity(intent);
                }
            }
        });

        Button btn = (Button)dialog.findViewById(R.id.btn_ok);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing()) {
                    click_count = 0;
                    dialog.hide();
                }
            }
        });

        dialog.show();
    }
}
