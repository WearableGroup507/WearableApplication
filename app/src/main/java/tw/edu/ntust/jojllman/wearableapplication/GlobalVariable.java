package tw.edu.ntust.jojllman.wearableapplication;

import android.app.Application;
import android.content.Context;
import android.os.Vibrator;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created by Lian on 2015/11/26.
 */
public class GlobalVariable extends Application {
    public static final int VIBRATE_LIGHT=1;
    public static final int VIBRATE_MID=2;
    public static final int VIBRATE_STRONG=4;

    private boolean _isSettingChanged=false;
    private int vibrate_level=VIBRATE_MID;

    public void setVibrate_level(int new_vibrate_level){
        if(new_vibrate_level != vibrate_level) {
            vibrate_level = new_vibrate_level;
            _isSettingChanged = true;
        }
    }

    public int getVibrate_level(){
        return vibrate_level;
    }

    public boolean isSettingChanged(){
        return _isSettingChanged;
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

    public boolean saveSetting(){
        _isSettingChanged=false;
        FileOutputStream out = null;
        try {
            //在 getFilesDir() 目錄底下建立 setting.txt 檔案用來進行寫入
            out = openFileOutput("setting.txt", Context.MODE_PRIVATE);

            String outstr="";
            outstr+="VIBRATE_LEVEL="+vibrate_level+"\n";

            //將資料寫入檔案中
            out.write(outstr.getBytes());
            out.flush();

            out.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean readSetting(){
        _isSettingChanged=false;
        FileInputStream in = null;
        StringBuffer data = new StringBuffer();
        try {
            //開啟 getFilesDir() 目錄底下名稱為 setting.txt 檔案
            in = openFileInput("setting.txt");

            //讀取該檔案的內容
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line + "\n");
            }
            in.close();
        } catch (Exception e) {
            return false;
        }
        int current_pos=data.indexOf("VIBRATE_LEVEL");
        if(current_pos>=0){
            vibrate_level=Integer.parseInt(data.substring(data.indexOf("=", current_pos) + 1, data.indexOf("\n", current_pos)));
        }else return false;
        return true;
    }
}
