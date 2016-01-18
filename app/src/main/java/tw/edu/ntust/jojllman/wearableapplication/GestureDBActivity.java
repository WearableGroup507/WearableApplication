package tw.edu.ntust.jojllman.wearableapplication;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GestureDBActivity extends AppCompatActivity {

    /*static private int STATE_IDLE = 1;
    static private int STATE_RECORDING = 2;
    static private int STATE_SAVING = 3;

    private TextView txt_record_description;
    private TextView txt_saving_record;
    private TextView txt_recording;
    private Button btn_start_record;
    private Button btn_stop_record;
    private ProgressBar progBar_record;

    private int view_state;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_db);

        /*txt_record_description = (TextView)findViewById(R.id.textView_record_description);
        txt_saving_record = (TextView)findViewById(R.id.textView_saving_record);
        txt_recording = (TextView)findViewById(R.id.textView_recording);
        btn_start_record = (Button)findViewById(R.id.btn_start_record);
        btn_stop_record = (Button)findViewById(R.id.btn_stop_record);
        progBar_record = (ProgressBar)findViewById(R.id.progressBar_record);

        view_state = 1;*/

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
            event.getText().add(GestureDBActivity.this.getResources().getText(R.string.gesture_db));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    /*private void setViewState(int state)
    {
        if(state <= view_state && (view_state - state) != 2)
            return;

        switch (state)
        {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;

            Log.e(this.ge)
        }
    }*/
}
