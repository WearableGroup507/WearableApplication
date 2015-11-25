package tw.edu.ntust.jojllman.wearableapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);

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
            btn[count].setText("裝置" + count);
            btn[count].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppSettingActivity.this.finish();
                }
            });
            ll.addView( btn[count] );
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
}