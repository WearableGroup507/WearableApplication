package tw.edu.ntust.jojllman.wearableapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ChooseClassActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_class);
    }

    public void OnHelpClick(View view){
        Intent intent = new Intent();
        intent.setClass(this  , SettingActivity.class);
        startActivity(intent);
    }
}
