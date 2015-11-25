package tw.edu.ntust.jojllman.wearableapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class ChooseClassActivity extends AppCompatActivity {
    int click_count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_class);
    }

    public void OnGlassesClicked(View view){
        click_count=0;
        Toast.makeText(this,"眼鏡裝置資訊",Toast.LENGTH_SHORT).show();
    }

    public void OnWristbandClicked(View view){
        click_count=0;
        Toast.makeText(this,"手環裝置資訊",Toast.LENGTH_SHORT).show();
    }

    public void OnHelpClicked(View view){
        if(click_count++>2){
            Toast.makeText(this,"開啟裝置設定頁",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setClass(this  , SettingActivity.class);
            startActivity(intent);
            click_count=0;
        }else{
            Toast.makeText(this,"說明訊息在這裡",Toast.LENGTH_SHORT).show();
        }
    }
}
