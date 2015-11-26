package tw.edu.ntust.jojllman.wearableapplication;

import android.app.Application;
import android.os.Vibrator;

/**
 * Created by Lian on 2015/11/26.
 */
public class GlobalVariable extends Application {
    public static final int VIBRATE_LIGHT=1;
    public static final int VIBRATE_MID=2;
    public static final int VIBRATE_STRONG=4;

    private int vibrate_level=VIBRATE_LIGHT;

    public void setVibrate_level(int new_vibrate_level){
        vibrate_level=new_vibrate_level;
    }

    public int getVibrate_level(){
        return vibrate_level;
    }

    public void vibrate(int duration){
        if(duration>1) {
            Vibrator vi = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vi.cancel();
            long pattern[] = new long[duration];
            for (int i = 0; i < pattern.length; i += 2) {
                pattern[i] = 10 / vibrate_level;
                pattern[i + 1] = 2*vibrate_level+1;
            }
            vi.vibrate(pattern, -1);
        }
    }
}
