package tw.edu.ntust.jojllman.wearableapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;

import java.util.List;

import tw.edu.ntust.jojllman.wearableapplication.BLE.GloveListener;
import tw.edu.ntust.jojllman.wearableapplication.BLE.GloveService;

public class GloveControlActivity extends AppCompatActivity implements GloveListener {

    private Button btnRecognize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glove_control);

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

        btnRecognize = (Button) findViewById(R.id.btn_glove_control_recognize);
    }

    protected void onStart(){
        super.onStart();
        GloveService.gloveService.initRecognition(this);
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(GloveControlActivity.this.getResources().getText(R.string.glove_control));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnGloveRecognizeClick(View view){
        btnRecognize.setText(R.string.btn_glove_control_recognizing);
        GloveService.gloveService.OnRecognizeClicked();
    }

    public void OnGloveResetClick(View view){
        GloveService.gloveService.OnResetClicked();
    }

    @Override
    public void RecognizeUpdate(final String result, final boolean isRecognizing) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!result.equals("")) {
                    btnRecognize.setText(getResources().getText(R.string.btn_glove_control_recognizing) + "\n" + result);
                }else if(!isRecognizing){
                    btnRecognize.setText(getResources().getText(R.string.btn_glove_control_recognize) + "\nstopped");
                }
            }
        });
    }

    @Override
    public void RSSIUpdate(boolean isLeftHand, int rssi) {

    }

    @Override
    public void RecordFileUpdate(List<String> files) {

    }
}
