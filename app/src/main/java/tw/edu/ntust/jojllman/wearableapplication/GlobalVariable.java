package tw.edu.ntust.jojllman.wearableapplication;

import android.app.Application;

/**
 * Created by Lian on 2015/11/26.
 */
public class GlobalVariable extends Application {
    public static final int VIBRATE_LIGHT=1;
    public static final int VIBRATE_MID=2;
    public static final int VIBRATE_STRONG=3;

    private int vibrate_level=VIBRATE_LIGHT;

    public void setVibrate_level(int new_vibrate_level){
        vibrate_level=new_vibrate_level;
    }

    public int getVibrate_level(){
        return vibrate_level;
    }
}
