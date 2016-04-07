package tw.edu.ntust.jojllman.wearableapplication;

import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import tw.edu.ntust.jojllman.wearableapplication.BLE.GloveService;

public class GloveSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glove_setting);

        GloveService.setGloveSettingActivity(this);
    }

}
