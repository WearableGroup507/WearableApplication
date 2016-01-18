package tw.edu.ntust.jojllman.wearableapplication;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SearchDeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);

        // 取得 LinearLayout 物件
        LinearLayout ll = (LinearLayout)findViewById(R.id.view_setting);

//         將 TextView 加入到 LinearLayout 中
        TextView tv = new TextView(this);
        tv.setText(R.string.btn_app_setting);
        tv.setTextSize(50);
        ll.addView( tv );

//         將 Button  加入到 LinearLayout 中
        int count=10;
        Button btn[] = new Button[count];
        while(count>0){
            btn[--count] = new Button(this);
            btn[count].setText("裝置" + count + "   訊號：" + (100/(btn.length-count+1)));
            if(count<5) {
                btn[count].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SearchDeviceActivity.this.finish();
                    }
                });
            }else{
                btn[count].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((GlobalVariable) getApplicationContext()).vibrate(200);
                    }
                });
            }
            ll.addView( btn[count] );
        }

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
            event.getText().add(SearchDeviceActivity.this.getResources().getText(R.string.search_device));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }
}
