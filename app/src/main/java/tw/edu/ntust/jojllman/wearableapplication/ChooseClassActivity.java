package tw.edu.ntust.jojllman.wearableapplication;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.ImageButton;

public class ChooseClassActivity extends AppCompatActivity {
    int click_count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_class);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ImageButton ibtnVisual = (ImageButton) findViewById(R.id.img_btn_visual);
        ibtnVisual.getLayoutParams().height = (int)(metrics.heightPixels*0.4);

        ImageButton ibtnHearing = (ImageButton) findViewById(R.id.img_btn_hearing);
        ibtnHearing.getLayoutParams().height = (int)(metrics.heightPixels*0.4);

        Button btnHelp = (Button) findViewById(R.id.btn_help);
        btnHelp.getLayoutParams().height = (int)(metrics.heightPixels*0.1);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(ChooseClassActivity.this.getResources().getText(R.string.app_menu));
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void OnVisualClick(View view){
        click_count=0;
        view.announceForAccessibility("test test");

        Intent intent = new Intent();
        intent.setClass(ChooseClassActivity.this  , VisualSupportActivity.class);
        startActivity(intent);
        this.finish();
    }

    public void OnHearingClick(View view){
        click_count=0;

        Intent intent = new Intent();
        intent.setClass(ChooseClassActivity.this  , HearingSupportActivity.class);
        startActivity(intent);
        this.finish();
    }

    public void OnHelpClick(View view){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final Dialog dialog = new Dialog(ChooseClassActivity.this,R.style.CustomDialog){
            @Override
            public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    event.getText().add(ChooseClassActivity.this.getResources().getText(R.string.help_dialog));
                    return true;
                }
                return super.dispatchPopulateAccessibilityEvent(event);
            }
        };
        dialog.setContentView(R.layout.custom_dialog);

        ImageButton ibtn = (ImageButton)dialog.findViewById(R.id.img_btn_setting);
        ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(click_count++>2){
                    click_count=0;
                    if(dialog.isShowing()) {
                        dialog.hide();
                    }
                    Intent intent = new Intent();
                    intent.setClass(ChooseClassActivity.this  , AppManageActivity.class);
                    startActivity(intent);
                }
            }
        });

        Button btn = (Button)dialog.findViewById(R.id.btn_ok);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing()) {
                    click_count = 0;
                    dialog.hide();
                }
            }
        });

        dialog.show();
    }
}
