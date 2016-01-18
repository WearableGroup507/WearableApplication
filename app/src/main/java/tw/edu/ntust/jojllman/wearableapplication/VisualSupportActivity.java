package tw.edu.ntust.jojllman.wearableapplication;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.List;

public class VisualSupportActivity extends AppCompatActivity {
    int click_count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_support);
        
        DeviceInfoView btn_device_glass = (DeviceInfoView)findViewById(R.id.dev_info_btn_visual_glass);
        DeviceInfoView btn_device_bracelet = (DeviceInfoView)findViewById(R.id.dev_info_btn_visual_bracelet);

        btn_device_glass.setDeviceType(DeviceInfoView.GLASS);
        btn_device_bracelet.setDeviceType(DeviceInfoView.BRACELET);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.glass);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.bracelet);
        btn_device_glass.setBitmapToDraw(bitmap);
        btn_device_bracelet.setBitmapToDraw(bitmap2);

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
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(VisualSupportActivity.this.getResources().getText(R.string.visual_main));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnDeviceClick(View view){
        if(view.getId() == R.id.dev_info_btn_visual_glass){
            // TODO: 增加眼鏡按下動作
        }

        if(view.getId() == R.id.dev_info_btn_visual_bracelet){
            // TODO: 增加手環按下動作
        }
    }

    public void OnHelpClick(View view){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final Dialog dialog = new Dialog(VisualSupportActivity.this,R.style.CustomDialog){
            @Override
            public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    String contents=VisualSupportActivity.this.getResources().getText(R.string.help_dialog).toString()+"，";
                    contents+=VisualSupportActivity.this.getResources().getText(R.string.help_message).toString();
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
                    intent.setClass(VisualSupportActivity.this  , AppManageActivity.class);
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

    public boolean isTalkbackEnabled()
    {
        Intent screenReaderIntent = new Intent("android.accessibilityservice.AccessibilityService");
        screenReaderIntent.addCategory("android.accessibilityservice.category.FEEDBACK_SPOKEN");
        List<ResolveInfo> screenReaders = getPackageManager().queryIntentServices(
                screenReaderIntent, 0);
        ContentResolver cr = getContentResolver();
        Cursor cursor = null;
        int status = 0;
        for (ResolveInfo screenReader : screenReaders) {
            // All screen readers are expected to implement a content provider
            // that responds to
            // content://<nameofpackage>.providers.StatusProvider
            cursor = cr.query(Uri.parse("content://" + screenReader.serviceInfo.packageName
                    + ".providers.StatusProvider"), null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                // These content providers use a special cursor that only has one element,
                // an integer that is 1 if the screen reader is running.
                status = cursor.getInt(0);
                cursor.close();
                if (status == 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
