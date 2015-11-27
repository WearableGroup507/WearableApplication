package tw.edu.ntust.jojllman.wearableapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.ImageButton;

public class HearingSupportActivity extends AppCompatActivity {
    int click_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearing_support);

        DeviceInfoView btn_device_glove = (DeviceInfoView)findViewById(R.id.dev_info_btn_hearing_glove);
        
        btn_device_glove.setDeviceType(DeviceInfoView.GLOVE);
        
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.glove);
        btn_device_glove.setBitmapToDraw(bitmap);
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
        // TODO: 增加手套按下動作
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
        dialog.setContentView(R.layout.custom_dialog);

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
