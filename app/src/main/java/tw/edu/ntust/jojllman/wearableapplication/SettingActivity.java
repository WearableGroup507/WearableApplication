package tw.edu.ntust.jojllman.wearableapplication;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 取得 LinearLayout 物件
        LinearLayout ll = (LinearLayout)findViewById(R.id.view_setting);

        // 將 TextView 加入到 LinearLayout 中
        TextView tv = new TextView(this);
        tv.setText(R.string.str_device_list);
        tv.setTextSize(50);
        ll.addView( tv );

        // 將 Button  加入到 LinearLayout 中
        int count=10;
        Button btn[] = new Button[count];
        while(count>0){
            btn[--count] = new Button(this);
            btn[count].setText("裝置" + count);
            btn[count].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(SettingActivity.this, "回到主畫面", Toast.LENGTH_SHORT).show();
                    SettingActivity.this.finish();
                }
            });
            ll.addView( btn[count] );
        }
    }
}
