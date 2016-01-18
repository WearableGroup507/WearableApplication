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

import java.util.Timer;
import java.util.TimerTask;

public class RecordGestureActivity extends AppCompatActivity {

    static private String TAG = "RecordGestureActivity";

    static private int STATE_IDLE = 1;
    static private int STATE_RECORDING = 2;
    static private int STATE_SAVING = 3;

    private TextView txt_record_description;
    private TextView txt_saving_record;
    private TextView txt_recording;
    private Button btn_start_record;
    private Button btn_stop_record;
    private ProgressBar progBar_record;
    private int progress;

    private int view_state;
    Timer timer = new Timer(true);
    MyTimerTask task = new MyTimerTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_gesture);

        txt_record_description = (TextView)findViewById(R.id.textView_record_description);
        txt_saving_record = (TextView)findViewById(R.id.textView_saving_record);
        txt_recording = (TextView)findViewById(R.id.textView_recording);
        btn_start_record = (Button)findViewById(R.id.btn_start_record);
        btn_stop_record = (Button)findViewById(R.id.btn_stop_record);
        progBar_record = (ProgressBar)findViewById(R.id.progressBar_record);

        view_state = 1;
        progress = 0;

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
            event.getText().add(RecordGestureActivity.this.getResources().getText(R.string.record_gesture));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnStartRecordClick(View view)
    {
        setViewState(2);
    }

    public void OnStopRecordClick(View view)
    {
        setViewState(3);
        timer.schedule(task, 1000, 1000);
    }

    private void setViewState(int state)
    {
        //if(state <= view_state && (view_state - state) != 2)
        //    return;

        btn_start_record.setVisibility(View.INVISIBLE);
        txt_record_description.setVisibility(View.INVISIBLE);
        progBar_record.setVisibility(View.INVISIBLE);
        txt_saving_record.setVisibility(View.INVISIBLE);
        btn_stop_record.setVisibility(View.INVISIBLE);
        txt_recording.setVisibility(View.INVISIBLE);

        switch (state)
        {
            case 1:
                btn_start_record.setVisibility(View.VISIBLE);
                txt_record_description.setVisibility(View.VISIBLE);
                break;
            case 2:
                btn_stop_record.setVisibility(View.VISIBLE);
                txt_recording.setVisibility(View.VISIBLE);
                break;
            case 3:
                progBar_record.setVisibility(View.VISIBLE);
                txt_saving_record.setVisibility(View.VISIBLE);
                break;
            default:
                Log.e(RecordGestureActivity.class.getSimpleName(), "Unknown view state!");
                break;
        }
    }

    public class MyTimerTask extends TimerTask
    {
        public void run()
        {
            progBar_record.post(new Runnable() {
                @Override
                public void run() {
                    //progBar_record.setProgress((int) (progBar_record.getProgress() + 20));
                    //progBar_record.setSecondaryProgress(progBar_record.getProgress() + 20);
                    progress = progress + 20;

                    if (progress >= 100) {
                        timer.cancel();
                        setViewState(1);
                    }
                    Log.d(TAG, "Progress: " + progress);
                }
            });
        }
    }

    ;
}
