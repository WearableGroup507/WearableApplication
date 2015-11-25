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

    public void OnHelpClick(View view){
        Toast.makeText(this,"說明訊息出現",Toast.LENGTH_SHORT).show();
        if(click_count++>2){
            Intent intent = new Intent();
            intent.setClass(this  , SettingActivity.class);
            startActivity(intent);
            click_count=0;
        }
    }
}
